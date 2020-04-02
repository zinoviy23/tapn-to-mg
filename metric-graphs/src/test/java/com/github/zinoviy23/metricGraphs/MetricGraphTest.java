package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static com.github.zinoviy23.metricGraphs.TestData.*;
import static org.junit.Assert.*;

public class MetricGraphTest {
    @Test(expected = IllegalArgumentException.class)
    public void addExistingNode() {
        MetricGraph.createBuilder()
                .setId("1")
                .addNode(new Node("1", "aaa"))
                .addNode(new Node("1", "bbb"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingArc() {
        MetricGraph
                .createBuilder()
                .setId("1")
                .addNode(node1)
                .addNode(node2)
                .addArc(Arc.createBuilder()
                                .setId("1")
                                .setLabel("2")
                                .setSource(node1)
                                .setTarget(node2)
                                .createArc()
                )
                .addArc(Arc.createBuilder()
                                .setId("1")
                                .setLabel("3")
                                .setTarget(node1)
                                .setSource(node2)
                                .createArc()
                );
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingPoint() {
        var arc1 = Arc.createBuilder()
                           .setId("1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        var arc2 = Arc.createBuilder()
                           .setId("2")
                           .setTarget(node1)
                           .setSource(node3)
                           .setLabel("2")
                           .setLength(1)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        MetricGraph.createBuilder()
                .setId("3")
                .addNode(node1)
                .addNode(node2)
                .addNode(node3)
                .addArc(arc1)
                .addArc(arc2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodeAndArc() {
        var arc1 = Arc.createBuilder()
                           .setId("Node1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("p1", 1))
                           .createArc();

        MetricGraph.createBuilder()
                .setId("3")
                .addNode(node1)
                .addNode(node2)
                .addArc(arc1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodePoint() {
        var arc1 = Arc.createBuilder()
                           .setId("arc1")
                           .setTarget(node1)
                           .setSource(node2)
                           .setLabel("1")
                           .setLength(10)
                           .addPoint(new MovingPoint("Node1", 1))
                           .createArc();

        MetricGraph.createBuilder()
                .setId("3")
                .addNode(node1)
                .addNode(node2)
                .addArc(arc1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdNodeAndGraph() {
        MetricGraph.createBuilder()
                .setId("Node1")
                .addNode(node1)
                .addNode(node2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdGraphAndNode() {
        MetricGraph.createBuilder()
                .addNode(node1)
                .setId("Node1")
                .addNode(node2);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expected = NullPointerException.class)
    public void withoutId() {
        MetricGraph.createBuilder()
                .addNode(node1)
                .buildGraph();
    }

    @Test
    public void validCreation() {
        var graph = createGraph();

        assertEquals("Graph", graph.getId());
        assertEquals("Graph", graph.getLabel());
        assertEquals("Comment", graph.getComment());

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
        var graph = MetricGraph.createBuilder().setId("1").buildGraph();
        assertEquals("MetricGraph{id='1'}", graph.toString());
    }
}