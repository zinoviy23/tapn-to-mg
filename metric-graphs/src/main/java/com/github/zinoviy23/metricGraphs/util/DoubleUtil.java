package com.github.zinoviy23.metricGraphs.util;

public class DoubleUtil {
  public static final double DELTA = 1e-5;

  public static boolean equals(double a, double b) {
    // Check infinities
    if (Double.isInfinite(a) && Double.isInfinite(b) && a > 0 && b > 0) return true;

    return Math.abs(a - b) < DELTA;
  }
}
