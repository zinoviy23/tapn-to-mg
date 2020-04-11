package com.github.zinoviy23.metricGraphs.util;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.FRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.IndexedFRLayoutAlgorithm2D;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;

import java.util.Random;

public class GraphLayout {
  private final long randomSeed;
  private final int iterations;
  private final double thetaFactor;
  private final double normalizationFactor;

  public GraphLayout() {
    this(11);
  }

  public GraphLayout(long randomSeed) {
    this(randomSeed, 200);
  }

  public GraphLayout(long randomSeed, int iterations) {
    this(randomSeed, iterations, IndexedFRLayoutAlgorithm2D.DEFAULT_THETA_FACTOR);
  }

  public GraphLayout(long randomSeed, int iterations, double thetaFactor) {
    this(randomSeed, iterations, thetaFactor, FRLayoutAlgorithm2D.DEFAULT_NORMALIZATION_FACTOR);
  }

  public GraphLayout(long randomSeed, int iterations, double thetaFactor, double normalizationFactor) {
    this.randomSeed = randomSeed;
    this.iterations = iterations;
    this.thetaFactor = thetaFactor;
    this.normalizationFactor = normalizationFactor;
  }

  public <V, E> @NotNull LayoutModel2D<V> createLayout(Graph<V, E> graph, int vertexSize, int sizeOffset) {
    Random random = new Random(randomSeed);

    var layoutAlgorithm2D = new IndexedFRLayoutAlgorithm2D<V, E>(
        iterations,
        thetaFactor,
        normalizationFactor,
        random
    );

    var size = calcSize(graph.vertexSet().size(), vertexSize, sizeOffset);
    MapLayoutModel2D<V> layoutModel2D = new MapLayoutModel2D<>(new Box2D(size, size));
    layoutAlgorithm2D.layout(graph, layoutModel2D);

    return layoutModel2D;
  }

  private static int calcSize(int vertexCount, int vertexSize, int sizeOffset) {
    return (int) Math.ceil(Math.sqrt(vertexCount) * vertexSize) + sizeOffset;
  }
}
