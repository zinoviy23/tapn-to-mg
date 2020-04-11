package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import com.github.zinoviy23.metricGraphs.util.GraphLayout;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.IndexedFRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.AnnotationNote;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class TapnLayoutMaker {
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

  TapnLayoutMaker(@NotNull String graphId, @NotNull Graph<Object, Object> graph) {
    this.graphId = graphId;
    this.graph = graph;
    layoutModel = new GraphLayout().createLayout(graph, VERTEX_SIZE, SIZE_OFFSET);
  }

  private static String getTextFromId(@NotNull String id) {
    return String.format(HEADER, TapnNamingUtil.getTapnName(id), id) + "\n" + DESCRIPTION_TEXT;
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
        true
    );
  }

  private @NotNull PetriNetObject createPetriNetObject(@NotNull Object object,
                                                       @NotNull LayoutModel2D<Object> model) {
    if (object instanceof TimedPlace) {
      var point2D = model.get(object);
      var timedPlaceComponent = new TimedPlaceComponent(
          point2D.getX() + X_OFFSET,
          point2D.getY() + Y_OFFSET,
          (TimedPlace) object
      );
      visualComponents.put(object, timedPlaceComponent);
      return timedPlaceComponent;
    }

    if (object instanceof TimedTransition) {
      var point2D = model.get(object);
      var timedTransitionComponent = new TimedTransitionComponent(
          point2D.getX() + X_OFFSET,
          point2D.getY() + Y_OFFSET,
          (TimedTransition) object
      );
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
}
