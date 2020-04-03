package com.github.zinoviy23.metricGraphs.io;

public class MetricGraphReadingException extends RuntimeException {
    public MetricGraphReadingException(String message) {
        super(message);
    }

    public MetricGraphReadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
