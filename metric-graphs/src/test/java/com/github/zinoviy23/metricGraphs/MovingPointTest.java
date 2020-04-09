package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MovingPointTest {

  @Test
  public void testEquals() {
    var point1 = new MovingPoint("0", 100);
    var point2 = new MovingPoint("0", -100);
    var point3 = new MovingPoint("1", 100);

    assertThat(point2).isEqualTo(point1);
    assertThat(point3).isNotEqualTo(point1);
  }

  @Test
  public void testToString() {
    var point = new MovingPoint("0", 100);
    assertThat(point.toString()).isEqualTo("MovingPoint{id='0', position=100.0}");
  }
}