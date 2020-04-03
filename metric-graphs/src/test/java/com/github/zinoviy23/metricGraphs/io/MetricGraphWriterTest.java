package com.github.zinoviy23.metricGraphs.io;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.*;
import java.util.stream.Collectors;

import static com.github.zinoviy23.metricGraphs.TestData.createGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricGraphWriterTest {
    @Test
    public void validateByScheme() throws IOException {
        var graph = createGraph();

        StringWriter sw = new StringWriter();
        try (var mgw = new MetricGraphWriter(sw, true)) {
            mgw.write(graph);
        }

        var json = sw.toString();
        JSONObject jsonSchema = new JSONObject(new JSONTokener(
                MetricGraphWriterTest.class.getResourceAsStream("/json-graph-schema.json")
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
            try (var writer = new MetricGraphWriter(new FileOutputStream(file), true)) {
                writer.write(graph);
            }

            var fileContent = new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining("\n"));
            var expectedContent = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/testData/writer/validGraph.json"))
            ).lines().collect(Collectors.joining("\n"));

            assertEquals(expectedContent, fileContent);
        } finally {
            assertTrue(file.delete());
        }
    }
}