package com.github.zinoviy23.tapnToMg.converters.tapnToMg;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.util.Ref;
import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

final class MgMetadataUtil {
  private MgMetadataUtil() {
  }

  static @NotNull String getNodeName(@NotNull TimedTransition transition) {
    return "n_" + transition.name();
  }

  static @NotNull String getNodeComment(@NotNull TimedTransition transition) {
    return "Generated from transition " + transition.name();
  }

  static @NotNull String getArcName(@NotNull TimedTransition transitionSource,
                                    @NotNull TimedPlace place,
                                    @NotNull TimedTransition transitionTarget) {
    return "a_" + transitionSource.name() + "_" + place.name() + "_" + transitionTarget.name();
  }

  static @NotNull String getNameForReversal(@NotNull String arcName) {
    return "rev_" + arcName;
  }

  static @NotNull String getTokenName(@NotNull Ref<Integer> currentId) {
    return "t_" + currentId.update(i -> i + 1);
  }

  static @NotNull String getLeadName(@NotNull TimedOutputArc outputArc) {
    return "l_" + outputArc.source().name() + "_" + outputArc.destination().name();
  }

  static @NotNull String getLeadName(@NotNull TimedInputArc inputArc) {
    return "l_" + inputArc.source().name() + "_" + inputArc.destination().name();
  }

  static @NotNull String getInfName(@NotNull TimedInputArc inputArc) {
    return "inf_" + inputArc.source().name() + "_" + inputArc.destination().name();
  }

  static @NotNull String getInfName(@NotNull TimedOutputArc outputArc) {
    return "inf_" + outputArc.source().name() + "_" + outputArc.destination().name();
  }

  static @NotNull String getGraphName(@NotNull TimedArcPetriNet tapn) {
    return "mg_" + tapn.name();
  }

  static @NotNull String getMultiedgeHandlerNodeName(@NotNull String name) {
    return "br_n_" + name;
  }

  static @NotNull Pair<String, String> getMultiedgeHandlerArcsNames(@NotNull String name) {
    return Pair.of("br_" + name + "_part1", "br_" + name + "_part2");
  }

  static @NotNull Triple<String, String, String> getSelfLoopHandleArcsNames(@NotNull String name) {
    return Triple.of("br_" + name + "_part1", "br_" + name + "_part2", "br_" + name + "_part3");
  }

  static @NotNull String getComment(@NotNull String name, @NotNull TimedArcPetriNet petriNet) {
    return String.format("Metric graph %s generated from corresponding Timed Arc Petri Net %s.\n" +
            "n_* - nodes, generated from transitions.\n" +
            "a_*_*_* - arc, generated from transition, place, and transition.\n" +
            "rev_* - reversal arc. Arc + reversal arc = edge in metric graph.\n" +
            "t_* - point generated from token.\n" +
            "l_* - lead generated from place, which has only incoming or only outgoing arcs.\n" +
            "inf_* - node representing infinity node, which isn't exist.\n" +
            "br_* - additional node, to break self loop or multiple edges between to nodes.",
        name, petriNet.name()
    );
  }
}
