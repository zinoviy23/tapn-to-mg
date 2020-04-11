package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import java.util.List;

import static com.github.zinoviy23.metricGraphs.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetricGraphTest {
  @Test
  public void addExistingNode() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("1")
            .addNode(Node.createNode("2", "aaa"))
            .addNode(Node.createNode("2", "bbb"))
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id 2 already assigned to Node{id='2', label='aaa'}");
  }

  @Test
  public void addExistingArc() {
    assertThatThrownBy(() ->
        MetricGraph
            .createBuilder()
            .setId("1")
            .addNode(node1)
            .addNode(node2)
            .addArc(Arc.createBuilder()
                .setId("2")
                .setLabel("2")
                .setLength(1)
                .setSource(node1)
                .setTarget(node2)
                .createArc()
            )
            .withReversal("tmp")
            .addArc(Arc.createBuilder()
                .setId("2")
                .setLabel("3")
                .setLength(2)
                .setTarget(node1)
                .setSource(node2)
                .createArc()
            ).withReversal("tmp2")
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id 2 already assigned to Raw Arc{id='2', label='2', length=1.0}");
  }

  @Test
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

    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("3")
            .addNode(node1)
            .addNode(node2)
            .addNode(node3)
            .addArc(arc1)
            .withReversal("rev1")
            .addArc(arc2)
            .withReversal("rev2")
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id p1 already assigned to MovingPoint{id='p1', position=1.0}");
  }

  @Test
  public void sameIdNodeAndArc() {
    var arc1 = Arc.createBuilder()
        .setId("Node1")
        .setTarget(node1)
        .setSource(node2)
        .setLabel("1")
        .setLength(10)
        .addPoint(new MovingPoint("p1", 1))
        .createArc();

    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("3")
            .addNode(node1)
            .addNode(node2)
            .addArc(arc1)
            .withReversal("rev3")
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id Node1 already assigned to Node{id='Node1', label='Node1'}");
  }

  @Test
  public void sameIdNodePoint() {
    var arc1 = Arc.createBuilder()
        .setId("arc1")
        .setTarget(node1)
        .setSource(node2)
        .setLabel("1")
        .setLength(10)
        .addPoint(new MovingPoint("Node1", 1))
        .createArc();

    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("3")
            .addNode(node1)
            .addNode(node2)
            .addArc(arc1)
            .withReversal("rev3")
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id Node1 already assigned to Node{id='Node1', label='Node1'}");
  }

  @Test
  public void sameIdNodeAndGraph() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("Node1")
            .addNode(node1)
            .addNode(node2)
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id Node1 already assigned to CURRENT GRAPH");
  }

  @Test
  public void sameIdGraphAndNode() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .addNode(node1)
            .setId("Node1")
            .addNode(node2)
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id Node1 already assigned to Node{id='Node1', label='Node1'}");
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void withoutId() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .addNode(node1)
            .buildGraph()
    )
        .isInstanceOf(NullPointerException.class)
        .hasMessage("id");
  }

  @Test
  public void wrongReversal() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .addNode(node1)
            .addNode(node2)
            .addArc(arc1)
            .withReversal(Arc.createBuilder()
                .setId("arc 2")
                .setLength(2)
                .setSource(node1)
                .setTarget(node2)
                .createArc())
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Reversal edge Raw Arc{id='arc 2', label='arc 2', length=2.0} of Raw Arc{id='arc1', label='my arc', length=1.0} must have length 1.000000");
  }

  @Test
  public void sameIdWithReversal() {
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .addNode(node1)
            .addNode(node2)
            .addArc(arc1).withReversal("arc1")
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id arc1 already assigned to Raw Arc{id='arc1', label='my arc', length=1.0}");
  }

  @Test
  public void validCreation() {
    var graph = createGraph();

    assertThat(graph.getId()).isEqualTo("Graph");
    assertThat(graph.getLabel()).isEqualTo("Graph");
    assertThat(graph.getComment()).isEqualTo("Comment");

    var arcs = graph.getGraph().edgeSet();
    assertThat(arcs.size()).isEqualTo(6);
    assertThat(arcs.contains(arc1)).isTrue();
    assertThat(arcs.contains(arc2)).isTrue();
    assertThat(arcs.contains(arc3)).isTrue();

    var nodes = graph.getGraph().vertexSet();
    assertThat(nodes.size()).isEqualTo(3);
    assertThat(nodes.contains(node1)).isTrue();
    assertThat(nodes.contains(node2)).isTrue();
    assertThat(nodes.contains(node3)).isTrue();

    assertThat(graph.getArc("arc1")).isNotNull();
    assertThat(graph.getArc("arc2")).isNotNull();
    assertThat(graph.getArc("arc3")).isNotNull();
    assertThat(graph.getArc("arc4")).isNull();

    assertThat(graph.getNode("Node1")).isNotNull();
    assertThat(graph.getNode("Node2")).isNotNull();
    assertThat(graph.getNode("Node3")).isNotNull();
    assertThat(graph.getNode("node4")).isNull();
  }

  @Test
  public void reversalTestNonExisting() {
    var graph = createGraph();
    var arc = Arc.createBuilder()
        .setSource(Node.createNode("1000"))
        .setTarget(Node.createNode("10000"))
        .setLength(1000)
        .setId("10000000")
        .createArc();
    assertThat(graph.getReversal(arc)).isNull();
  }

  @Test
  public void wrongNodesReversal() {
    assertThatThrownBy(() -> MetricGraph.createBuilder()
        .addNode(node1)
        .addNode(node2)
        .addArc(arc1)
        .withReversal(arc2))
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Reversal of Raw Arc{id='arc1', label='my arc', length=1.0} must have wrong source=Node{id='Node3', label='Node3'} and target=Node{id='Node2', label='Node2'}");
  }

  @Test
  public void reversalTestExisting() {
    var metricGraph = MetricGraph.createBuilder()
        .setId("graph")
        .addNode(node1)
        .addNode(node2)
        .addArc(arc1)
        .withReversal(Arc.createBuilder()
            .setId("arc 2")
            .setLength(1)
            .setSource(node1)
            .setTarget(node2)
            .createArc())
        .buildGraph();

    assertThat(metricGraph.getReversal(arc1))
        .isNotNull()
        .hasFieldOrPropertyWithValue("source", node1)
        .hasFieldOrPropertyWithValue("target", node2);
  }

  @Test
  public void addPointsSuccess() {
    var p1 = new MovingPoint("p1", 3);
    var metricGraph = MetricGraph.createBuilder()
        .setId("graph")
        .addNode(node1)
        .addNode(node2)
        .addArc(Arc.createBuilder()
            .setId("arc")
            .setSource(node1)
            .setTarget(node2)
            .setLength(10)
        )
        .withReversal("rev_arc")
        .addPoints("arc", List.of(p1))
        .buildGraph();

    assertThat(metricGraph.getGraph().getEdge(node1, node2).getPoints())
        .contains(p1);
  }

  @Test
  public void addPointsToNonexistingArc() {
    var p1 = new MovingPoint("p1", 3);
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("graph")
            .addNode(node1)
            .addNode(node2)
            .addArc(Arc.createBuilder()
                .setId("arc3")
                .setSource(node1)
                .setTarget(node2)
                .setLength(10)
            )
            .withReversal("rev_arc")
            .addPoints("arc", List.of(p1))
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Graph is not contain arc with id=arc");
  }

  @Test
  public void addExistingPointToArc() {
    var p1 = new MovingPoint("p1", 3);
    assertThatThrownBy(() ->
        MetricGraph.createBuilder()
            .setId("graph")
            .addNode(node1)
            .addNode(node2)
            .addArc(Arc.createBuilder()
                .setId("arc")
                .setSource(node1)
                .setTarget(node2)
                .setLength(10)
            )
            .withReversal("rev_arc", p1)
            .addPoints("arc", List.of(p1))
    )
        .isInstanceOf(MetricGraphStructureException.class)
        .hasMessage("Id p1 already assigned to MovingPoint{id='p1', position=3.0}");
  }

  @Test
  public void containsEdge() {
    var builder = MetricGraph.createBuilder()
        .setId("1")
        .addNode(node1)
        .addNode(node2)
        .addNode(node3)
        .addArc(arc1).withReversal("rev1")
        .addArc(arc2).withReversal("rev2");

    assertThat(builder.containsEdge(node1, node2)).isTrue();
    assertThat(builder.containsEdge(node2, node3)).isTrue();
    assertThat(builder.containsEdge(node1, node3)).isFalse();
  }

  @Test
  public void toStringTest() {
    var graph = MetricGraph.createBuilder().setId("1").buildGraph();
    assertThat(graph.toString()).isEqualTo("MetricGraph{id='1'}");
  }
}