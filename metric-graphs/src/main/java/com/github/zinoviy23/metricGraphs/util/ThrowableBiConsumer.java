package com.github.zinoviy23.metricGraphs.util;

@FunctionalInterface
public interface ThrowableBiConsumer<T, E, TExp extends Throwable> {
    void  consume(T t, E e) throws TExp;
}
