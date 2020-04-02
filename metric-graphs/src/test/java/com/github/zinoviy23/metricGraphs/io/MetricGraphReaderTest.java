package com.github.zinoviy23.metricGraphs.io;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MetricGraphReaderTest {
    @Test
    public void simpleTest() throws IOException {
        try (var reader = new MetricGraphReader(getClass().getResourceAsStream("/testData/writer/validGraph.json"))) {
            reader.read();
        }
    }
}