package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

final class MinMax {
  static final MinMax NEUTRAL = new MinMax(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
  double min;
  double max;

  MinMax(double min, double max) {
    this.min = min;
    this.max = max;
  }

  static MinMax reduce(MinMax minMax, double d) {
    return new MinMax(Math.min(minMax.min, d), Math.max(minMax.max, d));
  }

  static MinMax combine(MinMax a, MinMax b) {
    return new MinMax(Math.min(a.min, b.min), Math.max(a.max, b.max));
  }

  double avg() {
    return (min + max) / 2;
  }
}
