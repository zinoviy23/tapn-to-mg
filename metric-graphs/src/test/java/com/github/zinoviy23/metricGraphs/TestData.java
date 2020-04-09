package com.github.zinoviy23.metricGraphs;

public final class TestData {
  public static final Node node1 = new Node("Node1", "Node1", "AAAAAAA this is node!");
  public static final Node node2 = new Node("Node2", "Node2");
  public static final Node node3 = new Node("Node3", "Node3");

  public static final Arc arc1 = Arc.createBuilder()
      .setId("arc1")
      .setTarget(node1)
      .setSource(node2)
      .setLength(1)
      .addPoint(new MovingPoint("p1", 0.5))
      .setLabel("my arc")
      .createArc();

  public static final Arc arc2 = Arc.createBuilder()
      .setId("arc2")
      .setTarget(node2)
      .setSource(node3)
      .setLength(1)
      .addPoint(new MovingPoint("p2", 0.7))
      .setLabel("my arc")
      .createArc();

  public static final Arc arc3 = Arc.createBuilder()
      .setId("arc3")
      .setTarget(node3)
      .setSource(node1)
      .setLength(1)
      .addPoint(new MovingPoint("p3", 0.2))
      .setLabel("my arc")
      .createArc();

  private TestData() {
  }

  public static MetricGraph createGraph() {
    return MetricGraph.createBuilder()
        .setId("Graph")
        .addNode(node1)
        .addNode(node2)
        .addNode(node3)
        .addArc(arc1).withReversal("rev_arc1")
        .addArc(arc2).withReversal("rev_arc2")
        .addArc(arc3).withReversal("rev_arc3")
        .setComment("Comment")
        .buildGraph();
  }
}
