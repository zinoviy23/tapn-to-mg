package com.github.zinoviy23.metricGraphs.io;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public interface MetricGraphWriter<T extends Throwable> extends AutoCloseable, Closeable {
  void write(@NotNull MetricGraph graph) throws T;
}
