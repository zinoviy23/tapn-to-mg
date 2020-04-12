package com.github.zinoviy23.metricGraphs.io;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.*;
import java.util.stream.Collectors;

import static com.github.zinoviy23.metricGraphs.TestData.createGraph;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricGraphJsonWriterTest {
  @Test
  public void validateByScheme() throws IOException {
    var graph = createGraph();

    StringWriter sw = new StringWriter();
    try (var mgw = new MetricGraphJsonWriter(sw, true)) {
      mgw.write(graph);
    }

    var json = sw.toString();
    JSONObject jsonSchema = new JSONObject(new JSONTokener(
        MetricGraphJsonWriterTest.class.getResourceAsStream("/json-graph-schema.json")
    ));
    JSONObject jsonGraph = new JSONObject(json);
    var schema = SchemaLoader.load(jsonSchema);
    schema.validate(jsonGraph);
  }

  @Test
  public void writeToFile() throws IOException {
    var file = File.createTempFile("file", ".json");
    try {
      var graph = createGraph();
      try (var writer = new MetricGraphJsonWriter(new FileOutputStream(file), true)) {
        writer.write(graph);
      }

      var fileContent = new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining("\n"));
      var expectedContent = new BufferedReader(
          new InputStreamReader(getClass().getResourceAsStream("/testData/writer/validGraph.json"))
      ).lines().collect(Collectors.joining("\n"));

      assertThat(fileContent).isEqualTo(expectedContent);
    } finally {
      assertThat(file.delete()).isTrue();
    }
  }
}