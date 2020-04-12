package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.tapnToMg.converters.Converter;
import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.math.BigDecimal;
import java.util.ArrayList;
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
      if (Node.isInfinity(arc.getSource())) continue;

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
    if (Node.isInfinity(arc.getSource())) {
      handleInfinity(graph, arc, petriNet, mapping, graphToDraw);
      return;
    }

    var placeSourceTarget = mapping.get(arc);

    //TODO make rational
    var timedTransition = new TimedTransition(TapnNamingUtil.nameForTransition(arc), false);
    var arcLength = new IntBound(Double.isInfinite(arc.getLength()) ? 0 : (int) arc.getLength());
    var timedInputArc = new TimedInputArc(placeSourceTarget, timedTransition,
        new TimeInterval(
            true, arcLength, arcLength.copy(), true
        )
    );
    petriNet.add(timedTransition);
    petriNet.add(timedInputArc);

    graphToDraw.addVertex(timedTransition);
    graphToDraw.addEdge(placeSourceTarget, timedTransition, timedInputArc);

    addArcForNeighbours(graph, arc, petriNet, mapping, graphToDraw, timedTransition);

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

  private void addArcForNeighbours(@NotNull MetricGraph graph,
                                   @NotNull Arc arc,
                                   @NotNull TimedArcPetriNet petriNet,
                                   @NotNull Map<Arc, TimedPlace> mapping,
                                   @NotNull SimpleDirectedWeightedGraph<Object, Object> graphToDraw,
                                   @NotNull TimedTransition timedTransition) {
    if (!Node.isInfinity(arc.getTarget())) {
      for (Arc outgoingArc : graph.getGraph().outgoingEdgesOf(arc.getTarget())) {
        var destination = mapping.get(outgoingArc);
        var timedOutputArc = new TimedOutputArc(timedTransition, destination);
        petriNet.add(timedOutputArc);
        graphToDraw.addEdge(timedTransition, destination, timedOutputArc);
      }
    }
  }

  private void handleInfinity(@NotNull MetricGraph graph,
                              @NotNull Arc arc,
                              @NotNull TimedArcPetriNet petriNet,
                              @NotNull Map<Arc, TimedPlace> mapping,
                              @NotNull SimpleDirectedWeightedGraph<Object, Object> graphToDraw) {
    if (arc.getPoints().isEmpty()) {
      return;
    }

    var timedTransition = new TimedTransition(TapnNamingUtil.nameForTransition(arc), false);
    petriNet.add(timedTransition);
    var name = TapnNamingUtil.nameForPlace(arc);
    var arcs = new ArrayList<TimedInputArc>();
    for (MovingPoint point : arc.getPoints()) {
      var placeName = TapnNamingUtil.nameForInfinitePlace(name, point);
      var place = new LocalTimedPlace(placeName);
      var length = (int) point.getPosition();
      var inputArc = new TimedInputArc(place, timedTransition, new TimeInterval(
        true, new IntBound(length), new IntBound(length), true
      ));
      petriNet.add(place);
      var token = new TimedToken(place);
      place.addToken(token);
      petriNet.add(inputArc);
      graphToDraw.addVertex(place);
      arcs.add(inputArc);
    }

    graphToDraw.addVertex(timedTransition);
    for (TimedInputArc timedInputArc : arcs) {
      graphToDraw.addEdge(timedInputArc.source(), timedInputArc.destination(), timedInputArc);
    }

    addArcForNeighbours(graph, arc, petriNet, mapping, graphToDraw, timedTransition);
  }
}
