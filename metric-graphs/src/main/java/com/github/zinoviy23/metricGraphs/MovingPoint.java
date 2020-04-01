package com.github.zinoviy23.metricGraphs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class MovingPoint implements Identity {
    private final String id;
    private final double position;
    private final String comment;

    public MovingPoint(@NotNull String id, double position) {
        this(id, position, null);
    }

    public MovingPoint(@NotNull String id, double position, @Nullable String comment) {
        this.id = Objects.requireNonNull(id, "id");
        this.position = position;
        this.comment = comment;
    }

    public @NotNull String getId() {
        return id;
    }

    public double getPosition() {
        return position;
    }

    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovingPoint movingPoint = (MovingPoint) o;
        return id.equals(movingPoint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MovingPoint{" +
                       "id='" + id + '\'' +
                       ", position=" + position +
                       '}';
    }
}
