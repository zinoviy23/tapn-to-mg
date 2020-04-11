package com.github.zinoviy23.metricGraphs.io.graphml;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.util.GraphLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.drawing.model.LayoutModel2D;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class MetricGraphYedWriter implements Closeable, AutoCloseable {
  private boolean isWritten;
  private boolean errorOccurred;
  private final Writer resultWriter;
  private final File currentFile;
  private final boolean applyLayout;

  public MetricGraphYedWriter(@NotNull Writer writer, boolean applyLayout) throws IOException {
    currentFile = File.createTempFile("result", ".xml");
    resultWriter = writer;
    this.applyLayout = applyLayout;
  }

  public void write(@NotNull MetricGraph graph) throws IOException {
    if (isWritten || errorOccurred) {
      throw new IllegalStateException(XmlUtils.ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE);
    }

    try (var fileWriter = new FileWriter(currentFile); var writer = new YedWriter(fileWriter, applyLayout)) {
      writer.write(graph);
    } catch (XMLStreamException e) {
      errorOccurred = true;
      throw new IOException(e);
    }
    try (var fileReader = new FileReader(currentFile)) {
      addIndents(fileReader);
    } catch (TransformerException e) {
      errorOccurred = true;
      throw new IOException(e);
    }
    isWritten = true;
  }

  private void addIndents(@NotNull Reader reader) throws TransformerException {
    var transformer = XmlUtils.transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.transform(new StreamSource(reader), new StreamResult(resultWriter));
  }

  @Override
  public void close() throws IOException {
    if (!currentFile.delete()) {
      throw new IOException("Cannot delete cache file!");
    }
  }

  private static final class YedWriter implements Closeable, AutoCloseable {
    public static final String EDGE_POINTS = "d12";
    private static final String GRAPH_DESCR = "d0";
    private static final String NODE_DESCR = "d5";
    private static final String EDGE_DESCR = "d9";
    private static final String NODE_GRAPHICS = "d6";
    private static final String Y = "http://www.yworks.com/xml/graphml";
    private static final String EDGE_GRAPHICS = "d10";
    public static final String EDGE_LENGTH = "d11";

    private final XMLStreamWriter writer;
    private final boolean applyLayout;

    private final Set<Arc> drownArc = new HashSet<>();

    private YedWriter(Writer writer, boolean applyLayout) throws XMLStreamException {
      this.writer = XmlUtils.xmlOutputFactory.createXMLStreamWriter(writer);
      this.applyLayout = applyLayout;
    }

    private void write(@NotNull MetricGraph graph) throws XMLStreamException {
      writer.writeStartDocument();
      writer.writeStartElement("graphml");
      writeYedSchemes();
      writeDataElements();
      writeGraph(graph);
      writer.writeEndDocument();
    }

    private void writeYedSchemes() throws XMLStreamException {
      writer.writeNamespace("xmlns", "http://graphml.graphdrawing.org/xmlns");
      writer.writeNamespace( "java", "http://www.yworks.com/xml/yfiles-common/1.0/java");
      writer.writeNamespace( "sys", "http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0");
      writer.writeNamespace( "x", "http://www.yworks.com/xml/yfiles-common/markup/2.0");
      var xsi = "http://www.w3.org/2001/XMLSchema-instance";
      writer.writeNamespace( "xsi", xsi);
      writer.writeNamespace( "y", Y);
      writer.writeNamespace( "yed", "http://www.yworks.com/xml/yed/3");
      writer.writeAttribute(xsi, "schemaLocation", "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
    }

    private void writeDataElements() throws XMLStreamException {
      writeDataDef(GRAPH_DESCR, "graph", "Description", "string", null);
      writeDataDef("d1", "port", null, null, "portgraphics");
      writeDataDef("d2", "port", null, null, "portgeometry");
      writeDataDef("d3", "port", null, null, "portuserdata");
      writeDataDef("d4", "node", "url", "string", null);
      writeDataDef(NODE_DESCR, "node", "description", "string", null);
      writeDataDef(NODE_GRAPHICS, "node", null, null, "nodegraphics");
      writeDataDef("d7", "graphml", null, null, "resources");
      writeDataDef("d8", "edge", "url", "string", null);
      writeDataDef(EDGE_DESCR, "edge", "description", "string", null);
      writeDataDef(EDGE_GRAPHICS, "edge", null, null, "edgegraphics");
      writeDataDef(EDGE_LENGTH, "edge", "length", "double", null);
      writeDataDef(EDGE_POINTS, "edge", "points", null, null);
    }

    private void writeDataDef(@NotNull String id,
                              @NotNull String forAttr,
                              @Nullable String name,
                              @Nullable String type,
                              @Nullable String yfilesType) throws XMLStreamException {
      writer.writeStartElement("key");
      writer.writeAttribute("id", id);
      writer.writeAttribute("for", forAttr);
      if (name != null) {
        writer.writeAttribute("attr.name", name);
      }
      if (type != null) {
        writer.writeAttribute("attr.type", type);
      }
      if (yfilesType != null) {
        writer.writeAttribute("yfiles.type", yfilesType);
      }
      writer.writeEndElement();
    }

    private void writeGraph(MetricGraph graph) throws XMLStreamException {
      writer.writeStartElement("graph");
      writer.writeAttribute("edgedefault", "directed");
      writer.writeAttribute("id", graph.getId());
      writer.writeStartElement("data");
      writer.writeAttribute("key", GRAPH_DESCR);
      writer.writeCharacters(graph.getComment());
      writer.writeEndElement();

      LayoutModel2D<Node> layoutModel2D = applyLayout
          ? new GraphLayout().createLayout(graph.getGraph(), 200, 300)
          : null;

      for (Node node : graph.getGraph().vertexSet()) {
        writeNode(node, layoutModel2D);
      }

      for (Arc arc : graph.getGraph().edgeSet()) {
        writeArc(arc, graph);
      }
    }

    private void writeArc(@NotNull Arc arc, @NotNull MetricGraph graph) throws XMLStreamException {
      writer.writeStartElement("edge");
      writer.writeAttribute("id", arc.getId());
      writer.writeAttribute("source", arc.getSource().getId());
      writer.writeAttribute("target", arc.getTarget().getId());

      writeComment(arc.getComment(), EDGE_DESCR);

      writeArcGraphics(arc, drownArc.contains(graph.getReversal(arc)));

      writer.writeStartElement("data");
      writer.writeAttribute("key", EDGE_LENGTH);
      writer.writeCharacters(Double.toString(arc.getLength()));
      writer.writeEndElement();

      writer.writeStartElement("data");
      writer.writeAttribute("key", EDGE_POINTS);
      for (MovingPoint point : arc.getPoints()) {
        writePoint(point);
      }
      writer.writeEndElement();

      writer.writeEndElement();

      drownArc.add(arc);
    }

    private void writePoint(@NotNull MovingPoint point) throws XMLStreamException {
      writer.writeStartElement("point");
      writer.writeAttribute("id", point.getId());
      writer.writeAttribute("position", Double.toString(point.getPosition()));
      if (point.getComment() != null) {
        writer.writeCharacters(point.getComment());
      }
      writer.writeEndElement();
    }

    private void writeArcGraphics(@NotNull Arc arc, boolean hide) throws XMLStreamException {
      writer.writeStartElement("data");
      writer.writeAttribute("key", EDGE_GRAPHICS);
      writer.writeStartElement(Y, "PolyLineEdge");

      writer.writeStartElement(Y, "EdgeLabel");
      if (hide) {
        writer.writeAttribute("textColor", "#00000000");
      }
      writer.writeCharacters(arc.getLabel() + ", " + arc.getLength());
      writer.writeEndElement();

      writer.writeStartElement(Y, "Path");
      writer.writeAttribute("sx", "0.0");
      writer.writeAttribute("sy", "0.0");
      writer.writeAttribute("tx", "0.0");
      writer.writeAttribute("ty", "0.0");
      writer.writeEndElement();

      writer.writeStartElement(Y, "LineStyle");
      writer.writeAttribute("color",  hide ? "#00000000" : "#000000");
      writer.writeAttribute("width", "1.0");
      writer.writeEndElement();

      writer.writeStartElement(Y, "Arrows");
      writer.writeAttribute("source", "none");
      writer.writeAttribute("target", "none");
      writer.writeEndElement();

      writer.writeStartElement(Y, "BendElement");
      writer.writeAttribute("smoothed", "false");
      writer.writeEndElement();

      writer.writeEndElement();
      writer.writeEndElement();
    }

    private void writeComment(String comment, String descr) throws XMLStreamException {
      if (comment != null) {
        writer.writeStartElement("data");
        writer.writeAttribute("key", descr);
        writer.writeCharacters(comment);
        writer.writeEndElement();
      }
    }

    private void writeNode(@NotNull Node node, @Nullable LayoutModel2D<Node> layoutModel2D) throws XMLStreamException {
      writer.writeStartElement("node");
      writer.writeAttribute("id", node.getId());
      writeComment(node.getComment(), NODE_DESCR);
      writeNodeGraphics(node, layoutModel2D);
      writer.writeEndElement();
    }

    private void writeNodeGraphics(@NotNull Node node, @Nullable LayoutModel2D<Node> layoutModel2D) throws XMLStreamException {
      writer.writeStartElement("data");
      writer.writeAttribute("key", NODE_GRAPHICS);
      writer.writeStartElement(Y, "ShapeNode");

      writer.writeStartElement(Y, "NodeLabel");
      writer.writeCharacters(node.getLabel());
      writer.writeEndElement();

      writer.writeStartElement(Y, "Geometry");
      writer.writeAttribute("height", "30");
      writer.writeAttribute("width", "30");
      if (layoutModel2D != null) {
        var point = layoutModel2D.get(node);
        writer.writeAttribute("x", Double.toString(point.getX()));
        writer.writeAttribute("y", Double.toString(point.getY()));
      }
      writer.writeEndElement();

      writer.writeStartElement(Y, "Fill");
      writer.writeAttribute("color", "#FFCC00");
      writer.writeAttribute("transparent", "false");
      writer.writeEndElement();

      writer.writeStartElement(Y, "Shape");
      writer.writeAttribute("type", "ellipse");
      writer.writeEndElement();

      writer.writeEndElement();
      writer.writeEndElement();
    }

    @Override
    public void close() throws IOException {
      try {
        writer.close();
      } catch (XMLStreamException e) {
        throw new IOException(e);
      }
    }
  }
}
