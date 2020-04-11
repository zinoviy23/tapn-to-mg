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

import static com.github.zinoviy23.metricGraphs.util.ObjectUtil.*;

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
  private final boolean distanceToTarget;

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

    if (Node.isInfinity(source) && Node.isInfinity(target)) {
      throw new MetricGraphStructureException(String.format("%s and %s both are infinity nodes", source, target));
    }

    distanceToTarget = Node.isInfinity(source);
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull ArcBuilder createBuilder() {
    return new ArcBuilderImpl();
  }

  private static double verifyLength(double length) {
    if (length <= 0) throw new MetricGraphStructureException("length must be greater than 0");
    return length;
  }

  static @NotNull List<MovingPoint> checkPointsOnArc(double arcLength, @NotNull List<MovingPoint> points) {
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

  /**
   * Direction for points movement. <br>
   * If this is true, it means that points positions are relative to arc's target, not source. <br>
   * If this is false, which is regular situation, it means, that points positions are relative to arc's source.
   * @return true, only if this arc is lead, with infinity source, otherwise false
   */
  public boolean isDistanceToTarget() {
    return distanceToTarget;
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

  /**
   * Too support lazy creation of graphs and allow adding built arcs to graph builder
   * @return wrapper builder, that "builds" this arc
   */
  @Contract(" -> new")
  @NotNull ArcBuilder toBuilder() {
    return new ExactArcBuilder(this);
  }

  /**
   * Class, because currently java do not support sealed interfaces
   */
  public abstract static class ArcBuilder implements Identity {
    private ArcBuilder() {
    }

    public abstract @NotNull ArcBuilder setSource(@NotNull Node source);

    public abstract @Nullable Node getSource();

    public abstract @NotNull ArcBuilder setTarget(@NotNull Node target);

    public abstract @Nullable Node getTarget();

    public abstract @NotNull ArcBuilder setLength(double length);

    public abstract @Nullable Double getLength();

    public abstract @NotNull ArcBuilder addPoint(@NotNull MovingPoint point);

    public abstract @NotNull Arc createArc();

    public abstract @NotNull String getId();

    public abstract @NotNull ArcBuilder setId(@NotNull String id);

    public abstract @NotNull List<MovingPoint> getPoints();

    public abstract @NotNull ArcBuilder setPoints(@NotNull List<MovingPoint> points);

    public abstract @Nullable String getLabel();

    public abstract @NotNull ArcBuilder setLabel(@Nullable String label);

    public abstract @Nullable String getComment();

    public abstract @NotNull ArcBuilder setComment(@Nullable String comment);

    public abstract @NotNull ArcBuilder copy();

    @Override
    public final boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ArcBuilder)) return false;
      ArcBuilder that = (ArcBuilder) o;
      return Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
      return Objects.hash(getId());
    }

    protected static @NotNull String makeString(@Nullable String id, @Nullable String label, @Nullable Double length) {
      var beginning = "Raw Arc{";
      StringBuilder sb = new StringBuilder(beginning);
      if (id != null) {
        sb.append("id='").append(id).append("', ");
      }
      if (label != null) {
        sb.append("label='").append(label).append("', ");
      }
      if (length != null) {
        sb.append("length=").append(length).append(", ");
      }
      if (sb.length() != beginning.length()) {
        sb.delete(sb.length() - 2, sb.length());
      }

      return sb.append("}").toString();
    }
  }

  private static final class ArcBuilderImpl extends ArcBuilder {
    private String id;
    private String label;
    private Node source;
    private Node target;
    private String comment;
    private Double length;
    private List<MovingPoint> points = new ArrayList<>();

    private ArcBuilderImpl() {
    }

    public @NotNull ArcBuilderImpl setSource(@NotNull Node source) {
      this.source = Objects.requireNonNull(source, "source");
      return this;
    }

    @Override
    public @Nullable Node getSource() {
      return source;
    }

    public @NotNull ArcBuilderImpl setTarget(@NotNull Node target) {
      this.target = Objects.requireNonNull(target, "target");
      return this;
    }

    @Override
    public @Nullable Node getTarget() {
      return target;
    }

    public @NotNull ArcBuilderImpl setLength(double length) {
      this.length = verifyLength(length);
      return this;
    }

    @Override
    public @Nullable Double getLength() {
      return length;
    }

    public @NotNull ArcBuilderImpl addPoint(@NotNull MovingPoint point) {
      points.add(verifyPointId(Objects.requireNonNull(point, "point"), id));
      return this;
    }

    @Contract(" -> new")
    public @NotNull Arc createArc() {
      return new Arc(id, label, source, target, comment, Objects.requireNonNull(length, "length"), points);
    }

    private @NotNull MovingPoint verifyPointId(@NotNull MovingPoint movingPoint, @NotNull String id) {
      if (movingPoint.getId().equals(id)) {
        throw new MetricGraphStructureException(String.format(POINT_ID_ALREADY_ASSIGNED_TO_NODE, id));
      }
      return movingPoint;
    }

    public @NotNull String getId() {
      if (id != null) {
        return id;
      }

      throw new IllegalStateException("ArcBuilder has not id");
    }

    public @NotNull ArcBuilderImpl setId(@NotNull String id) {
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

    public @NotNull ArcBuilderImpl setPoints(@NotNull List<MovingPoint> points) {
      for (var point : Objects.requireNonNull(points, "points")) {
        verifyPointId(Objects.requireNonNull(point, "point in points"), id);
      }
      this.points = new ArrayList<>(points);
      return this;
    }

    public @Nullable String getLabel() {
      return label;
    }

    public @NotNull ArcBuilderImpl setLabel(@Nullable String label) {
      this.label = label;
      return this;
    }

    public @Nullable String getComment() {
      return comment;
    }

    public @NotNull ArcBuilderImpl setComment(@Nullable String comment) {
      this.comment = comment;
      return this;
    }

    @Override
    public @NotNull ArcBuilder copy() {
      var builder = createBuilder();
      doIfNotNull(id, builder::setId);
      doIfNotNull(label, builder::setLabel);
      doIfNotNull(source, builder::setSource);
      doIfNotNull(target, builder::setTarget);
      doIfNotNull(length, builder::setLength);
      builder.setComment(comment);
      builder.setPoints(points);
      return builder;
    }

    @Override
    public String toString() {
      return makeString(id, label, length);
    }
  }

  private static final class ExactArcBuilder extends ArcBuilder {
    private final Arc arc;

    private ExactArcBuilder(Arc arc) {
      this.arc = arc;
    }

    @Override
    public @NotNull ArcBuilder setSource(@NotNull Node source) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Node getSource() {
      return arc.getSource();
    }

    @Override
    public @NotNull ArcBuilder setTarget(@NotNull Node target) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Node getTarget() {
      return arc.getTarget();
    }

    @Override
    public @NotNull ArcBuilder setLength(double length) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Double getLength() {
      return arc.getLength();
    }

    @Override
    public @NotNull ArcBuilder addPoint(@NotNull MovingPoint point) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Arc createArc() {
      return arc;
    }

    @Override
    public @NotNull String getId() {
      return arc.getId();
    }

    @Override
    public @NotNull ArcBuilder setId(@NotNull String id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<MovingPoint> getPoints() {
      return arc.getPoints();
    }

    @Override
    public @NotNull ArcBuilder setPoints(@NotNull List<MovingPoint> points) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getLabel() {
      return arc.getLabel();
    }

    @Override
    public @NotNull ArcBuilder setLabel(@Nullable String label) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable String getComment() {
      return arc.getComment();
    }

    @Override
    public @NotNull ArcBuilder setComment(@Nullable String comment) {
      throw new UnsupportedOperationException();
    }

    /**
     * Do not create any new object, because it is immutable, so it can be shared
     * @return {@code this} instance
     */
    @Override
    public @NotNull ArcBuilder copy() {
      return this;
    }

    @Override
    public String toString() {
      return makeString(arc.getId(), arc.getLabel(), arc.getLength());
    }
  }
}
