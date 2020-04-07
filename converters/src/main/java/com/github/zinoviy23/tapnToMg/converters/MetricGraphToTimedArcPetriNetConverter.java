package com.github.zinoviy23.tapnToMg.converters;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.IndexedFRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.AnnotationNote;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MetricGraphToTimedArcPetriNetConverter implements Converter<MetricGraph, ConvertedTimedArcPetriNet> {
    @Override
    public @NotNull ConvertedTimedArcPetriNet convert(@NotNull MetricGraph graph) {
        var petriNet = new TimedArcPetriNet(getTapnName(graph.getId()));

        var layoutGraph = new SimpleDirectedWeightedGraph<>(null, null);
        var mapping = addPlacesForEachArc(graph, petriNet, layoutGraph);

        for (Arc arc : graph.getGraph().edgeSet()) {
            processArc(graph, arc, petriNet, mapping, layoutGraph);
        }

        var result = new TimedArcPetriNetNetwork();
        result.add(petriNet);

        var layoutMaker = new LayoutMaker(graph.getId(), layoutGraph);

        return new ConvertedTimedArcPetriNet(result, petriNet, layoutMaker.createDataLayer());
    }

    private static @NotNull String getTapnName(@NotNull String id) {
        return "TAPN_" + id;
    }

    private @NotNull Map<Arc, TimedPlace> addPlacesForEachArc(@NotNull MetricGraph graph,
                                                              @NotNull TimedArcPetriNet petriNet,
                                                              @NotNull Graph<Object, Object> layoutGraph) {
        var arcToPlaceMapping = new HashMap<Arc, TimedPlace>();
        for (Arc arc : graph.getGraph().edgeSet()) {
            var placeSourceTarget = new LocalTimedPlace(nameForPlace(arc));
            arcToPlaceMapping.put(arc, placeSourceTarget);
            petriNet.add(placeSourceTarget);
            layoutGraph.addVertex(placeSourceTarget);

            //TODO: tokens cannot be serialized
            arc.getPoints().stream()
                    .map(point -> new TimedToken(placeSourceTarget, BigDecimal.valueOf(point.getPosition())))
                    .forEach(placeSourceTarget::addToken);
        }

        return arcToPlaceMapping;
    }

    private void processArc(@NotNull MetricGraph graph,
                            @NotNull Arc arc,
                            @NotNull TimedArcPetriNet petriNet,
                            @NotNull Map<Arc, TimedPlace> mapping,
                            @NotNull SimpleDirectedWeightedGraph<Object, Object> graphToDraw) {
        var placeSourceTarget = mapping.get(arc);

        //TODO make rational
        var timedTransition = new TimedTransition(nameForTransition(arc), false);
        var timedInputArc = new TimedInputArc(placeSourceTarget, timedTransition,
                new TimeInterval(
                        true, new IntBound(0), new IntBound((int) arc.getLength()), true
                )
        );
        petriNet.add(timedTransition);
        petriNet.add(timedInputArc);

        graphToDraw.addVertex(timedTransition);
        graphToDraw.addEdge(placeSourceTarget, timedTransition, timedInputArc);

        for (Arc outgoingArc : graph.getGraph().outgoingEdgesOf(arc.getTarget())) {
            var destination = mapping.get(outgoingArc);
            var timedOutputArc = new TimedOutputArc(timedTransition, destination);
            petriNet.add(timedOutputArc);
            graphToDraw.addEdge(timedTransition, destination, timedOutputArc);
        }

        var collapsingTransition = new TimedTransition(nameForCollapsingTransition(arc), false);
        var collapsingInputArc = new TimedInputArc(placeSourceTarget, collapsingTransition,
                new TimeInterval(
                        true, new IntBound(0), new IntBound(0), true
                ),
                new IntWeight(2)
        );

        var collapsingOutputArc = new TimedOutputArc(collapsingTransition, placeSourceTarget);

        petriNet.add(collapsingTransition);
        petriNet.add(collapsingInputArc);
        petriNet.add(collapsingOutputArc);

        graphToDraw.addVertex(collapsingTransition);
        graphToDraw.addEdge(placeSourceTarget, collapsingTransition, collapsingInputArc);
        graphToDraw.setEdgeWeight(collapsingInputArc, 2);
        graphToDraw.addEdge(collapsingTransition, placeSourceTarget, collapsingOutputArc);
    }


    private static class LayoutMaker {
        public static final int ITERATIONS = 200;
        private static final double X_OFFSET = 100;
        private static final double Y_OFFSET = 100;
        private static final int NOTE_OFFSET = 50;
        private static final String HEADER = "'%s' - Timed Arc Petri Net automatically generated from Dynamic Metric Graph '%s'";
        private static final String DESCRIPTION_TEXT = "Places p_* - generated from corresponding arc in metric graph\n" +
                "Transitions t_* - generated from corresponding arc in metric graph\n" +
                "Transitions ct_* - generated for simulated collapsing of two points on corresponding arc.";
        private static final int NOTE_WIDTH = 500;
        private static final int NODE_HEIGHT = 200;
        private static final int VERTEX_SIZE = 130;
        private static final int SIZE_OFFSET = 300;

        private final String graphId;
        private final Graph<Object, Object> graph;
        private final LayoutModel2D<Object> layoutModel;

        private final Map<Object, PlaceTransitionObject> visualComponents = new HashMap<>();

        private LayoutMaker(@NotNull String graphId, @NotNull Graph<Object, Object> graph) {
            this.graphId = graphId;
            this.graph = graph;
            layoutModel = createLayout(graph);
        }

        public @NotNull DataLayer createDataLayer() {
            var dataLayer = new DataLayer();

            for (Object vertex : graph.vertexSet()) {
                dataLayer.addPetriNetObject(createPetriNetObject(vertex, layoutModel));
            }

            for (Object edge : graph.edgeSet()) {
                dataLayer.addPetriNetObject(createPetriNetObject(edge, layoutModel));
            }

            AnnotationNote note = createNote();
            dataLayer.addPetriNetObject(note);

            return dataLayer;
        }

        private @NotNull AnnotationNote createNote() {
            var maxX = (int) visualComponents.values().stream()
                                     .mapToDouble(PlaceTransitionObject::getPositionX)
                                     .max()
                                     .orElse(0) + NOTE_OFFSET;
            var maxY = (int) visualComponents.values().stream()
                                     .map(PlaceTransitionObject::getPositionY)
                                     .reduce(MinMax.NEUTRAL, MinMax::reduce, MinMax::combine)
                                     .avg();
            return new AnnotationNote(
                    getTextFromId(graphId),
                    maxX,
                    maxY,
                    NOTE_WIDTH,
                    NODE_HEIGHT,
                    true,
                    true);
        }

        private @NotNull PetriNetObject createPetriNetObject(@NotNull Object object,
                                                             @NotNull LayoutModel2D<Object> model) {
            if (object instanceof TimedPlace) {
                var point2D = model.get(object);
                var timedPlaceComponent = new TimedPlaceComponent(
                        point2D.getX() + X_OFFSET,
                        point2D.getY() + Y_OFFSET,
                        (TimedPlace) object);
                visualComponents.put(object, timedPlaceComponent);
                return timedPlaceComponent;
            }

            if (object instanceof TimedTransition) {
                var point2D = model.get(object);
                var timedTransitionComponent = new TimedTransitionComponent(
                        point2D.getX() + X_OFFSET,
                        point2D.getY() + Y_OFFSET,
                        (TimedTransition) object);
                visualComponents.put(object, timedTransitionComponent);
                return timedTransitionComponent;
            }

            if (object instanceof TimedInputArc) {
                var source = visualComponents.get(((TimedInputArc) object).source());
                var target = visualComponents.get(((TimedInputArc) object).destination());

                var arc = new TimedOutputArcComponent(
                        source.getPositionX() + X_OFFSET,
                        source.getPositionY() + Y_OFFSET,
                        target.getPositionX() + X_OFFSET,
                        target.getPositionY() + Y_OFFSET,
                        source,
                        target,
                        0,
                        "arc",
                        false
                );
                var resultArc = new TimedInputArcComponent(arc);
                resultArc.setUnderlyingArc((TimedInputArc) object);

                return resultArc;
            }

            if (object instanceof TimedOutputArc) {
                var source = visualComponents.get(((TimedOutputArc) object).source());
                var target = visualComponents.get(((TimedOutputArc) object).destination());

                var arc = new TimedOutputArcComponent(
                        source.getPositionX() + X_OFFSET,
                        source.getPositionY() + Y_OFFSET,
                        target.getPositionX() + X_OFFSET,
                        target.getPositionY() + Y_OFFSET,
                        source,
                        target,
                        0,
                        "arc",
                        false
                );
                arc.setUnderlyingArc((TimedOutputArc) object);

                return arc;
            }

            throw new AssertionError("Cannot be here");
        }

        private static @NotNull LayoutModel2D<Object> createLayout(@NotNull Graph<Object, Object> objectGraph) {
            Random random = new Random(11);

            var layoutAlgorithm2D = new IndexedFRLayoutAlgorithm2D<>(
                    ITERATIONS,
                    IndexedFRLayoutAlgorithm2D.DEFAULT_THETA_FACTOR,
                    FRLayoutAlgorithm2D.DEFAULT_NORMALIZATION_FACTOR,
                    random);

            var size = calcSize(objectGraph.vertexSet().size());
            MapLayoutModel2D<Object> layoutModel2D = new MapLayoutModel2D<>(new Box2D(size, size));
            layoutAlgorithm2D.layout(objectGraph, layoutModel2D);

            return layoutModel2D;
        }

        private static int calcSize(int vertexCount) {
            return (int) Math.ceil(Math.sqrt(vertexCount) * VERTEX_SIZE) + SIZE_OFFSET;
        }

        private static String getTextFromId(@NotNull String id) {
            return String.format(HEADER, getTapnName(id), id) + "\n" + DESCRIPTION_TEXT;
        }
    }

    private static @NotNull String nameForPlace(@NotNull Arc arc) {
        return "p_" + arc.getId();
    }

    private static @NotNull String nameForTransition(@NotNull Arc arc) {
        return "t_" + arc.getId();
    }

    private static @NotNull String nameForCollapsingTransition(@NotNull Arc arc) {
        return "ct_" + arc.getId();
    }

    private static class MinMax {
        double min;
        double max;

        static final MinMax NEUTRAL = new MinMax(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

        MinMax(double min, double max) {
            this.min = min;
            this.max = max;
        }

        double avg() {
            return (min + max) / 2;
        }

        static MinMax reduce(MinMax minMax, double d) {
            return new MinMax(Math.min(minMax.min, d), Math.max(minMax.max, d));
        }

        static MinMax combine(MinMax a, MinMax b) {
            return new MinMax(Math.min(a.min, b.min), Math.max(a.max, b.max));
        }
    }
}
