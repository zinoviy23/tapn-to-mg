package com.github.zinoviy23.metricGraphs.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class MetricGraphJsonWriter implements MetricGraphWriter<IOException> {

  private final JsonGenerator generator;
  private boolean isWritten;
  private boolean errorOccurred;

  public MetricGraphJsonWriter(@NotNull OutputStream outputStream, boolean usePrettyPrinter) throws IOException {
    this(IoUtils.factory.createGenerator(outputStream), usePrettyPrinter);
  }

  public MetricGraphJsonWriter(@NotNull Writer writer, boolean usePrettyPrinter) throws IOException {
    this(IoUtils.factory.createGenerator(writer), usePrettyPrinter);
  }

  private MetricGraphJsonWriter(@NotNull JsonGenerator generator, boolean usePrettyPrint) {
    this.generator = generator;
    if (usePrettyPrint) {
      this.generator.useDefaultPrettyPrinter();
    }
  }

  public void write(@NotNull MetricGraph graph) throws IOException {
    if (isWritten || errorOccurred) throw new IllegalStateException(IoUtils.ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE);

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
    generator.writeObjectFieldStart(IoUtils.GRAPH);

    writeGraphInfo(graph);
    writeNodes(graph);
    writeEdges(graph);
    writeComment(graph);

    generator.writeEndObject();
    generator.writeEndObject();
  }

  private void writeGraphInfo(@NotNull MetricGraph graph) throws IOException {
    generator.writeBooleanField(IoUtils.DIRECTED, true);
    generator.writeStringField(IoUtils.ID, graph.getId());
    generator.writeStringField(IoUtils.TYPE, IoUtils.METRIC_GRAPH);
    generator.writeStringField(IoUtils.LABEL, graph.getLabel());
  }

  private void writeNodes(@NotNull MetricGraph graph) throws IOException {
    generator.writeObjectFieldStart(IoUtils.NODES);

    for (var node : graph.getGraph().vertexSet()) {
      generator.writeObjectFieldStart(node.getId());
      generator.writeStringField(IoUtils.LABEL, node.getLabel());
      writeComment(node);
      generator.writeEndObject();
    }
    generator.writeEndObject();
  }

  private void writeEdges(@NotNull MetricGraph graph) throws IOException {
    generator.writeArrayFieldStart(IoUtils.EDGES);

    for (var arc : graph.getGraph().edgeSet()) {
      generator.writeStartObject();
      generator.writeStringField(IoUtils.ID, arc.getId());
      generator.writeStringField(IoUtils.SOURCE, arc.getSource().getId());
      generator.writeStringField(IoUtils.TARGET, arc.getTarget().getId());
      generator.writeStringField(IoUtils.RELATION, IoUtils.NODES_CONNECTION);
      generator.writeStringField(IoUtils.LABEL, arc.getLabel());
      writeArcMetadata(arc);
      generator.writeEndObject();
    }

    generator.writeEndArray();
  }

  private void writeArcMetadata(@NotNull Arc arc) throws IOException {
    generator.writeObjectFieldStart(IoUtils.METADATA);

    generator.writeArrayFieldStart(IoUtils.POINTS);
    for (var point : arc.getPoints()) {
      generator.writeStartObject();
      generator.writeStringField(IoUtils.ID, point.getId());
      generator.writeNumberField(IoUtils.POSITION, point.getPosition());
      writeComment(point);
      generator.writeEndObject();
    }
    generator.writeEndArray();

    if (arc.getComment() != null) {
      generator.writeStringField(IoUtils.COMMENT, arc.getComment());
    }

    generator.writeNumberField(IoUtils.LENGTH, arc.getLength());

    generator.writeEndObject();
  }

  private void writeComment(@NotNull ObjectWithComment objectWithComment) throws IOException {
    if (objectWithComment.getComment() == null) return;

    generator.writeObjectFieldStart(IoUtils.METADATA);
    generator.writeStringField(IoUtils.COMMENT, objectWithComment.getComment());

    generator.writeEndObject();
  }


  @Override
  public void close() throws IOException {
    generator.close();
  }
}
