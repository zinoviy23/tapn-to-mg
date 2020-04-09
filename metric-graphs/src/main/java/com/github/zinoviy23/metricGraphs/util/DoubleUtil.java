package com.github.zinoviy23.metricGraphs.util;

public class DoubleUtil {
  public static final double DELTA = 1e-5;

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean equals(double a, double b) {
    return Math.abs(a - b) < DELTA;
  }
}
