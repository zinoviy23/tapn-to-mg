package com.github.zinoviy23.metricGraphs.io.graphml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static com.github.zinoviy23.metricGraphs.TestData.createGraph;

public class MetricGraphYedWriterTest {
  private static final Logger LOG = LogManager.getLogger(MetricGraphYedWriterTest.class);

  @Test
  public void simpleWrite() throws IOException {
    var graph = createGraph();

    var stringWriter = new StringWriter();
    try (var writer = new MetricGraphYedWriter(stringWriter, true)) {
      writer.write(graph);
    }
    LOG.info(stringWriter.toString());
  }
}