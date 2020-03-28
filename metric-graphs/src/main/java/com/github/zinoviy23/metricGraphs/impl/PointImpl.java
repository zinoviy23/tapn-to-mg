package com.github.zinoviy23.metricGraphs.impl;

import com.github.zinoviy23.metricGraphs.Point;

public class PointImpl implements Point {
    private final double position;

    public PointImpl(double position) {
        this.position = position;
    }

    @Override
    public double getPosition() {
        return 0;
    }
}
