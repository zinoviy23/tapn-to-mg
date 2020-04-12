package com.github.zinoviy23.metricGraphs.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ValidatedMetricGraphJsonReaderTest {
  @Rule
  public TestName testName = new TestName();

  @Test
  public void invalidGraph() {
    assertThatThrownBy(() ->
      doTest(false)
    ).isInstanceOf(IOException.class)
        .hasMessage("#/graph/edges/0: required key [id] not found\n" +
            "#/graph/edges/1: required key [id] not found\n" +
            "#/graph/edges/2: required key [target] not found");
  }

  @Test
  public void validGraph() throws IOException {
    doTest(true);
  }

  @Test
  public void edgesBeforeNodes() throws IOException {
    doTest(true);
  }

  @Test
  public void wrongIdInEdgeSource() throws IOException {
    doTest(false);
  }

  @Test
  public void wrongIdInEdgeTarget() throws IOException {
    doTest(false);
  }

  @Test
  public void checkPoints() throws IOException {
    doTest(true);
  }

  @Test
  public void graphWithoutNodes() throws IOException {
    doTest(false);
  }

  @Test
  public void hasNotReversal() throws IOException {
    doTest(false);
  }

  @Test
  public void graphWithLead() throws IOException {
    doTest(true);
  }

  private void doTest(boolean read) throws IOException {
    var fileName = "/testData/reader/" + testName.getMethodName() + ".json";

    var file = File.createTempFile("file", ".json");
    try {
      try (var output = new PrintWriter(file);
           var input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
        input.lines().forEach(output::println);
      }

      var validatedMetricGraphReader = new ValidatedMetricGraphJsonReader(file);
      if (read) {
        assertThat(validatedMetricGraphReader.getReader().read()).isNotNull();
      }
    } finally {
      assertThat(file.delete()).isTrue();
    }
  }
}