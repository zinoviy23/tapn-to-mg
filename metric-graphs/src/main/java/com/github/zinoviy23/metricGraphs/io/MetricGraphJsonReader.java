package com.github.zinoviy23.metricGraphs.io;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.util.ContainerUtil;
import com.github.zinoviy23.metricGraphs.util.Ref;
import com.github.zinoviy23.metricGraphs.util.ThrowableBiConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

import java.io.*;
import java.util.*;

public final class MetricGraphJsonReader implements AutoCloseable, Closeable {
  private static final Logger LOG = LogManager.getLogger(MetricGraphJsonReader.class);

  private static final String GRAPH_HAS_EDGES_BUT_HASN_T_NODES_MESSAGE = "Graph has edges, but hasn't nodes";
  private static final String HASN_T_ANY_NODES_WITH_ID_MESSAGE = "Hasn't any nodes with id=";
  private static final String ARC_S_MUST_HAVE_REVERSAL_EDGE = "Arc %s must have reversal edge!";

  private final JsonParser parser;
  private boolean isRead;
  private boolean errorOccurred;

  public MetricGraphJsonReader(@NotNull Reader reader) throws IOException {
    parser = IoUtils.factory.createParser(reader);
  }

  public MetricGraphJsonReader(@NotNull InputStream inputStream) throws IOException {
    parser = IoUtils.factory.createParser(inputStream);
  }

  public MetricGraphJsonReader(@NotNull File file) throws IOException {
    parser = IoUtils.factory.createParser(file);
  }

  public MetricGraphJsonReader(@NotNull String source) throws IOException {
    parser = IoUtils.factory.createParser(source);
  }

  public @Nullable MetricGraph read() throws IOException {
    if (isRead || errorOccurred) {
      throw new IllegalStateException(IoUtils.ALREADY_READ_GRAPH_FROM_STREAM_MESSAGE);
    }

    try {
      var graph = internalRead();
      isRead = true;
      return graph;
    } catch (JsonParseException e) {
      errorOccurred = true;
      throw new MetricGraphReadingException(e.getMessage());
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
    Map<String, Map<String, Arc.ArcBuilder>> edgesInfo = null;
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      var currentName = parser.getCurrentName();
      if (IoUtils.ID.equals(currentName)) {
        parser.nextToken();
        graphBuilder.setId(parser.getValueAsString());
      }

      if (IoUtils.LABEL.equals(currentName)) {
        parser.nextToken();
        graphBuilder.setLabel(parser.getValueAsString());
      }

      if (IoUtils.NODES.equals(currentName)) {
        parser.nextToken();
        nodes = readNodes(graphBuilder);
      }

      if (IoUtils.EDGES.equals(currentName)) {
        parser.nextToken();
        edgesInfo = readEdges();
      }

      if (IoUtils.METADATA.equals(currentName)) {
        fetchMetadata((name, balance) -> {
          if (IoUtils.COMMENT.equals(name) && balance == 1) {
            parser.nextToken();
            graphBuilder.setComment(parser.getValueAsString());
          }
        });
      }
    }

    if (edgesInfo != null && nodes != null) {
      Set<String> processedArcs = new HashSet<>();

      var edgesStream = edgesInfo.entrySet().stream()
          .flatMap(mapEntry ->
              mapEntry.getValue().entrySet().stream()
                  .map(targetAndBuilder -> Triple.of(mapEntry.getKey(),
                      targetAndBuilder.getKey(),
                      targetAndBuilder.getValue()
                      )
                  )
          );

      for (var triple : ContainerUtil.iterate(edgesStream)) {
        if (processedArcs.contains(triple.getThird().getId())) continue;

        var stringStringPair = addArcAndReversal(graphBuilder, nodes, edgesInfo, triple);
        processedArcs.add(stringStringPair.getFirst());
        processedArcs.add(stringStringPair.getSecond());
      }
    } else if (edgesInfo != null) {
      throw new MetricGraphReadingException(GRAPH_HAS_EDGES_BUT_HASN_T_NODES_MESSAGE);
    }

    return graphBuilder.buildGraph();
  }

  private @NotNull Pair<String, String> addArcAndReversal(@NotNull MetricGraph.MetricGraphBuilder graphBuilder,
                                                          @NotNull Map<String, Node> finalNodes,
                                                          @NotNull Map<String, Map<String, Arc.ArcBuilder>> finalEdgesInfo,
                                                          @NotNull Triple<String, String, Arc.ArcBuilder> triple) {
    var source = finalNodes.get(triple.getFirst());
    var target = finalNodes.get(triple.getSecond());
    if (source == null) {
      throw new MetricGraphReadingException(HASN_T_ANY_NODES_WITH_ID_MESSAGE + triple.getFirst());
    }
    if (target == null) {
      throw new MetricGraphReadingException(HASN_T_ANY_NODES_WITH_ID_MESSAGE + triple.getSecond());
    }

    var arcBuilder = triple.getThird();
    var reversal = ContainerUtil.getFromTable(finalEdgesInfo, triple.getSecond(), triple.getFirst());
    if (reversal == null) {
      throw new MetricGraphReadingException(String.format(ARC_S_MUST_HAVE_REVERSAL_EDGE, arcBuilder.getId()));
    }

    var arc = arcBuilder
        .setSource(source)
        .setTarget(target)
        .createArc();
    graphBuilder
        .addArc(arc)
        .setReversalComment(reversal.getComment())
        .setReversalLabel(reversal.getLabel())
        .withReversal(reversal.getId(), reversal.getPoints());

    return Pair.of(arc.getId(), reversal.getId());
  }

  private @NotNull Map<String, Map<String, Arc.ArcBuilder>> readEdges() throws IOException {
    var result = new HashMap<String, Map<String, Arc.ArcBuilder>>();
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      if (parser.currentToken() == JsonToken.START_OBJECT) {
        var edgeInfo = readEdge();
        result.computeIfAbsent(edgeInfo.getFirst(), (k) -> new HashMap<>())
            .put(edgeInfo.getSecond(), edgeInfo.getThird());
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
        var valueAsString = parser.getValueAsString();
        arcBuilder.setId(valueAsString);
        LOG.debug(() -> "read edge " + valueAsString);
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
        if (parser.getCurrentToken().isNumeric()) {
          arcBuilder.setLength(parser.getNumberValue().doubleValue());
        } else if ("Infinity".equals(parser.getValueAsString())) {
          arcBuilder.setLength(Double.POSITIVE_INFINITY);
        } else {
          throw new MetricGraphReadingException("Unexpected value: " + parser.getValueAsString());
        }
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
        String finalId = id;
        LOG.debug(() -> "read point " + finalId);
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
      LOG.debug(() -> "read nodes");
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
        LOG.debug(() -> "read node " + id);
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
    if (Node.INFINITY_NODE_LABEL.equals(label)) {
      return Node.createInfinity(id);
    }
    return Node.createNode(id, label, comment.getData());
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

  public @NotNull JsonLocation lastLocation() {
    return parser.getCurrentLocation();
  }

  @Override
  public void close() throws IOException {
    parser.close();
  }
}
