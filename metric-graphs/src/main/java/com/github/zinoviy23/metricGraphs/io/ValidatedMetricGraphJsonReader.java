package com.github.zinoviy23.metricGraphs.io;

import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class ValidatedMetricGraphJsonReader {
  public static final String JSON_GRAPH_SCHEMA_JSON = "/json-graph-schema.json";
  private final MetricGraphJsonReader reader;

  public ValidatedMetricGraphJsonReader(@NotNull File file) throws IOException {
    try (var bufferedReader = new BufferedReader(new FileReader(file))) {
      validate(bufferedReader);
    } catch (ValidationException e) {
      throw new IOException(String.join("\n", e.getAllMessages()));
    }
    reader = new MetricGraphJsonReader(file);
  }

  public ValidatedMetricGraphJsonReader(@NotNull String string) throws IOException {
    validate(new StringReader(string));
    reader = new MetricGraphJsonReader(string);
  }

  private static void validate(@NotNull Reader reader) {
    JSONObject jsonSchema = new JSONObject(new JSONTokener(
        ValidatedMetricGraphJsonReader.class.getResourceAsStream(JSON_GRAPH_SCHEMA_JSON)
    ));
    JSONObject jsonGraph = new JSONObject(new JSONTokener(reader));
    var schema = SchemaLoader.load(jsonSchema);
    schema.validate(jsonGraph);
  }

  public @NotNull MetricGraphJsonReader getReader() {
    return reader;
  }
}
