package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeTest {

    @Test
    public void testEquals() {
        var node1 = new Node("0", "aaaa", "bbbbb");
        var node2 = new Node("0", "bbbbbb", "aaaaa");
        var node3 = new Node("1", "aaaa", "bbbbb");

        assertThat(node2).isEqualTo(node1);
        assertThat(node3).isNotEqualTo(node1);
    }

    @Test
    public void testToString() {
        var node = new Node("0", "aaaa", "bbbbb");
        assertThat(node.toString()).isEqualTo("Node{id='0', label='aaaa'}");
    }
}