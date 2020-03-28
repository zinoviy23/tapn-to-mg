package com.github.zinoviy23.metricGraphs.impl;

import com.github.zinoviy23.metricGraphs.Node;

public class NodeImpl implements Node {
    private final String label;

    public NodeImpl(String label) {
        this.label = label;
    }

    @Override
    public @org.jetbrains.annotations.NotNull String getLabel() {
        return label;
    }
}
