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
import static com.github.zinoviy23.metricGraphs.util.ContainerUtil.second;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class MetricGraphReaderTest {
    @Rule
    public TestName name = new TestName();

    private MetricGraph readGraph() throws IOException {
        var fileName = "/testData/reader/" + name.getMethodName() + ".json";
        MetricGraph graph;
        try (var reader = new MetricGraphReader(getClass().getResourceAsStream(fileName))) {
            graph = reader.read();
        }
        assertThat(graph).isNotNull();
        return graph;
    }

    @Test
    public void validGraph() throws IOException {
        MetricGraph graph = readGraph();

        var nodes = graph.getGraph().vertexSet();
        assertThat(nodes.size()).isEqualTo(3);
        var arcs = graph.getGraph().edgeSet();
        assertThat(arcs.size()).isEqualTo(6);

        assertThat(arcs.stream().filter(checkArc("arc1", "p1", 1.0, 0.5, "Node2", "Node1")).count()).isEqualTo(1);
        assertThat(arcs.stream().filter(checkArc("arc2", "p2", 1.0, 0.7, "Node3", "Node2")).count()).isEqualTo(1);
        assertThat(arcs.stream().filter(checkArc("arc3", "p3", 1.0, 0.2, "Node1", "Node3")).count()).isEqualTo(1);

        assertThat(nodes.stream().filter(node -> "Node1".equals(node.getId())).count()).isEqualTo(1);
        assertThat(nodes.stream().filter(node -> "Node2".equals(node.getId())).count()).isEqualTo(1);
        assertThat(nodes.stream().filter(node -> "Node3".equals(node.getId())).count()).isEqualTo(1);

        assertThat(graph.getComment()).isEqualTo("Comment");
    }

    @Test
    public void edgesBeforeNodes() throws IOException {
        MetricGraph metricGraph = readGraph();

        var arcs = metricGraph.getGraph().edgeSet();
        assertThat(arcs.size()).isEqualTo(2);

        var nodes = metricGraph.getGraph().vertexSet();
        assertThat(nodes.size()).isEqualTo(2);
        assertThat(nodes.stream().filter(node -> "Node1".equals(node.getId())).count()).isEqualTo(1);
        assertThat(nodes.stream().filter(node -> "Node2".equals(node.getId())).count()).isEqualTo(1);

        var arc = first(arcs);
        //noinspection ConstantConditions
        assertThat(arc.getSource().getId()).isEqualTo("Node1");
        assertThat(arc.getTarget().getId()).isEqualTo("Node2");
    }

    @Test
    public void wrongIdInEdgeSource() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertThat(e.getMessage()).isEqualTo("Hasn't any nodes with id=Node3");
        }
    }

    @Test
    public void wrongIdInEdgeTarget() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertThat(e.getMessage()).isEqualTo("Hasn't any nodes with id=Node4");
        }
    }

    @Test
    public void checkPoints() throws IOException {
        MetricGraph graph = readGraph();

        var arc = second(graph.getGraph().edgeSet());
        assertThat(arc).isNotNull();

        assertThat(arc.getPoints().size()).isEqualTo(2);
        assertPoint(arc.getPoints().get(0), "p1", 0.3, "this is point 1");
        assertPoint(arc.getPoints().get(1), "p2", 0.4, "this is point 2");
    }

    @Test
    public void graphWithoutNodes() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertThat("Graph has edges, but hasn't nodes").isEqualTo(e.getMessage());
        }
    }

    @Test
    public void hasNotReversal() throws IOException {
        try {
            readGraph();
        } catch (MetricGraphReadingException e) {
            assertThat("Arc arc1 must have reversal edge!").isEqualTo(e.getMessage());
        }
    }

    private void assertPoint(MovingPoint point, String id, double position, String comment) {
        assertThat(point.getId()).isEqualTo(id);
        assertThat(point.getPosition()).isCloseTo(position, offset(1e-7));
        assertThat(point.getComment()).isEqualTo(comment);
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