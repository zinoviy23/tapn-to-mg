package com.github.zinoviy23.metricGraphs.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class Ref<T> {
  private T data;

  public Ref(T data) {
    this.data = data;
  }

  public Ref() {
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  /**
   * Updates containing value
   * @param updateOperator operator for updating value
   * @return previous value
   */
  public T update(@NotNull UnaryOperator<T> updateOperator) {
    T prev = data;
    data = updateOperator.apply(data);
    return prev;
  }
}
