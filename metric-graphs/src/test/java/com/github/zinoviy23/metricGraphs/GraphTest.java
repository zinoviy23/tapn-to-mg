package com.github.zinoviy23.metricGraphs;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void simpleGraph() {
        Graph<Node, Edge> simpleGraph = new SimpleGraph<>(null, null, false);
    }
}