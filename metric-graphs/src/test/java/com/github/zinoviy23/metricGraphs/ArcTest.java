package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ArcTest {
    private final Node node1 = new Node("Node1", "Node1");
    private final Node node2 = new Node("Node2", "Node2");

    @Test
    public void validCreation() {
        var p1 = new MovingPoint("p1", 0.1);
        var arc = Arc.createBuilder("0")
                          .setLabel("Arc 0")
                          .setLength(10)
                          .setSource(node1)
                          .setTarget(node2)
                          .addPoint(p1)
                          .createArc();

        assertEquals("0", arc.getId());
        assertEquals("Arc 0", arc.getLabel());
        assertEquals(10.0, arc.getLength(), 1e-7);
        assertEquals(node1, arc.getSource());
        assertEquals(node2, arc.getTarget());
        assertEquals(1, arc.getPoints().size());
        assertTrue(arc.getPoints().contains(p1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongPointGreater() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setSource(node1)
                .setTarget(node2)
                .addPoint(new MovingPoint("point0", 11))
                .createArc();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongPointLesser() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setSource(node1)
                .setTarget(node2)
                .addPoint(new MovingPoint("point0", -1))
                .createArc();
    }

    @Test(expected = NullPointerException.class)
    public void withoutSource() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setTarget(node2)
                .createArc();
    }

    @Test(expected = NullPointerException.class)
    public void withoutTarget() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setSource(node1)
                .createArc();
    }

    @Test(expected = NullPointerException.class)
    public void withoutLabel() {
        Arc.createBuilder("0")
                .setLength(10)
                .setSource(node1)
                .setTarget(node2)
                .createArc();
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdWithPoint() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setSource(node1)
                .setTarget(node2)
                .addPoint(new MovingPoint("0", 2))
                .createArc();
    }

    @Test(expected = IllegalArgumentException.class)
    public void sameIdWithPoints() {
        Arc.createBuilder("0")
                .setLabel("Arc 0")
                .setLength(10)
                .setSource(node1)
                .setTarget(node2)
                .setPoints(List.of(new MovingPoint("0", 2)))
                .createArc();
    }

    @Test
    public void equals() {
        var arc1 = Arc.createBuilder("0")
                          .setLabel("Arc 0")
                          .setLength(10)
                          .setSource(node1)
                          .setTarget(node2)
                          .createArc();

        var arc2 = Arc.createBuilder("0")
                          .setLabel("Arc 1")
                          .setLength(11)
                          .setSource(node2)
                          .setTarget(node1)
                          .createArc();

        var arc3 = Arc.createBuilder("1")
                           .setLabel("Arc 0")
                           .setLength(10)
                           .setSource(node1)
                           .setTarget(node2)
                           .createArc();

        assertEquals(arc1, arc2);
        assertNotEquals(arc1, arc3);
    }

    @Test
    public void toStringTest() {
        var arc = Arc.createBuilder("0")
                          .setLabel("Arc 0")
                          .setLength(10)
                          .setSource(node1)
                          .setTarget(node2)
                          .createArc();
        assertEquals("Arc{id='0', label='Arc 0', length=10.0}", arc.toString());
    }
}