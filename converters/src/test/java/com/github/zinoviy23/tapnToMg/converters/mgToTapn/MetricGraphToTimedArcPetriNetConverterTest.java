package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.tapnToMg.converters.ConvertersFactory;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.*;
import org.assertj.core.api.Condition;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

public class MetricGraphToTimedArcPetriNetConverterTest {
  private MetricGraph graphFromPaper() {
    var node1 = Node.createNode("n1");
    var node2 = Node.createNode("n2");
    var node3 = Node.createNode("n3");
    var node4 = Node.createNode("n4");
    var node5 = Node.createNode("n5");

    return MetricGraph.createBuilder()
        .setId("graph")
        .addNode(node1)
        .addNode(node2)
        .addNode(node3)
        .addNode(node4)
        .addNode(node5)
        .addArc(Arc.createBuilder()
            .setId("a1")
            .setSource(node1)
            .setTarget(node5)
            .setLength(10)
            .addPoint(new MovingPoint("p1", 3))
            .createArc()
        )
        .withReversal("rev_a1")
        .addArc(Arc.createBuilder()
            .setId("a2")
            .setSource(node5)
            .setTarget(node2)
            .setLength(10)
            .addPoint(new MovingPoint("p2", 3))
            .createArc()
        )
        .withReversal("rev_a2")
        .addArc(Arc.createBuilder()
            .setId("a3")
            .setSource(node5)
            .setTarget(node3)
            .setLength(10)
            .addPoint(new MovingPoint("p3", 3))
            .createArc()
        )
        .withReversal("rev_a3")
        .addArc(Arc.createBuilder()
            .setId("a4")
            .setSource(node5)
            .setTarget(node4)
            .setLength(10)
            .addPoint(new MovingPoint("p4", 3))
            .createArc()
        )
        .withReversal("rev_a4")
        .buildGraph();
  }


  @Test
  public void convertFromPaperIsWriting() throws ParserConfigurationException, TransformerException, IOException {
    var graph = graphFromPaper();

    var network = new MetricGraphToTimedArcPetriNetConverter().convert(graph);
    assertThat(network.getNetwork().getTAPNByName("TAPN_graph")).isNotNull();

    TimedArcPetriNetNetworkWriter writer = new TimedArcPetriNetNetworkWriter(network.getNetwork(), List.of(network.getTemplate()), Collections.emptyList(), network.getNetwork().constants());
    File file = File.createTempFile("myFile", ".tapn");
    try {
      writer.savePNML(file);
    } finally {
      assertThat(file.delete()).isTrue();
    }
  }

  @Test
  public void convertFromPaperUsingFileConverter() {
    var graph = graphFromPaper();
    var converter = new MetricGraphToTimedArcPetriNetConverter()
        .andThen(ConvertersFactory.createTimedArcPetriNetToFileConverter());
    var apply = converter.apply(graph);
    assertThat(apply.exists());
  }

  @Test
  public void simpleGraph() {
    var node1 = Node.createNode("n1");
    var node2 = Node.createNode("n2");

    var graph = MetricGraph.createBuilder()
        .setId("graph1")
        .addNode(node1)
        .addNode(node2)
        .addArc(Arc.createBuilder()
            .setLength(10)
            .addPoint(new MovingPoint("p1", 9))
            .addPoint(new MovingPoint("p2", 5))
            .setId("a1")
            .setSource(node1)
            .setTarget(node2)
            .createArc()
        ).withReversal("rev_a1", new MovingPoint("p3", 1), new MovingPoint("p4", 3), new MovingPoint("p5", 6))
        .buildGraph();

    var network = new MetricGraphToTimedArcPetriNetConverter().convert(graph);
    var tapnGraph1 = network.getNetwork().getTAPNByName("TAPN_graph1");
    assertThat(tapnGraph1).isNotNull();

    var pa1 = tapnGraph1.getPlaceByName("p_a1");
    assertThat(pa1)
        .isNotNull()
        .is(new Condition<>(p -> p.numberOfTokens() == 2, "Tokens"));
    var pReva1 = tapnGraph1.getPlaceByName("p_rev_a1");
    assertThat(pReva1)
        .isNotNull()
        .is(new Condition<>(p -> p.numberOfTokens() == 3, "Tokens"));

    var ta1 = tapnGraph1.getTransitionByName("t_a1");
    assertThat(ta1).isNotNull();
    var cta1 = tapnGraph1.getTransitionByName("ct_a1");
    assertThat(cta1).isNotNull();
    var tReva1 = tapnGraph1.getTransitionByName("t_rev_a1");
    assertThat(tReva1).isNotNull();
    var ctReva1 = tapnGraph1.getTransitionByName("ct_rev_a1");
    assertThat(ctReva1).isNotNull();

    var timeInterval = new TimeInterval(true, new IntBound(10), new IntBound(10), true);
    var pa1ta1 = tapnGraph1.getInputArcFromPlaceToTransition(pa1, ta1);
    assertThat(pa1ta1)
        .isNotNull()
        .hasFieldOrPropertyWithValue("interval", timeInterval);
    var pReva1tReva1 = tapnGraph1.getInputArcFromPlaceToTransition(pReva1, tReva1);
    assertThat(pReva1tReva1)
        .isNotNull()
        .hasFieldOrPropertyWithValue("interval", timeInterval);

    var zeroInterval = new TimeInterval(true, new IntBound(0), new IntBound(0), true);
    var pa1cta1 = tapnGraph1.getInputArcFromPlaceToTransition(pa1, cta1);
    assertThat(pa1cta1)
        .isNotNull()
        .hasFieldOrPropertyWithValue("interval", zeroInterval)
        .is(new Condition<>(arc -> arc.getWeight().value() == 2, "Weight"));
    var pReva1ctReva1 = tapnGraph1.getInputArcFromPlaceToTransition(pReva1, ctReva1);
    assertThat(pReva1ctReva1)
        .isNotNull()
        .hasFieldOrPropertyWithValue("interval", zeroInterval)
        .is(new Condition<>(arc -> arc.getWeight().value() == 2, "Weight"));

    assertThat(tapnGraph1.getOutputArcFromTransitionAndPlace(cta1, pa1)).isNotNull();
    assertThat(tapnGraph1.getOutputArcFromTransitionAndPlace(ctReva1, pReva1)).isNotNull();

    assertThat(tapnGraph1.getOutputArcFromTransitionAndPlace(ta1, pReva1)).isNotNull();
    assertThat(tapnGraph1.getOutputArcFromTransitionAndPlace(tReva1, pa1)).isNotNull();
  }
}