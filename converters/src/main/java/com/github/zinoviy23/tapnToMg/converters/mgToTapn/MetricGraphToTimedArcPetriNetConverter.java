package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.tapnToMg.converters.Converter;
import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class MetricGraphToTimedArcPetriNetConverter implements Converter<MetricGraph, ConvertedTimedArcPetriNet> {
    @Override
    public @NotNull ConvertedTimedArcPetriNet convert(@NotNull MetricGraph graph) {
        var petriNet = new TimedArcPetriNet(TapnNamingUtil.getTapnName(graph.getId()));

        var layoutGraph = new SimpleDirectedWeightedGraph<>(null, null);
        var mapping = addPlacesForEachArc(graph, petriNet, layoutGraph);

        for (Arc arc : graph.getGraph().edgeSet()) {
            processArc(graph, arc, petriNet, mapping, layoutGraph);
        }

        var result = new TimedArcPetriNetNetwork();
        result.add(petriNet);

        var layoutMaker = new TapnLayoutMaker(graph.getId(), layoutGraph);

        return new ConvertedTimedArcPetriNet(result, petriNet, layoutMaker.createDataLayer());
    }

    private @NotNull Map<Arc, TimedPlace> addPlacesForEachArc(@NotNull MetricGraph graph,
                                                              @NotNull TimedArcPetriNet petriNet,
                                                              @NotNull Graph<Object, Object> layoutGraph) {
        var arcToPlaceMapping = new HashMap<Arc, TimedPlace>();
        for (Arc arc : graph.getGraph().edgeSet()) {
            var placeSourceTarget = new LocalTimedPlace(TapnNamingUtil.nameForPlace(arc));
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
        var timedTransition = new TimedTransition(TapnNamingUtil.nameForTransition(arc), false);
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

        var collapsingTransition = new TimedTransition(TapnNamingUtil.nameForCollapsingTransition(arc), false);
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
}
