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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MetricGraph implements Identity, ObjectWithComment {
    private final String id;
    private final String label;
    private final String comment;
    private final Graph<Node, Arc> graph;

    private MetricGraph(@NotNull String id,
                        @Nullable String label,
                        @Nullable String comment,
                        @NotNull Graph<Node, Arc> graph) {
        this.id = Objects.requireNonNull(id, "id");
        this.comment = comment;
        this.label = Objects.requireNonNullElse(label, id);
        this.graph = Objects.requireNonNull(graph, "graph");
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

    @Contract(" -> new")
    public static @NotNull MetricGraphBuilder createBuilder() {
        return new MetricGraphBuilder();
    }

    public static final class MetricGraphBuilder {
        private static final String ALREADY_EXISTS_IN_GRAPH_MESSAGE = " already exists in graph ";
        private static final String ID_ALREADY_EXISTS_MESSAGE = "Id %s already assigned to %s";

        private final Graph<Node, Arc> graph = new SimpleDirectedWeightedGraph<>(null, null);

        private final Map<MovingPoint, Arc> containingPoints = new HashMap<>();
        private final Map<String, Object> ids = new HashMap<>();

        private String id;
        private String label;
        private String comment;

        private MetricGraphBuilder() {
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull MetricGraph buildGraph() {
            return new MetricGraph(id, label, comment, new AsUnmodifiableGraph<>(graph));
        }

        public @NotNull MetricGraphBuilder addNode(@NotNull Node node) {
            verifyId(node);
            if (!graph.addVertex(Objects.requireNonNull(node, "node"))) {
                failAlreadyExists(node);
            }
            addId(node);
            return this;
        }

        public @NotNull ArcWithReversalBuilder addArc(@NotNull Arc arc) {
            Objects.requireNonNull(arc, "arc");
            return new ArcWithReversalBuilder(arc);
        }

        public class ArcWithReversalBuilder {
            private final Arc currentArc;

            private String comment;
            private String label;

            private ArcWithReversalBuilder(@NotNull Arc currentArc) {
                this.currentArc = currentArc;
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
                return withReversal(Arc.createBuilder()
                                            .setId(id)
                                            .setLength(currentArc.getLength())
                                            .setSource(currentArc.getTarget())
                                            .setTarget(currentArc.getSource())
                                            .setLabel(label)
                                            .setComment(comment)
                                            .setPoints(points)
                                            .createArc()
                );
            }

            public @NotNull MetricGraphBuilder withReversal(@NotNull Arc reversalArc) {
                Objects.requireNonNull(reversalArc, "reversalArc");

                if (!reversalArc.getTarget().equals(currentArc.getSource()) ||
                            !reversalArc.getSource().equals(currentArc.getTarget())) {
                    throw new MetricGraphStructureException(String.format("Reversal of %s must have wrong source=%s and target=%s",
                            currentArc,
                            reversalArc.getSource(),
                            reversalArc.getTarget()));
                }

                verifyId(currentArc);
                verifyPoints(currentArc);
                verifyId(reversalArc);
                verifyPoints(reversalArc);

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
                addPoints(currentArc);
                addId(reversalArc);
                addPoints(reversalArc);

                return MetricGraphBuilder.this;
            }
        }

        public MetricGraphBuilder setLabel(@Nullable String label) {
            this.label = label;
            return this;
        }

        public MetricGraphBuilder setComment(@Nullable String comment) {
            this.comment = comment;
            return this;
        }

        public MetricGraphBuilder setId(@NotNull String id) {
            verifyId(() -> id);
            this.id = id;
            ids.put(id, "CURRENT GRAPH");
            return this;
        }

        private void verifyPoints(@NotNull Arc arc) {
            for (var point : arc.getPoints()) {
                verifyId(point);
                if (containingPoints.containsKey(point)) {
                    throw new MetricGraphStructureException(point + ALREADY_EXISTS_IN_GRAPH_MESSAGE + id + " in node " + arc);
                }
            }
        }

        private void addPoints(@NotNull Arc arc) {
            for (var point : arc.getPoints()) {
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

        private void verifyReversal(@NotNull Arc arc) {
            var edge = graph.getEdge(arc.getTarget(), arc.getSource());
            if (edge == null) return;

            if (!DoubleUtil.equals(edge.getLength(), arc.getLength())) {
                throw new MetricGraphStructureException(
                        String.format("Reversal edge %s of %s must have length %f", edge, arc, arc.getLength())
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
    }
}
