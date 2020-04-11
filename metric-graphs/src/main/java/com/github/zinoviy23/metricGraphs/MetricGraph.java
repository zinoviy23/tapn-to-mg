package com.github.zinoviy23.metricGraphs;

import com.github.zinoviy23.metricGraphs.api.Identity;
import com.github.zinoviy23.metricGraphs.api.ObjectWithComment;
import com.github.zinoviy23.metricGraphs.util.DoubleUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public final class MetricGraph implements Identity, ObjectWithComment {
  private final String id;
  private final String label;
  private final String comment;
  private final Graph<Node, Arc> graph;
  private final Map<String, Identity> ids;

  private MetricGraph(@NotNull String id,
                      @Nullable String label,
                      @Nullable String comment,
                      @NotNull Graph<Node, Arc> graph,
                      @NotNull Map<String, Identity> ids) {
    this.id = Objects.requireNonNull(id, "id");
    this.comment = comment;
    this.label = Objects.requireNonNullElse(label, id);
    this.graph = Objects.requireNonNull(graph, "graph");
    this.ids = new HashMap<>(ids);
  }

  @Contract(" -> new")
  public static @NotNull MetricGraphBuilder createBuilder() {
    return new MetricGraphBuilder();
  }

  @Override
  public @NotNull String getId() {
    return id;
  }

  public @NotNull Graph<Node, Arc> getGraph() {
    return graph;
  }

  public @NotNull String getLabel() {
    return label;
  }

  @Override
  public @Nullable String getComment() {
    return comment;
  }

  public @Nullable Arc getReversal(@NotNull Arc arc) {
    Objects.requireNonNull(arc, "arc");

    return graph.getEdge(arc.getTarget(), arc.getSource());
  }

  public @Nullable Node getNode(@NotNull String id) {
    Objects.requireNonNull(id);

    var identity = ids.get(id);
    return identity instanceof Node ? ((Node) identity) : null;
  }

  public @Nullable Arc getArc(@NotNull String id) {
    Objects.requireNonNull(id);

    var identity = ids.get(id);
    return identity instanceof Arc ? ((Arc) identity) : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MetricGraph that = (MetricGraph) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "MetricGraph{" +
        "id='" + id + '\'' +
        '}';
  }

  public static final class MetricGraphBuilder {
    private static final String ALREADY_EXISTS_IN_GRAPH_MESSAGE = " already exists in graph ";
    private static final String ID_ALREADY_EXISTS_MESSAGE = "Id %s already assigned to %s";

    private final Graph<Node, Arc.ArcBuilder> graph = new SimpleDirectedWeightedGraph<>(null, null);

    private final Map<MovingPoint, Arc.ArcBuilder> containingPoints = new HashMap<>();
    private final Map<String, Identity> ids = new HashMap<>();

    private String id;
    private String label;
    private String comment;

    private MetricGraphBuilder() {
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull MetricGraph buildGraph() {
      var resultGraph = new SimpleDirectedWeightedGraph<Node, Arc>(null, null);
      for (Node node : graph.vertexSet()) {
        resultGraph.addVertex(node);
      }
      Map<String, Arc> arcIds = new HashMap<>();
      graph.edgeSet().stream()
          .map(Arc.ArcBuilder::createArc)
          .forEach(arc -> {
            resultGraph.addEdge(arc.getSource(), arc.getTarget(), arc);
            resultGraph.setEdgeWeight(arc, arc.getLength());
            arcIds.put(arc.getId(), arc);
          });
      var newIds = new HashMap<>(ids);
      newIds.putAll(arcIds);
      return new MetricGraph(id, label, comment, new AsUnmodifiableGraph<>(resultGraph), newIds);
    }

    public @NotNull MetricGraphBuilder addNode(@NotNull Node node) {
      verifyId(node);
      if (!graph.addVertex(Objects.requireNonNull(node, "node"))) {
        failAlreadyExists(node);
      }
      addId(node);
      return this;
    }

    public @NotNull ArcWithReversalBuilder addArc(@NotNull Arc.ArcBuilder arcBuilder) {
      Objects.requireNonNull(arcBuilder, "arcBuilder");
      return new ArcWithReversalBuilder(arcBuilder.copy());
    }

    public @NotNull ArcWithReversalBuilder addArc(@NotNull Arc arc) {
      Objects.requireNonNull(arc, "arc");
      return new ArcWithReversalBuilder(arc.toBuilder());
    }

    public @NotNull MetricGraphBuilder setLabel(@Nullable String label) {
      this.label = label;
      return this;
    }

    public @NotNull MetricGraphBuilder setComment(@Nullable String comment) {
      this.comment = comment;
      return this;
    }

    public @NotNull MetricGraphBuilder setId(@NotNull String id) {
      Identity identity = new Identity() {
        @Override
        public @NotNull String getId() {
          return id;
        }

        @Override
        public String toString() {
          return "CURRENT GRAPH";
        }
      };
      verifyId(identity);

      this.id = id;
      ids.put(id, identity);
      return this;
    }

    public @NotNull MetricGraphBuilder addPoints(@NotNull String arcId, @NotNull List<MovingPoint> points) {
      var arc = ids.get(arcId);
      if (!(arc instanceof Arc.ArcBuilder)) {
        throw new MetricGraphStructureException("Graph is not contain arc with id=" + arcId);
      }
      verifyPoints(((Arc.ArcBuilder) arc), points);
      //noinspection ConstantConditions verified while adding arc
      Arc.checkPointsOnArc(((Arc.ArcBuilder) arc).getLength(), points);
      for (MovingPoint point : points) {
        ((Arc.ArcBuilder) arc).addPoint(point);
      }
      addPoints(((Arc.ArcBuilder) arc), points);
      return this;
    }

    public boolean containsEdge(Node source, Node target) {
      return graph.containsEdge(source, target);
    }

    private void verifyPoints(@NotNull Arc.ArcBuilder arc, @NotNull List<MovingPoint> points) {
      for (var point : points) {
        verifyId(point);
        if (containingPoints.containsKey(point)) {
          throw new MetricGraphStructureException(point + ALREADY_EXISTS_IN_GRAPH_MESSAGE + id + " in node " + arc);
        }
      }
    }

    private void addPoints(@NotNull Arc.ArcBuilder arc, @NotNull List<MovingPoint> points) {
      for (var point : points) {
        addId(point);
        containingPoints.put(point, arc);
      }
    }

    private void verifyId(@NotNull Identity identity) {
      if (ids.containsKey(identity.getId())) {
        throw new MetricGraphStructureException(
            String.format(ID_ALREADY_EXISTS_MESSAGE, identity.getId(), ids.get(identity.getId()))
        );
      }
    }

    private void addId(@NotNull Identity identity) {
      ids.put(identity.getId(), identity);
    }

    @Contract("_ -> fail")
    private void failAlreadyExists(Object o) {
      throw new MetricGraphStructureException(o + ALREADY_EXISTS_IN_GRAPH_MESSAGE + id);
    }

    public class ArcWithReversalBuilder {
      private final Arc.ArcBuilder currentArc;

      private String comment;
      private String label;

      private ArcWithReversalBuilder(@NotNull Arc.ArcBuilder currentArc) {
        this.currentArc = currentArc;
        verifyArcBuilder(currentArc, "currentArc");
      }

      public @NotNull ArcWithReversalBuilder setReversalComment(@Nullable String comment) {
        this.comment = comment;
        return this;
      }

      public @NotNull ArcWithReversalBuilder setReversalLabel(@Nullable String label) {
        this.label = label;
        return this;
      }

      public @NotNull MetricGraphBuilder withReversal(@NotNull String id, MovingPoint @NotNull ... points) {
        return withReversal(id, List.of(points));
      }

      public @NotNull MetricGraphBuilder withReversal(@NotNull String id, @NotNull List<MovingPoint> points) {
        //noinspection ConstantConditions verified in class ctor
        return internalWithReversal(Arc.createBuilder()
            .setId(id)
            .setLength(currentArc.getLength())
            .setSource(currentArc.getTarget())
            .setTarget(currentArc.getSource())
            .setLabel(label)
            .setComment(comment)
            .setPoints(points)
        );
      }

      public @NotNull MetricGraphBuilder withReversal(@NotNull Arc reversalArc) {
        return withReversal(reversalArc.toBuilder());
      }

      public @NotNull MetricGraphBuilder withReversal(@NotNull Arc.ArcBuilder reversalArc) {
        return internalWithReversal(reversalArc.copy());
      }

      private @NotNull MetricGraphBuilder internalWithReversal(@NotNull Arc.ArcBuilder reversalArc) {
        verifyArcBuilder(reversalArc, "reversalArc");

        //noinspection ConstantConditions verified in verifyArcBuilder and ctor
        if (!reversalArc.getTarget().equals(currentArc.getSource()) ||
            !reversalArc.getSource().equals(currentArc.getTarget())) {
          throw new MetricGraphStructureException(String.format("Reversal of %s must have wrong source=%s and target=%s",
              currentArc,
              reversalArc.getSource(),
              reversalArc.getTarget()
          ));
        }

        verifyId(currentArc);
        verifyPoints(currentArc, currentArc.getPoints());
        verifyId(reversalArc);
        verifyPoints(reversalArc, reversalArc.getPoints());

        //noinspection ConstantConditions verified in verifyArcBuilder and ctor
        if (!DoubleUtil.equals(currentArc.getLength(), reversalArc.getLength())) {
          throw new MetricGraphStructureException(
              String.format("Reversal edge %s of %s must have length %f", reversalArc, currentArc, currentArc.getLength())
          );
        }

        if (currentArc.getId().equals(reversalArc.getId())) {
          throw new MetricGraphStructureException(String.format(ID_ALREADY_EXISTS_MESSAGE, currentArc.getId(), currentArc));
        }

        if (graph.containsEdge(currentArc) || graph.containsEdge(currentArc.getSource(), currentArc.getTarget())) {
          failAlreadyExists(currentArc);
        }
        if (graph.containsEdge(reversalArc) || graph.containsEdge(reversalArc.getSource(), reversalArc.getTarget())) {
          failAlreadyExists(reversalArc);
        }
        graph.addEdge(currentArc.getSource(), currentArc.getTarget(), currentArc);
        graph.setEdgeWeight(currentArc, currentArc.getLength());
        graph.addEdge(reversalArc.getSource(), reversalArc.getTarget(), reversalArc);
        graph.setEdgeWeight(reversalArc, reversalArc.getLength());

        addId(currentArc);
        addPoints(currentArc, currentArc.getPoints());
        addId(reversalArc);
        addPoints(reversalArc, reversalArc.getPoints());

        return MetricGraphBuilder.this;
      }

      private void verifyArcBuilder(Arc.@NotNull ArcBuilder arcBuilder, final String fieldName) {
        Objects.requireNonNull(arcBuilder, fieldName);
        Objects.requireNonNull(arcBuilder.getSource(), fieldName + " must have source");
        Objects.requireNonNull(arcBuilder.getTarget(), fieldName + " must have target");
        Objects.requireNonNull(arcBuilder.getLength(), fieldName + " must have length");
      }
    }
  }
}
