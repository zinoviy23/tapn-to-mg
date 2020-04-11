package com.github.zinoviy23.tapnToMg.converters.tapnToMg;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.util.ContainerUtil;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.util.FormatException;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TimedArcPetriNetToMetricGraphConverterTest {
  @Rule public TestName testName = new TestName();

  private TimedArcPetriNet readNet() throws FormatException {
    var loader = new TapnXmlLoader();
    var name = String.format("/%s.tapn", testName.getMethodName());
    var loadedModel = loader.load(getClass().getResourceAsStream(name));
    var network = loadedModel.network();

    assertThat(network.allTemplates()).hasSize(1);
    return ContainerUtil.first(network.allTemplates());
  }

  private MetricGraph getGraph() throws FormatException {
    var tapn = readNet();
    var convert = new TimedArcPetriNetToMetricGraphConverter();
    return convert.convert(tapn);
  }

  @Test
  public void fig2() throws FormatException {
    MetricGraph graph = getGraph();

    assertThat(graph).hasFieldOrPropertyWithValue("id", "mg_TAPN1");
    assertThat(graph.getGraph().vertexSet())
        .hasSize(6)
        .contains(Node.createNode("n_T1"), Node.createNode("n_T2"), Node.createNode("n_T3"))
        .contains(Node.createInfinity("inf_P1_T1"), Node.createInfinity("inf_T1_P4"), Node.createInfinity("inf_T3_P4"));
    assertThat(graph.getGraph().edgeSet().stream().map(Arc::getId))
        .hasSize(12)
        .contains("a_T1_P2_T2", "a_T1_P3_T3", "a_T2_P3_T3", "l_P1_T1", "l_T3_P4", "l_T1_P4")
        .contains("rev_a_T1_P2_T2", "rev_a_T1_P3_T3", "rev_a_T2_P3_T3", "rev_l_P1_T1", "rev_l_T3_P4", "rev_l_T1_P4");
    assertThat(graph.getArc("l_P1_T1"))
        .isNotNull()
        .hasFieldOrPropertyWithValue("points", List.of(new MovingPoint("t_1", 10.0)));
    //noinspection ConstantConditions
    assertThat(ContainerUtil.first(graph.getArc("l_P1_T1").getPoints()))
        .isNotNull()
        .hasFieldOrPropertyWithValue("position", 10.0);
    assertThat(graph.getArc("a_T1_P2_T2"))
        .hasFieldOrPropertyWithValue("points", List.of(new MovingPoint("t_0", 0.0)));
    //noinspection ConstantConditions
    assertThat(ContainerUtil.first(graph.getArc("a_T1_P2_T2").getPoints()))
        .hasFieldOrPropertyWithValue("position", 0.0);
  }

  @Test
  public void selfLoop() throws FormatException {
    MetricGraph graph = getGraph();

    assertThat(graph).hasFieldOrPropertyWithValue("id", "mg_TAPN1");
    var n = Node.createNode("n_T0");
    assertThat(graph.getGraph().vertexSet())
        .contains(n)
        .contains(Node.createMultiEdgeHandler("br_n_a_T0_P0_T0_1", n, n))
        .contains(Node.createMultiEdgeHandler("br_n_a_T0_P0_T0_2", n, n));
    assertThat(graph.getGraph().edgeSet().size()).isEqualTo(6);
    assertThat(graph.getArc("br_a_T0_P0_T0_part1"))
        .isNotNull()
        .has(new Condition<>(arc -> arc.getPoints().size() == 2, "has 2 points"));
  }

  @Test
  public void multiEdge() throws FormatException {
    MetricGraph graph = getGraph();

    assertThat(graph).hasFieldOrPropertyWithValue("id", "mg_TAPN1");
    var n0 = Node.createNode("n_T0");
    var n1 = Node.createNode("n_T1");
    assertThat(graph.getGraph().vertexSet())
        .contains(n0, n1, Node.createMultiEdgeHandler("br_n_a_T0_P1_T1", n0, n1));
    assertThat(graph.getGraph().edgeSet().stream().map(Arc::getId))
        .contains("a_T0_P0_T1")
        .contains("br_a_T0_P1_T1_part1", "br_a_T0_P1_T1_part2");
  }

  @Test
  public void wrongArcIntervalBounds() {
    assertThatThrownBy(this::getGraph)
        .isInstanceOf(TimedArcPetriNetToMetricGraphConversionException.class)
        .hasMessage("Time interval [0,inf) in From P0 to T0 with interval [0,inf) must be closed!");
  }

  @Test
  public void wrongArcIntervalLength() {
    assertThatThrownBy(this::getGraph)
        .isInstanceOf(TimedArcPetriNetToMetricGraphConversionException.class)
        .hasMessage("Time interval [10,20] in From P0 to T0 with interval [10,20] must be zero length!");
  }

  @Test
  public void urgentTransition() {
    assertThatThrownBy(this::getGraph)
        .isInstanceOf(TimedArcPetriNetToMetricGraphConversionException.class)
        .hasMessage("Transition T0 must be non-urgent");
  }
}