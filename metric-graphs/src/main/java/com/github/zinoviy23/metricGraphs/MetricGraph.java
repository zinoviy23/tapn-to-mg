package com.github.zinoviy23.metricGraphs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MetricGraph implements Identity {
    private final String id;
    private final Graph<Node, Arc> graph;

    public MetricGraph(@NotNull String id, @NotNull Graph<Node, Arc> graph) {
        this.id = Objects.requireNonNull(id, "id");
        this.graph = Objects.requireNonNull(graph, "graph");
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull Graph<Node, Arc> getGraph() {
        return graph;
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

    @Contract("_ -> new")
    public static @NotNull MetricGraphBuilder createBuilder(@NotNull String id) {
        return new MetricGraphBuilder(id);
    }

    public static final class MetricGraphBuilder {
        private static final String ALREADY_EXISTS_IN_GRAPH_MESSAGE = " already exists in graph ";
        private static final String ID_ALREADY_EXISTS_MESSAGE = "Id %s already assigned to %s";

        private final Graph<Node, Arc> graph = new SimpleDirectedWeightedGraph<>(null, null);
        private final String id;

        private final Map<MovingPoint, Arc> containingPoints = new HashMap<>();
        private final Map<String, Object> ids = new HashMap<>();

        public MetricGraphBuilder(@NotNull String id) {
            this.id = Objects.requireNonNull(id, "id");
            ids.put(id, "CURRENT GRAPH");
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull MetricGraph buildGraph() {
            return new MetricGraph(id, new AsUnmodifiableGraph<>(graph));
        }

        public @NotNull MetricGraphBuilder addNode(@NotNull Node node) {
            verifyId(node);
            if (!graph.addVertex(Objects.requireNonNull(node, "node"))) {
                failAlreadyExists(node);
            }
            addId(node);
            return this;
        }

        public @NotNull MetricGraphBuilder addArc(@NotNull Arc arc) {
            Objects.requireNonNull(arc, "arc");
            verifyId(arc);
            verifyPoints(arc);
            if (!graph.addEdge(arc.getSource(), arc.getTarget(), arc)) {
                failAlreadyExists(arc);
            }
            graph.setEdgeWeight(arc, arc.getLength());
            addId(arc);
            addPoints(arc);
            return this;
        }

        private void verifyPoints(@NotNull Arc arc) {
            for (var point : arc.getPoints()) {
                verifyId(point);
                if (containingPoints.containsKey(point)) {
                    throw new IllegalArgumentException(point + ALREADY_EXISTS_IN_GRAPH_MESSAGE + id + " in node " + arc);
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
                throw new IllegalArgumentException(
                        String.format(ID_ALREADY_EXISTS_MESSAGE, identity.getId(), ids.get(identity.getId()))
                );
            }
        }

        private void addId(@NotNull Identity identity) {
            ids.put(identity.getId(), identity);
        }

        @Contract("_ -> fail")
        private void failAlreadyExists(Object o) {
            throw new IllegalArgumentException(o + ALREADY_EXISTS_IN_GRAPH_MESSAGE + id);
        }
    }
}
