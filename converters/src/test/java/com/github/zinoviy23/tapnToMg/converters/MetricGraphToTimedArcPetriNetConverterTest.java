package com.github.zinoviy23.tapnToMg.converters;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class MetricGraphToTimedArcPetriNetConverterTest {
    private MetricGraph graphFromPaper() {
        var node1 = new Node("n1");
        var node2 = new Node("n2");
        var node3 = new Node("n3");
        var node4 = new Node("n4");
        var node5 = new Node("n5");

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
                            .addArc(Arc.createBuilder()
                                            .setId("a2")
                                            .setSource(node5)
                                            .setTarget(node2)
                                            .setLength(10)
                                            .addPoint(new MovingPoint("p2", 3))
                                            .createArc()
                            )
                            .addArc(Arc.createBuilder()
                                            .setId("a3")
                                            .setSource(node5)
                                            .setTarget(node3)
                                            .setLength(10)
                                            .addPoint(new MovingPoint("p3", 3))
                                            .createArc()
                            )
                            .addArc(Arc.createBuilder()
                                            .setId("a4")
                                            .setSource(node5)
                                            .setTarget(node4)
                                            .setLength(10)
                                            .addPoint(new MovingPoint("p4", 3))
                                            .createArc()
                            )
                            .buildGraph();
    }


    @Test
    public void convert() throws ParserConfigurationException, TransformerException, IOException {
        var graph = graphFromPaper();

        var network = new MetricGraphToTimedArcPetriNetConverter().convert(graph);
        assertThat(network.getNetwork().getTAPNByName("tmp net")).isNotNull();

        TimedArcPetriNetNetworkWriter writer = new TimedArcPetriNetNetworkWriter(network.getNetwork(), List.of(network.getTemplate()), Collections.emptyList(), network.getNetwork().constants());
        File file = new File("myFile.tapn");
        writer.savePNML(file);
    }
}