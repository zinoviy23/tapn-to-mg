package com.github.zinoviy23.metricGraphs;

import com.github.zinoviy23.metricGraphs.api.Identity;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Node implements Identity, ObjectWithComment {
  public static final Node INFINITY = new Node(
      "infinity", "infinity", "This is an infinity node. Edges with this node has only one vertex"
  );
  private final String id;
  private final String label;
  private final String comment;

  public Node(@NotNull String id) {
    this(id, id);
  }

  public Node(@NotNull String id, @Nullable String label) {
    this(id, label, null);
  }

  public Node(@NotNull String id, @Nullable String label, @Nullable String comment) {
    this.id = Objects.requireNonNull(id, "id");
    this.label = Objects.requireNonNull(label, "label");
    this.comment = comment;
  }

  @Override
  public @NotNull String getId() {
    return id;
  }

  public @NotNull String getLabel() {
    return label;
  }

  @Override
  public @Nullable String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Node node = (Node) o;
    return id.equals(node.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Node{" +
        "id='" + id + '\'' +
        ", label='" + label + '\'' +
        '}';
  }
}
