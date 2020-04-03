package com.github.zinoviy23.metricGraphs.io;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.function.Predicate;

import static com.github.zinoviy23.metricGraphs.util.ContainerUtil.first;
import static org.junit.Assert.*;

public class MetricGraphReaderTest {
    @Rule public TestName name = new TestName();

    private MetricGraph readGraph() throws IOException {
        var fileName = "/testData/reader/" + name.getMethodName() + ".json";
        MetricGraph graph;
        try (var reader = new MetricGraphReader(getClass().getResourceAsStream(fileName))) {
            graph = reader.read();
        }
        assertNotNull(graph);
        return graph;
    }

    @Test
    public void validGraph() throws IOException {
        MetricGraph graph = readGraph();

        var nodes = graph.getGraph().vertexSet();
        assertEquals(3, nodes.size());
        var arcs = graph.getGraph().edgeSet();
        assertEquals(3, arcs.size());

        assertEquals(1, arcs.stream().filter(checkArc("arc1", "p1", 1.0, 0.5, "Node2", "Node1")).count());
        assertEquals(1, arcs.stream().filter(checkArc("arc2", "p2", 1.0, 0.7, "Node3", "Node2")).count());
        assertEquals(1, arcs.stream().filter(checkArc("arc3", "p3", 1.0, 0.2, "Node1", "Node3")).count());

        assertEquals(1, nodes.stream().filter(node -> "Node1".equals(node.getId())).count());
        assertEquals(1, nodes.stream().filter(node -> "Node2".equals(node.getId())).count());
        assertEquals(1, nodes.stream().filter(node -> "Node3".equals(node.getId())).count());

        assertEquals("Comment", graph.getComment());
    }

    @Test
    public void edgesBeforeNodes() throws IOException {
        MetricGraph metricGraph = readGraph();

        var arcs = metricGraph.getGraph().edgeSet();
        assertEquals(1, arcs.size());

        var nodes = metricGraph.getGraph().vertexSet();
        assertEquals(2, nodes.size());
        assertEquals(1, nodes.stream().filter(node -> "Node1".equals(node.getId())).count());
        assertEquals(1, nodes.stream().filter(node -> "Node2".equals(node.getId())).count());

        var arc = first(arcs);
        //noinspection ConstantConditions
        assertEquals("Node2", arc.getSource().getId());
        assertEquals("Node1", arc.getTarget().getId());
    }

    @Test
    public void wrongIdInEdgeSource() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertEquals("Hasn't any nodes with id=Node3", e.getMessage());
        }
    }

    @Test
    public void wrongIdInEdgeTarget() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertEquals("Hasn't any nodes with id=Node4", e.getMessage());
        }
    }

    @Test
    public void checkPoints() throws IOException {
        MetricGraph graph = readGraph();

        var arc = first(graph.getGraph().edgeSet());
        assertNotNull(arc);

        assertEquals(2, arc.getPoints().size());
        assertPoint(arc.getPoints().get(0), "p1", 0.3, "this is point 1");
        assertPoint(arc.getPoints().get(1), "p2", 0.4, "this is point 2");
    }

    @Test
    public void graphWithoutNodes() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertEquals(e.getMessage(), "Graph has edges, but hasn't nodes");
        }
    }

    private void assertPoint(MovingPoint point, String id, double position, String comment) {
        assertEquals(id, point.getId());
        assertEquals(position, point.getPosition(), 1e-7);
        assertEquals(comment, point.getComment());
    }

    @SuppressWarnings("SameParameterValue")
    private Predicate<Arc> checkArc(String id, String pointId, double arcLength, double pointPosition, String source, String target) {
        return arc -> id.equals(arc.getId())
                              && arc.getPoints().size() == 1
                              && pointId.equals(arc.getPoints().get(0).getId())
                              && arc.getPoints().get(0).getPosition() == pointPosition
                              && arc.getLength() == arcLength
                              && source.equals(arc.getSource().getId())
                              && target.equals(arc.getTarget().getId());
    }
}