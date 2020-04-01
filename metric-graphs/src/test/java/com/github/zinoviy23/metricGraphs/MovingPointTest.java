package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static org.junit.Assert.*;

public class MovingPointTest {

    @Test
    public void testEquals() {
        var point1 = new MovingPoint("0", 100);
        var point2 = new MovingPoint("0", -100);
        var point3 = new MovingPoint("1", 100);

        assertEquals(point1, point2);
        assertNotEquals(point1, point3);
    }

    @Test
    public void testToString() {
        var point = new MovingPoint("0", 100);
        assertEquals("MovingPoint{id='0', position=100.0}", point.toString());
    }
}