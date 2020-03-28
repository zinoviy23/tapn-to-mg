package com.github.zinoviy23.metricGraphs;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Edge {
    double getLength();

    @NotNull Node getInput();

    @NotNull Node getOutput();

    @NotNull List<@NotNull Point> getPoints();
}
