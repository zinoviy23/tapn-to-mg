package com.github.zinoviy23.metricGraphs.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class MetricGraphWriter implements AutoCloseable, Closeable {
    private static final JsonFactory factory = new JsonFactory();

    private static final String DIRECTED = "directed";
    private static final String ID = "id";
    private static final String GRAPH = "graph";
    private static final String ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE = "Already write graph to stream!";
    private static final String TYPE = "type";
    private static final String METRIC_GRAPH = "metric graph";
    private static final String LABEL = "label";
    private static final String NODES = "nodes";
    private static final String EDGES = "edges";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String RELATION = "relation";
    private static final String NODES_CONNECTION = "nodes connection";
    private static final String METADATA = "metadata";
    private static final String POINTS = "points";
    private static final String POSITION = "position";
    private static final String COMMENT = "comment";

    private final JsonGenerator generator;
    private boolean isWritten;
    private boolean errorOccurred;

    public MetricGraphWriter(@NotNull OutputStream outputStream, boolean usePrettyPrinter) throws IOException {
        this(factory.createGenerator(outputStream), usePrettyPrinter);
    }

    public MetricGraphWriter(@NotNull Writer writer, boolean usePrettyPrinter) throws IOException {
        this(factory.createGenerator(writer), usePrettyPrinter);
    }

    private MetricGraphWriter(@NotNull JsonGenerator generator, boolean usePrettyPrint) {
        this.generator = generator;
        if (usePrettyPrint) {
            this.generator.useDefaultPrettyPrinter();
        }
    }

    public void write(@NotNull MetricGraph graph) throws IOException {
        if (isWritten || errorOccurred) throw new IllegalStateException(ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE);

        try {
            internalWrite(graph);
        } catch (IOException e) {
            errorOccurred = true;
            throw new IOException(e);
        }

        isWritten = true;
    }

    private void internalWrite(@NotNull MetricGraph graph) throws IOException {
        generator.writeStartObject();
        generator.writeObjectFieldStart(GRAPH);

        writeGraphInfo(graph);
        writeNodes(graph);
        writeEdges(graph);
        writeComment(graph);

        generator.writeEndObject();
        generator.writeEndObject();
    }

    private void writeGraphInfo(@NotNull MetricGraph graph) throws IOException {
        generator.writeBooleanField(DIRECTED, true);
        generator.writeStringField(ID, graph.getId());
        generator.writeStringField(TYPE, METRIC_GRAPH);
        generator.writeStringField(LABEL, graph.getLabel());
    }

    private void writeNodes(@NotNull MetricGraph graph) throws IOException {
        generator.writeObjectFieldStart(NODES);

        for (var node : graph.getGraph().vertexSet()) {
            generator.writeObjectFieldStart(node.getId());
            generator.writeStringField(LABEL, node.getLabel());
            writeComment(node);
            generator.writeEndObject();
        }
        generator.writeEndObject();
    }

    private void writeEdges(@NotNull MetricGraph graph) throws IOException {
        generator.writeArrayFieldStart(EDGES);

        for (var arc : graph.getGraph().edgeSet()) {
            generator.writeStartObject();
            generator.writeStringField(ID, arc.getId());
            generator.writeStringField(SOURCE, arc.getSource().getId());
            generator.writeStringField(TARGET, arc.getTarget().getId());
            generator.writeStringField(RELATION, NODES_CONNECTION);
            generator.writeStringField(LABEL, arc.getLabel());
            writeArcMetadata(arc);
            generator.writeEndObject();
        }

        generator.writeEndArray();
    }

    private void writeArcMetadata(@NotNull Arc arc) throws IOException {
        generator.writeObjectFieldStart(METADATA);

        generator.writeArrayFieldStart(POINTS);
        for (var point : arc.getPoints()) {
            generator.writeStartObject();
            generator.writeStringField(ID, point.getId());
            generator.writeNumberField(POSITION, point.getPosition());
            writeComment(point);
            generator.writeEndObject();
        }
        generator.writeEndArray();

        if (arc.getComment() != null) {
            generator.writeStringField(COMMENT, arc.getComment());
        }

        generator.writeEndObject();
    }

    private void writeComment(@NotNull ObjectWithComment objectWithComment) throws IOException {
        if (objectWithComment.getComment() == null) return;

        generator.writeObjectFieldStart(METADATA);
        generator.writeStringField(COMMENT, objectWithComment.getComment());

        generator.writeEndObject();
    }


    @Override
    public void close() throws IOException {
        generator.close();
    }
}
