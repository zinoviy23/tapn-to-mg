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
import org.jgrapht.traverse.DepthFirstIterator;
import pipe.dataLayer.DataLayer;
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
        var petriNet = new TimedArcPetriNet("tmp net"); // TODO



        var layoutGraph = new SimpleDirectedWeightedGraph<>(null, null);
        var mapping = addPlacesForEachArc(graph, petriNet, layoutGraph);

        for (Arc arc : graph.getGraph().edgeSet()) {
            processArc(graph, arc, petriNet, mapping, layoutGraph);
        }

        var result = new TimedArcPetriNetNetwork();
        result.add(petriNet);

        var layoutMaker = new LayoutMaker(layoutGraph);

        return new ConvertedTimedArcPetriNet(result, petriNet, layoutMaker.createDataLayer());
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
        private final Graph<Object, Object> graph;
        private final LayoutModel2D<Object> layoutModel;

        private final Map<Object, PlaceTransitionObject> visualComponents = new HashMap<>();

        private LayoutMaker(@NotNull Graph<Object, Object> graph) {
            this.graph = graph;
            layoutModel = createLayout(graph);
        }

        public  @NotNull DataLayer createDataLayer() {
            var dataLayer = new DataLayer();

            for (Object vertex : graph.vertexSet()) {
                dataLayer.addPetriNetObject(createPetriNetObject(vertex, layoutModel));
            }

            for (Object edge : graph.edgeSet()) {
                dataLayer.addPetriNetObject(createPetriNetObject(edge, layoutModel));
            }

            return dataLayer;
        }

        private @NotNull PetriNetObject createPetriNetObject(@NotNull Object object,
                                                             @NotNull LayoutModel2D<Object> model) {
            if (object instanceof TimedPlace) {
                var point2D = model.get(object);
                var timedPlaceComponent = new TimedPlaceComponent(point2D.getX(), point2D.getY(), (TimedPlace) object);
                visualComponents.put(object, timedPlaceComponent);
                return timedPlaceComponent;
            }

            if (object instanceof TimedTransition) {
                var point2D = model.get(object);
                var timedTransitionComponent = new TimedTransitionComponent(point2D.getX(), point2D.getY(), (TimedTransition) object);
                visualComponents.put(object, timedTransitionComponent);
                return timedTransitionComponent;
            }

            if (object instanceof TimedInputArc) {
                var source = visualComponents.get(((TimedInputArc) object).source());
                var target = visualComponents.get(((TimedInputArc) object).destination());

                var arc = new TimedOutputArcComponent(
                        source.getPositionX(),
                        source.getPositionY(),
                        target.getPositionX(),
                        target.getPositionY(),
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
                        source.getPositionX(),
                        source.getPositionY(),
                        target.getPositionX(),
                        target.getPositionY(),
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

        private static  @NotNull LayoutModel2D<Object> createLayout(@NotNull Graph<Object, Object> objectGraph) {
            Random random = new Random(11);

            var layoutAlgorithm2D = new IndexedFRLayoutAlgorithm2D<>(
                    FRLayoutAlgorithm2D.DEFAULT_ITERATIONS,
                    IndexedFRLayoutAlgorithm2D.DEFAULT_THETA_FACTOR,
                    FRLayoutAlgorithm2D.DEFAULT_NORMALIZATION_FACTOR,
                    random);

            var size = calcSize(objectGraph.vertexSet().size());
            MapLayoutModel2D<Object> layoutModel2D = new MapLayoutModel2D<>(new Box2D(size, size));
            layoutAlgorithm2D.layout(objectGraph, layoutModel2D);

            return layoutModel2D;
        }

        private static int calcSize(int vertexCount) {
            return (int)Math.ceil(Math.sqrt(vertexCount) * 100) + 300;
        }
    }

    private static @NotNull String nameForPlace(@NotNull Arc arc) {
        return "place_" + arc.getSource().getId() + "_" + arc.getTarget().getId();
    }

    private static @NotNull String nameForTransition(@NotNull Arc arc) {
        return "transition_" + arc.getSource().getId() + "_" + arc.getTarget().getId();
    }

    private static @NotNull String nameForCollapsingTransition(@NotNull Arc arc) {
        return "collapsing_transition_" + arc.getSource().getId() + "_" + arc.getTarget().getId();
    }
}
