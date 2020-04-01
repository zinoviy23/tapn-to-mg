package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void testEquals() {
        var node1 = new Node("0", "aaaa", "bbbbb");
        var node2 = new Node("0", "bbbbbb", "aaaaa");
        var node3 = new Node("1", "aaaa", "bbbbb");

        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
    }

    @Test
    public void testToString() {
        var node = new Node("0", "aaaa", "bbbbb");
        assertEquals("Node{id='0', label='aaaa'}", node.toString());
    }
}