package com.github.zinoviy23.metricGraphs.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ObjectUtil {
  private ObjectUtil() {
  }

  public static <T> void doIfNotNull(@Nullable T t, Consumer<@NotNull T> consumer) {
    if (t != null) {
      consumer.accept(t);
    }
  }
}
