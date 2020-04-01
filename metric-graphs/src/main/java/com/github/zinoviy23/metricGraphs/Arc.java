package com.github.zinoviy23.metricGraphs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Arc implements Identity {
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

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Arc.ArcBuilder createBuilder(@NotNull String id) {
        return new ArcBuilder(id);
    }

    public final static class ArcBuilder {
        private final String id;
        private String label;
        private Node source;
        private Node target;
        private String comment;
        private double length;
        private List<MovingPoint> points = new ArrayList<>();

        private ArcBuilder(@NotNull String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public @NotNull ArcBuilder setLabel(@NotNull String label) {
            this.label = Objects.requireNonNull(label, "label");
            return this;
        }

        public @NotNull ArcBuilder setSource(@NotNull Node source) {
            this.source = Objects.requireNonNull(source, "source");
            return this;
        }

        public @NotNull ArcBuilder setTarget(@NotNull Node target) {
            this.target = Objects.requireNonNull(target, "target");
            return this;
        }

        public @NotNull ArcBuilder setComment(@Nullable String comment) {
            this.comment = comment;
            return this;
        }

        public @NotNull ArcBuilder setLength(double length) {
            this.length = verifyLength(length);
            return this;
        }

        public @NotNull ArcBuilder setPoints(@NotNull List<MovingPoint> points) {
            for (var point : Objects.requireNonNull(points, "points")) {
                verifyPointId(Objects.requireNonNull(point, "point in points"));
            }
            this.points = points;
            return this;
        }

        public @NotNull ArcBuilder addPoint(@NotNull MovingPoint point) {
            points.add(verifyPointId(Objects.requireNonNull(point, "point")));
            return this;
        }

        @Contract(" -> new")
        public @NotNull Arc createArc() {
            return new Arc(id, label, source, target, comment, length, points);
        }

        private @NotNull MovingPoint verifyPointId(@NotNull MovingPoint movingPoint) {
            if (movingPoint.getId().equals(id)) {
                throw new IllegalArgumentException(String.format(POINT_ID_ALREADY_ASSIGNED_TO_NODE, id));
            }
            return movingPoint;
        }
    }

    private static double verifyLength(double length) {
        if (length <= 0) throw new IllegalArgumentException("length must be greater than 0");
        return length;
    }

    private static @NotNull List<MovingPoint> checkPointsOnArc(double arcLength, @NotNull List<MovingPoint> points) {
        points.stream()
                .filter(point -> point.getPosition() < 0 || point.getPosition() > arcLength)
                .findAny()
                .ifPresent(point -> {
                    throw new IllegalArgumentException(String.format(POINT_MUST_BE_ON_ARC_ERROR, point, arcLength));
                });
        return points;
    }
}
