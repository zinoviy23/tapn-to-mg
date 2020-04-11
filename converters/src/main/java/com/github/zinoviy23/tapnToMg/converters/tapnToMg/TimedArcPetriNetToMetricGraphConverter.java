package com.github.zinoviy23.tapnToMg.converters.tapnToMg;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import com.github.zinoviy23.metricGraphs.Node;
import com.github.zinoviy23.metricGraphs.util.Ref;
import com.github.zinoviy23.tapnToMg.converters.Converter;
import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TimedArcPetriNetToMetricGraphConverter implements Converter<TimedArcPetriNet, MetricGraph> {
  @Override
  public @NotNull MetricGraph convert(@NotNull TimedArcPetriNet petriNet) {
    var graphBuilder = MetricGraph.createBuilder().setId(MgMetadataUtil.getGraphName(petriNet));

    var transitionNodes = addNodes(graphBuilder, petriNet);
    var placesArcs = new TapnPlacesArcs(petriNet);
    var tokenId = addEdges(graphBuilder, petriNet, transitionNodes, placesArcs);
    addLeads(graphBuilder, petriNet, transitionNodes, placesArcs, tokenId);

    return graphBuilder.buildGraph();
  }

  private @NotNull Map<TimedTransition, Node> addNodes(@NotNull MetricGraph.MetricGraphBuilder builder,
                                                       @NotNull TimedArcPetriNet petriNet) {
    var result = new HashMap<TimedTransition, Node>();

    for (TimedTransition transition : petriNet.transitions()) {
      assertTransition(transition);

      var id = MgMetadataUtil.getNodeName(transition);
      var comment = MgMetadataUtil.getNodeComment(transition);
      var node = Node.createNode(id, id, comment);
      builder.addNode(node);
      result.put(transition, node);
    }

    return result;
  }

  private Ref<Integer> addEdges(@NotNull MetricGraph.MetricGraphBuilder builder,
                                @NotNull TimedArcPetriNet petriNet,
                                @NotNull Map<TimedTransition, Node> transitionNodes,
                                @NotNull TapnPlacesArcs arcs) {
    var tokenId = new Ref<>(0);
    for (TimedTransition transition : petriNet.transitions()) {
      var source = transitionNodes.get(transition);
      for (TimedOutputArc outputArc : transition.getOutputArcs()) {
        var place = outputArc.destination();
        for (TimedInputArc arc : arcs.getOutputArcs(place)) {
          var target = transitionNodes.get(arc.destination());
          String name = addEdge(builder, transition, source, place, arc, target);

          var points = getPointsFromPlace(tokenId, place);
          builder.addPoints(name, points);
        }
      }
    }
    return tokenId;
  }

  private @NotNull String addEdge(@NotNull MetricGraph.MetricGraphBuilder builder,
                                  @NotNull TimedTransition transition,
                                  @NotNull Node source,
                                  @NotNull TimedPlace place,
                                  @NotNull TimedInputArc arc,
                                  @NotNull Node target) {
    String name = MgMetadataUtil.getArcName(transition, place, arc.destination());
    if (!builder.containsEdge(source, target) && !source.equals(target)) {
      var resultArc = Arc.createBuilder()
          .setSource(source)
          .setTarget(target)
          .setLength(arc.interval().upperBound().value())
          .setId(name);

      builder.addArc(resultArc).withReversal(MgMetadataUtil.getNameForReversal(name));
    } else if (!source.equals(target)) {
      var handlerNode = Node.createMultiEdgeHandler(MgMetadataUtil.getMultiedgeHandlerNodeName(name), source, target);
      var edgesNames = MgMetadataUtil.getMultiedgeHandlerArcsNames(name);
      var length = arc.interval().upperBound().value() / 2.0;
      builder
          .addNode(handlerNode)
          .addArc(Arc.createBuilder()
              .setSource(source)
              .setTarget(handlerNode)
              .setId(edgesNames.getFirst())
              .setLength(length)
          ).withReversal(MgMetadataUtil.getNameForReversal(edgesNames.getFirst()))
          .addArc(Arc.createBuilder()
              .setSource(handlerNode)
              .setTarget(target)
              .setId(edgesNames.getSecond())
              .setLength(length)
          ).withReversal(MgMetadataUtil.getNameForReversal(edgesNames.getSecond()));

      name = edgesNames.getFirst();
    } else {
      var edgesNames = MgMetadataUtil.getSelfLoopHandleArcsNames(name);
      var loopFixes = fixSelfLoop(name, source, target);
      var length = arc.interval().upperBound().value() / 3.0;
      builder
          .addNode(loopFixes.getFirst())
          .addNode(loopFixes.getSecond())
          .addArc(Arc.createBuilder()
              .setSource(source)
              .setTarget(loopFixes.getFirst())
              .setId(edgesNames.getFirst())
              .setLength(length)
          ).withReversal(MgMetadataUtil.getNameForReversal(edgesNames.getFirst()))
          .addArc(Arc.createBuilder()
              .setSource(loopFixes.getFirst())
              .setTarget(loopFixes.getSecond())
              .setId(edgesNames.getSecond())
              .setLength(length)
          ).withReversal(MgMetadataUtil.getNameForReversal(edgesNames.getSecond()))
          .addArc(Arc.createBuilder()
              .setSource(loopFixes.getSecond())
              .setTarget(target)
              .setId(edgesNames.getThird())
              .setLength(length)
          ).withReversal(MgMetadataUtil.getNameForReversal(edgesNames.getThird()));
      name = edgesNames.getFirst();
    }

    return name;
  }

  private Pair<Node, Node> fixSelfLoop(@NotNull String name, @NotNull Node source, @NotNull Node target) {
    var handlerNode1 = Node.createMultiEdgeHandler(MgMetadataUtil.getMultiedgeHandlerNodeName(name) + "_1", source, target);
    var handlerNode2 = Node.createMultiEdgeHandler(MgMetadataUtil.getMultiedgeHandlerNodeName(name) + "_2", source, target);
    return Pair.of(handlerNode1, handlerNode2);
  }

  private @NotNull List<MovingPoint> getPointsFromPlace(Ref<Integer> tokenId, TimedPlace place) {
    // TODO: in paper points should be at the beginning of arc, but it isn't intuitive
    return place.tokens().stream()
        .map(token -> new MovingPoint(MgMetadataUtil.getTokenName(tokenId), 0))
        .collect(Collectors.toList());
  }

  private void addLeads(@NotNull MetricGraph.MetricGraphBuilder builder,
                        @NotNull TimedArcPetriNet petriNet,
                        @NotNull Map<TimedTransition, Node> transitionNodes,
                        @NotNull TapnPlacesArcs arcs,
                        @NotNull Ref<Integer> tokenId) {
    for (TimedPlace place : petriNet.places()) {
      var outputArcs = arcs.getOutputArcs(place);
      var inputArcs = arcs.getInputArcs(place);
      if (!inputArcs.isEmpty() && outputArcs.isEmpty()) {
        for (TimedOutputArc inputArc : inputArcs) {
          var node = transitionNodes.get(inputArc.source());
          var leadName = MgMetadataUtil.getLeadName(inputArc);
          var infinity = Node.createInfinity(MgMetadataUtil.getInfName(inputArc));
          builder
              .addNode(infinity)
              .addArc(Arc.createBuilder()
                  .setSource(node)
                  .setTarget(infinity)
                  .setLength(Double.POSITIVE_INFINITY)
                  .setId(leadName)
                  .createArc()
              )
              .withReversal(MgMetadataUtil.getNameForReversal(leadName));
        }
      }
      if (inputArcs.isEmpty() && !outputArcs.isEmpty()) {
        for (TimedInputArc outputArc : outputArcs) {
          var node = transitionNodes.get(outputArc.destination());
          var leadName = MgMetadataUtil.getLeadName(outputArc);
          var infinity = Node.createInfinity(MgMetadataUtil.getInfName(outputArc));
          builder
              .addNode(infinity)
              .addArc(Arc.createBuilder()
                  .setId(leadName)
                  .setSource(infinity)
                  .setTarget(node)
                  .setLength(Double.POSITIVE_INFINITY)
                  .setPoints(getPointsFromInputArc(tokenId, outputArc))
                  .createArc()
              )
              .withReversal(MgMetadataUtil.getNameForReversal(leadName));
        }
      }
    }
  }

  private @NotNull List<MovingPoint> getPointsFromInputArc(Ref<Integer> tokenId, TimedInputArc arc) {
    return arc.source().tokens().stream()
        .map(token -> new MovingPoint(MgMetadataUtil.getTokenName(tokenId), arc.interval().upperBound().value()))
        .collect(Collectors.toList());
  }

  private static void assertTransition(@NotNull TimedTransition transition) {
    if (transition.isUrgent()) {
      throw new TimedArcPetriNetToMetricGraphConversionException(
          String.format("Transition %s must be non-urgent", transition.name())
      );
    }
  }
}
