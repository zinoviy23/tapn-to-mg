package com.github.zinoviy23.metricGraphs;

import com.github.zinoviy23.metricGraphs.api.Identity;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Arc implements Identity, ObjectWithComment {
  private static final String POINT_MUST_BE_ON_ARC_ERROR = "Point %s isn't on arc. [0, %f] is not contains it";
  private static final String POINT_ID_ALREADY_ASSIGNED_TO_NODE = "Points id %s already assigned to node";

  private final String id;
  private final String label;
  private final Node source;
  private final Node target;
  private final String comment;
  private final double length;
  private final List<MovingPoint> points;

  private Arc(@NotNull String id,
              @Nullable String label,
              @NotNull Node source,
              @NotNull Node target,
              @Nullable String comment,
              double length,
              @NotNull List<MovingPoint> points) {
    this.id = Objects.requireNonNull(id, "id");
    this.label = Objects.requireNonNullElse(label, id);
    this.source = Objects.requireNonNull(source, "source");
    this.target = Objects.requireNonNull(target, "target");
    this.comment = comment;
    this.length = length;
    this.points = List.copyOf(checkPointsOnArc(length, Objects.requireNonNull(points, "points")));
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull Arc.ArcBuilder createBuilder() {
    return new ArcBuilder();
  }

  private static double verifyLength(double length) {
    if (length <= 0) throw new MetricGraphStructureException("length must be greater than 0");
    return length;
  }

  private static @NotNull List<MovingPoint> checkPointsOnArc(double arcLength, @NotNull List<MovingPoint> points) {
    points.stream()
        .filter(point -> point.getPosition() < 0 || point.getPosition() > arcLength)
        .findAny()
        .ifPresent(point -> {
          throw new MetricGraphStructureException(String.format(POINT_MUST_BE_ON_ARC_ERROR, point, arcLength));
        });
    return points;
  }

  @Override
  public @NotNull String getId() {
    return id;
  }

  public @NotNull String getLabel() {
    return label;
  }

  public @NotNull Node getSource() {
    return source;
  }

  public @NotNull Node getTarget() {
    return target;
  }

  @Override
  public @Nullable String getComment() {
    return comment;
  }

  public double getLength() {
    return length;
  }

  public @NotNull List<MovingPoint> getPoints() {
    return points;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Arc arc = (Arc) o;
    return id.equals(arc.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Arc{" +
        "id='" + id + '\'' +
        ", label='" + label + '\'' +
        ", length=" + length +
        '}';
  }

  public static final class ArcBuilder {
    private String id;
    private String label;
    private Node source;
    private Node target;
    private String comment;
    private double length;
    private List<MovingPoint> points = new ArrayList<>();

    private ArcBuilder() {
    }

    public @NotNull ArcBuilder setSource(@NotNull Node source) {
      this.source = Objects.requireNonNull(source, "source");
      return this;
    }

    public @NotNull ArcBuilder setTarget(@NotNull Node target) {
      this.target = Objects.requireNonNull(target, "target");
      return this;
    }

    public @NotNull ArcBuilder setLength(double length) {
      this.length = verifyLength(length);
      return this;
    }

    public @NotNull ArcBuilder addPoint(@NotNull MovingPoint point) {
      points.add(verifyPointId(Objects.requireNonNull(point, "point"), id));
      return this;
    }

    @Contract(" -> new")
    public @NotNull Arc createArc() {
      return new Arc(id, label, source, target, comment, length, points);
    }

    private @NotNull MovingPoint verifyPointId(@NotNull MovingPoint movingPoint, @NotNull String id) {
      if (movingPoint.getId().equals(id)) {
        throw new MetricGraphStructureException(String.format(POINT_ID_ALREADY_ASSIGNED_TO_NODE, id));
      }
      return movingPoint;
    }

    public @NotNull String getId() {
      return id;
    }

    public @NotNull ArcBuilder setId(@NotNull String id) {
      Objects.requireNonNull(id, "id");
      for (var point : points) {
        verifyPointId(point, id);
      }
      this.id = id;
      return this;
    }

    public @NotNull List<MovingPoint> getPoints() {
      return Collections.unmodifiableList(points);
    }

    public @NotNull ArcBuilder setPoints(@NotNull List<MovingPoint> points) {
      for (var point : Objects.requireNonNull(points, "points")) {
        verifyPointId(Objects.requireNonNull(point, "point in points"), id);
      }
      this.points = points;
      return this;
    }

    public @Nullable String getLabel() {
      return label;
    }

    public @NotNull ArcBuilder setLabel(@Nullable String label) {
      this.label = label;
      return this;
    }

    public @Nullable String getComment() {
      return comment;
    }

    public @NotNull ArcBuilder setComment(@Nullable String comment) {
      this.comment = comment;
      return this;
    }
  }
}
