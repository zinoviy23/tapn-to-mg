package com.github.zinoviy23.metricGraphs;

import org.junit.Test;

import java.util.List;

import static com.github.zinoviy23.metricGraphs.TestData.node1;
import static com.github.zinoviy23.metricGraphs.TestData.node2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class ArcTest {
  @Test
  public void validCreation() {
    var p1 = new MovingPoint("p1", 0.1);
    var arc = Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .addPoint(p1)
        .createArc();

    assertThat(arc.getId()).isEqualTo("0");
    assertThat(arc.getLabel()).isEqualTo("Arc 0");
    assertThat(arc.getLength()).isCloseTo(10.0, offset(1e-7));
    assertThat(arc.getSource()).isEqualTo(node1);
    assertThat(arc.getTarget()).isEqualTo(node2);
    assertThat(arc.getPoints().size()).isEqualTo(1);
    assertThat(arc.getPoints().contains(p1)).isTrue();
  }

  @Test(expected = MetricGraphStructureException.class)
  public void wrongPointGreater() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .addPoint(new MovingPoint("point0", 11))
        .createArc();
  }

  @Test(expected = MetricGraphStructureException.class)
  public void wrongPointLesser() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .addPoint(new MovingPoint("point0", -1))
        .createArc();
  }

  @Test(expected = NullPointerException.class)
  public void withoutSource() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setTarget(node2)
        .createArc();
  }

  @Test(expected = NullPointerException.class)
  public void withoutTarget() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .createArc();
  }

  @Test
  public void withoutLabel() {
    var arc = Arc.createBuilder()
        .setId("0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .createArc();
    assertThat(arc.getLabel()).isEqualTo("0");
  }

  @Test(expected = MetricGraphStructureException.class)
  public void sameIdWithPoint() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .addPoint(new MovingPoint("0", 2))
        .createArc();
  }

  @Test(expected = MetricGraphStructureException.class)
  public void sameAfterIdWithPoint() {
    Arc.createBuilder()
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .addPoint(new MovingPoint("0", 2))
        .setId("0")
        .createArc();
  }

  @Test(expected = MetricGraphStructureException.class)
  public void sameIdWithPoints() {
    Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .setPoints(List.of(new MovingPoint("0", 2)))
        .createArc();
  }

  @Test
  public void equals() {
    var arc1 = Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .createArc();

    var arc2 = Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 1")
        .setLength(11)
        .setSource(node2)
        .setTarget(node1)
        .createArc();

    var arc3 = Arc.createBuilder()
        .setId("1")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .createArc();

    assertThat(arc2).isEqualTo(arc1);
    assertThat(arc3).isNotEqualTo(arc1);
  }

  @Test
  public void toStringTest() {
    var arc = Arc.createBuilder()
        .setId("0")
        .setLabel("Arc 0")
        .setLength(10)
        .setSource(node1)
        .setTarget(node2)
        .createArc();
    assertThat(arc.toString()).isEqualTo("Arc{id='0', label='Arc 0', length=10.0}");
  }
}