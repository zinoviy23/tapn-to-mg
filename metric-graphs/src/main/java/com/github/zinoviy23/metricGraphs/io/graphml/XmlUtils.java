package com.github.zinoviy23.metricGraphs.io.graphml;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;

class XmlUtils {
  static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
  static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  static final String ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE = "Already write graph to stream!";
}
