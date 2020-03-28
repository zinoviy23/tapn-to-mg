package com.github.zinoviy23.metricGraphs.impl;

import com.github.zinoviy23.metricGraphs.Edge;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public class EdgeImpl implements Edge {
    private final Node input;
    private final Node output;
    private final List<Point> points;

    public EdgeImpl(Node input, Node output) {
        this.input = input;
        this.output = output;
        points = new ArrayList<>();
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public @NotNull Node getInput() {
        return input;
    }

    @Override
    public @NotNull Node getOutput() {
        return output;
    }

    @Override
    public @NotNull List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }
}
