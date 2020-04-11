package com.github.zinoviy23.tapnToMg.converters.tapnToMg;

import dk.aau.cs.model.tapn.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class TapnPlacesArcs {
  private final Map<TimedPlace, List<TimedInputArc>> outputArcs = new HashMap<>();
  private final Map<TimedPlace, List<TimedOutputArc>> inputArcs = new HashMap<>();

  public TapnPlacesArcs(@NotNull TimedArcPetriNet petriNet) {
    for (TimedTransition transition : petriNet.transitions()) {
      for (TimedOutputArc outputArc : transition.getOutputArcs()) {
        inputArcs.computeIfAbsent(outputArc.destination(), __ -> new ArrayList<>()).add(outputArc);
      }
      for (TimedInputArc inputArc : transition.getInputArcs()) {
        assertInputArc(inputArc);
        outputArcs.computeIfAbsent(inputArc.source(), __ -> new ArrayList<>()).add(inputArc);
      }
    }
  }

  public List<TimedInputArc> getOutputArcs(@NotNull TimedPlace place) {
    var list = outputArcs.get(place);
    return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
  }

  public List<TimedOutputArc> getInputArcs(@NotNull TimedPlace place) {
    var list = inputArcs.get(place);
    return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
  }

  private static void assertInputArc(@NotNull TimedInputArc arc) {
    var interval = arc.interval();
    if (!interval.IsLowerBoundNonStrict() || !interval.IsUpperBoundNonStrict()) {
      throw new TimedArcPetriNetToMetricGraphConversionException(
          String.format("Time interval %s in %s must be closed!", interval, arc)
      );
    }

    if (interval.lowerBound().value() != interval.upperBound().value()) {
      throw new TimedArcPetriNetToMetricGraphConversionException(
          String.format("Time interval %s in %s must be zero length!", interval, arc)
      );
    }
  }
}
