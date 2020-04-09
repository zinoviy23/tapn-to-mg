package com.github.zinoviy23.metricGraphs.io;

import com.fasterxml.jackson.core.JsonFactory;

final class IoUtils {
  static final JsonFactory factory = new JsonFactory();

  static final String LENGTH = "length";
  static final String DIRECTED = "directed";
  static final String ID = "id";
  static final String GRAPH = "graph";
  static final String ALREADY_WRITE_GRAPH_TO_STREAM_MESSAGE = "Already write graph to stream!";
  static final String ALREADY_READ_GRAPH_FROM_STREAM_MESSAGE = "Already read graph from stream";
  static final String TYPE = "type";
  static final String METRIC_GRAPH = "metric graph";
  static final String LABEL = "label";
  static final String NODES = "nodes";
  static final String EDGES = "edges";
  static final String SOURCE = "source";
  static final String TARGET = "target";
  static final String RELATION = "relation";
  static final String NODES_CONNECTION = "nodes connection";
  static final String METADATA = "metadata";
  static final String POINTS = "points";
  static final String POSITION = "position";
  static final String COMMENT = "comment";

  private IoUtils() {
  }
}
