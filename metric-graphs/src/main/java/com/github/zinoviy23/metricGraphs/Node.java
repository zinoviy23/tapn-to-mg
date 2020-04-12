package com.github.zinoviy23.metricGraphs;

import com.github.zinoviy23.metricGraphs.api.Identity;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Node implements Identity, ObjectWithComment {
  public static final String INFINITY_NODE_LABEL = "Infinity node";
  private final String id;
  private final String label;
  private final String comment;

  private Node(@NotNull String id) {
    this(id, id);
  }

  private Node(@NotNull String id, @Nullable String label) {
    this(id, label, null);
  }

  private Node(@NotNull String id, @Nullable String label, @Nullable String comment) {
    this.id = Objects.requireNonNull(id, "id");
    this.label = Objects.requireNonNull(label, "label");
    this.comment = comment;
  }

  @Contract("_ -> new")
  public static @NotNull Node createNode(@NotNull String id) {
    return new NodeImpl(id);
  }

  @Contract("_, _ -> new")
  public static @NotNull Node createNode(@NotNull String id, @Nullable String label) {
    return new NodeImpl(id, label);
  }

  @Contract("_, _, _ -> new")
  public static @NotNull Node createNode(@NotNull String id, @Nullable String label, @Nullable String comment) {
    return new NodeImpl(id, label, comment);
  }

  @Contract("_ -> new")
  public static @NotNull Node createInfinity(@NotNull String id) {
    return new InfinityNode(id);
  }

  public static @NotNull Node createMultiEdgeHandler(@NotNull String id, @NotNull Node source, @NotNull Node target) {
    return new MultiEdgeHandleNode(id, source, target);
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

  public static boolean isInfinity(@Nullable Node node) {
    return node instanceof InfinityNode;
  }

  static class NodeImpl extends Node {
    private NodeImpl(@NotNull String id) {
      super(id);
    }

    private NodeImpl(@NotNull String id, @Nullable String label) {
      super(id, label);
    }

    private NodeImpl(@NotNull String id, @Nullable String label, @Nullable String comment) {
      super(id, label, comment);
    }
  }

  static class InfinityNode extends Node {
    private InfinityNode(@NotNull String id) {
      super(id, INFINITY_NODE_LABEL, "This node placed on infinity distance from graph.");
    }
  }

  static class MultiEdgeHandleNode extends Node {
    private MultiEdgeHandleNode(@NotNull String id, @NotNull Node source, @NotNull Node target) {
      super(id, id, "Handles multiedges between " + source.getId() + " and " + target.getId());
    }
  }
}
