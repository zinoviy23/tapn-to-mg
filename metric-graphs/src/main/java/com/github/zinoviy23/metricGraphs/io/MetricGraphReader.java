package com.github.zinoviy23.metricGraphs.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.util.Ref;
import com.github.zinoviy23.metricGraphs.util.ThrowableBiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Triple;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class MetricGraphReader implements AutoCloseable, Closeable {
    private final JsonParser parser;
    private boolean isRead;
    private boolean errorOccurred;

    public MetricGraphReader(@NotNull Reader reader) throws IOException {
        parser = IoUtils.factory.createParser(reader);
    }

    public MetricGraphReader(@NotNull InputStream inputStream) throws IOException {
        parser = IoUtils.factory.createParser(inputStream);
    }

    public MetricGraphReader(@NotNull File file) throws IOException {
        parser = IoUtils.factory.createParser(file);
    }

    public @Nullable MetricGraph read() throws IOException {
        if (isRead || errorOccurred) {
            throw new IllegalStateException(IoUtils.ALREADY_READ_GRAPH_FROM_STREAM_MESSAGE);
        }

        try {
            var graph = internalRead();
            isRead = true;
            return graph;
        } catch (IOException e) {
            errorOccurred = true;
            throw new IOException(e);
        }
    }

    private @Nullable MetricGraph internalRead() throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.getCurrentName();
            if (IoUtils.GRAPH.equals(currentName)) {
                return readGraph();
            }
        }

        return null;
    }

    private @NotNull MetricGraph readGraph() throws IOException {
        var graphBuilder = MetricGraph.createBuilder();
        Map<String, Node> nodes = null;
        List<Triple<String, String, Arc.ArcBuilder>> edgesInfo = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (IoUtils.ID.equals(parser.getCurrentName())) {
                parser.nextToken();
                graphBuilder.setId(parser.getValueAsString());
            }

            if (IoUtils.LABEL.equals(parser.getCurrentName())) {
                parser.nextToken();
                graphBuilder.setLabel(parser.getValueAsString());
            }

            if (IoUtils.NODES.equals(parser.getCurrentName())) {
                parser.nextToken();
                nodes = readNodes(graphBuilder);
            }

            if (IoUtils.EDGES.equals(parser.getCurrentName())) {
                parser.nextToken();
                edgesInfo = readEdges();
            }
        }

        if (edgesInfo != null && nodes != null) {
            for (var triple : edgesInfo) {
                var source = nodes.get(triple.getFirst());
                var target = nodes.get(triple.getSecond());
                if (source != null && target != null) {
                    var arcBuilder = triple.getThird();
                    graphBuilder.addArc(
                            arcBuilder
                                    .setSource(source)
                                    .setTarget(target)
                                    .createArc()
                    );
                } else {
                    throw new RuntimeException("(((");
                }
            }
        } else if (edgesInfo != null) {
            throw new RuntimeException("(((((");
        }

        return graphBuilder.buildGraph();
    }

    private @NotNull List<Triple<String, String, Arc.ArcBuilder>> readEdges() throws IOException {
        var result = new ArrayList<Triple<String, String, Arc.ArcBuilder>>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                result.add(readEdge());
            }
        }
        return result;
    }

    private @NotNull Triple<String, String, Arc.ArcBuilder> readEdge() throws IOException {
        var arcBuilder = Arc.createBuilder();
        String source = null;
        String target = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            var currentName = parser.getCurrentName();
            if (IoUtils.LABEL.equals(currentName)) {
                parser.nextToken();
                arcBuilder.setLabel(parser.getValueAsString());
            }
            if (IoUtils.SOURCE.equals(currentName)) {
                parser.nextToken();
                source = parser.getValueAsString();
            }
            if (IoUtils.TARGET.equals(currentName)) {
                parser.nextToken();
                target = parser.getValueAsString();
            }
            if (IoUtils.ID.equals(currentName)) {
                parser.nextToken();
                arcBuilder.setId(parser.getValueAsString());
                System.out.println("read edge " + parser.getValueAsString());
            }
            if (IoUtils.METADATA.equals(currentName)) {
                readArcMetadata(arcBuilder);
            }
        }

        return Triple.of(source, target, arcBuilder);
    }

    private void readArcMetadata(@NotNull Arc.ArcBuilder arcBuilder) throws IOException {
        fetchMetadata((name, balance) -> {
            if (IoUtils.COMMENT.equals(name) && balance == 1) {
                parser.nextToken();
                arcBuilder.setComment(parser.getValueAsString());
            }
            if (IoUtils.POINTS.equals(name) && balance == 1) {
                parser.nextToken();
                readPoints(arcBuilder);
            }
            if (IoUtils.LENGTH.equals(name) && balance == 1) {
                parser.nextToken();
                arcBuilder.setLength(parser.getNumberValue().doubleValue());
            }
        });
    }

    private void readPoints(@NotNull Arc.ArcBuilder arcBuilder) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                arcBuilder.addPoint(readPoint());
            }
        }
    }

    private MovingPoint readPoint() throws IOException {
        String id = null;
        Double position = null;
        Ref<String> comment = new Ref<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            var currentName = parser.getCurrentName();
            if (IoUtils.ID.equals(currentName)) {
                parser.nextToken();
                id = parser.getValueAsString();
                System.out.println("read point " + id);
            }
            if (IoUtils.POSITION.equals(currentName)) {
                parser.nextToken();
                position = parser.getNumberValue().doubleValue();
            }
            if (IoUtils.METADATA.equals(currentName)) {
                fetchMetadata((name, balance) -> {
                    // comment must be property of metadata obj, not its child
                    if (IoUtils.COMMENT.equals(name) && balance == 1) {
                        parser.nextToken();
                        comment.setData(parser.getValueAsString());
                    }
                });
            }
        }

        //noinspection ConstantConditions
        return new MovingPoint(id, position, comment.getData());
    }

    private @NotNull Map<String, Node> readNodes(@NotNull MetricGraph.MetricGraphBuilder graphBuilder) throws IOException {
        Map<String, Node> nodes = new HashMap<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            System.out.println("read nodes");
            String id = parser.getCurrentName();
            if (id != null) {
                var node = readNode(id);
                graphBuilder.addNode(node);
                nodes.put(id, node);
            }
        }
        return nodes;
    }

    private @NotNull Node readNode(@NotNull String id) throws IOException {
        String label = null;
        Ref<String> comment = new Ref<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (IoUtils.LABEL.equals(parser.getCurrentName())) {
                System.out.println("read node " + id);
                parser.nextToken();
                label = parser.getValueAsString();
            }
            if (IoUtils.METADATA.equals(parser.getCurrentName())) {
                fetchMetadata((name, balance) -> {
                    // comment must be property of metadata obj, not its child
                    if (IoUtils.COMMENT.equals(name) && balance == 1) {
                        parser.nextToken();
                        comment.setData(parser.getValueAsString());
                    }
                });
            }
        }
        return new Node(id, label, comment.getData());
    }

    private void fetchMetadata(ThrowableBiConsumer<String, Integer, IOException> fetcher) throws IOException {
        parser.nextToken();
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            parser.nextToken();
            return;
        }
        int balance = 1;
        while (balance != 0) {
            parser.nextToken();
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                balance++;
            }
            if (parser.currentToken() == JsonToken.END_OBJECT) {
                balance--;
            }
            fetcher.consume(parser.getCurrentName(), balance);
        }
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }
}
