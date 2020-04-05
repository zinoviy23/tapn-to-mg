package com.github.zinoviy23.metricGraphs.io;

import org.everit.json.schema.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatedMetricGraphReaderTest {
    @Rule public TestName testName = new TestName();

    @Test(expected = ValidationException.class)
    public void invalidGraph() throws IOException {
        doTest(false);
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

    private void doTest(boolean read) throws IOException {
        var fileName = "/testData/reader/" + testName.getMethodName() + ".json";

        var file = File.createTempFile("file", ".json");
        try {
            try (var output = new PrintWriter(file);
                 var input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
                input.lines().forEach(output::println);
            }

            var validatedMetricGraphReader = new ValidatedMetricGraphReader(file);
            if (read) {
                assertThat(validatedMetricGraphReader.getReader().read()).isNotNull();
            }
        } finally {
            assertThat(file.delete()).isTrue();
        }
    }
}