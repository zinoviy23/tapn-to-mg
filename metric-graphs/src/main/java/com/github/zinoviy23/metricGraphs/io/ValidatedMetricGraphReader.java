package com.github.zinoviy23.metricGraphs.io;

import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class ValidatedMetricGraphReader {
  public static final String JSON_GRAPH_SCHEMA_JSON = "/json-graph-schema.json";
  private final MetricGraphReader reader;

  public ValidatedMetricGraphReader(@NotNull File file) throws IOException {
    try (var bufferedReader = new BufferedReader(new FileReader(file))) {
      validate(bufferedReader);
    } catch (ValidationException e) {
      throw new IOException(String.join("\n", e.getAllMessages()));
    }
    reader = new MetricGraphReader(file);
  }

  public ValidatedMetricGraphReader(@NotNull String string) throws IOException {
    validate(new StringReader(string));
    reader = new MetricGraphReader(string);
  }

  private static void validate(@NotNull Reader reader) {
    JSONObject jsonSchema = new JSONObject(new JSONTokener(
        ValidatedMetricGraphReader.class.getResourceAsStream(JSON_GRAPH_SCHEMA_JSON)
    ));
    JSONObject jsonGraph = new JSONObject(new JSONTokener(reader));
    var schema = SchemaLoader.load(jsonSchema);
    schema.validate(jsonGraph);
  }

  public @NotNull MetricGraphReader getReader() {
    return reader;
  }
}
