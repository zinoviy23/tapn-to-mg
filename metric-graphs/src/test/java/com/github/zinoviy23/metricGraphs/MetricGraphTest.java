package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricGraphTest {
    private final Node node1 = new Node("Node1", "Node1");
    private final Node node2 = new Node("Node2", "Node2");
    private final Node node3 = new Node("Node3", "Node3");

    @Test(expected = IllegalArgumentException.class)
    public void addExistingNode() {
        MetricGraph.createBuilder("1")
                .addNode(new Node("1", "aaa"))
                .addNode(new Node("1", "bbb"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingArc() {
        MetricGraph
                .createBuilder("1")
                .addNode(node1)
                .addNode(node2)
                .addArc(Arc.createBuilder("1")
                                .setLabel("2")
                                .setSource(node1)
                                .setTarget(node2)
                                .createArc()
                )
                .addArc(Arc.createBuilder("1")
                                .setLabel("3")
                                .setTarget(node1)
                                .setSource(node2)
                                .createArc()
                );
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingPoint() {
        var arc1 = Arc.createBuilder("1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        var arc2 = Arc.createBuilder("2")
                           .setTarget(node1)
                           .setSource(node3)
                           .setLabel("2")
                           .setLength(1)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        MetricGraph.createBuilder("3")
                .addNode(node1)
                .addNode(node2)
                .addNode(node3)
                .addArc(arc1)
                .addArc(arc2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodeAndArc() {
        var arc1 = Arc.createBuilder("Node1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        MetricGraph.createBuilder("3")
                .addNode(node1)
                .addNode(node2)
                .addArc(arc1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodePoint() {
        var arc1 = Arc.createBuilder("arc1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("Node1", 1))
                           .createArc();

        MetricGraph.createBuilder("3")
                .addNode(node1)
                .addNode(node2)
                .addArc(arc1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodeAndGraph() {
        MetricGraph.createBuilder("Node1")
                .addNode(node1)
                .addNode(node2);
    }

    @Test
    public void validCreation() {
        var arc1 = Arc.createBuilder("arc1")
                          .setTarget(node1)
                          .setSource(node2)
                          .setLength(1)
                          .addPoint(new MovingPoint("p1", 0.5))
                          .setLabel("my arc")
                          .createArc();

        var arc2 = Arc.createBuilder("arc2")
                          .setTarget(node2)
                          .setSource(node3)
                          .setLength(1)
                          .addPoint(new MovingPoint("p2", 0.7))
                          .setLabel("my arc")
                          .createArc();

        var arc3 = Arc.createBuilder("arc3")
                          .setTarget(node3)
                          .setSource(node1)
                          .setLength(1)
                          .addPoint(new MovingPoint("p3", 0.2))
                          .setLabel("my arc")
                          .createArc();

        var graph = MetricGraph.createBuilder("Graph")
                            .addNode(node1)
                            .addNode(node2)
                            .addNode(node3)
                            .addArc(arc1)
                            .addArc(arc2)
                            .addArc(arc3)
                            .buildGraph();

        var arcs = graph.getGraph().edgeSet();
        assertEquals(3, arcs.size());
        assertTrue(arcs.contains(arc1));
        assertTrue(arcs.contains(arc2));
        assertTrue(arcs.contains(arc3));

        var nodes = graph.getGraph().vertexSet();
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(node1));
        assertTrue(nodes.contains(node2));
        assertTrue(nodes.contains(node3));
    }

    @Test
    public void toStringTest() {
        var graph = MetricGraph.createBuilder("1").buildGraph();
        assertEquals("MetricGraph{id='1'}", graph.toString());
    }
}