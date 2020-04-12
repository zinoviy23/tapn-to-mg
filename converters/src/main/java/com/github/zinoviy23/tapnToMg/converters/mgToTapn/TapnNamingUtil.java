package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import com.github.zinoviy23.metricGraphs.Arc;
import com.github.zinoviy23.metricGraphs.MovingPoint;
import org.jetbrains.annotations.NotNull;

final class TapnNamingUtil {
  private TapnNamingUtil() {
  }

  static @NotNull String getTapnName(@NotNull String id) {
    return "TAPN_" + id;
  }

  static @NotNull String nameForPlace(@NotNull Arc arc) {
    return "p_" + arc.getId();
  }

  static @NotNull String nameForTransition(@NotNull Arc arc) {
    return "t_" + arc.getId();
  }

  static @NotNull String nameForCollapsingTransition(@NotNull Arc arc) {
    return "ct_" + arc.getId();
  }

  static @NotNull String nameForInfinitePlace(@NotNull String name, @NotNull MovingPoint point) {
    return name + "_" + point.getId();
  }
}
