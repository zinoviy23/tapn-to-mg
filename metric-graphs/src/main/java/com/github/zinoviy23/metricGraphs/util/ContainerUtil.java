package com.github.zinoviy23.metricGraphs.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class ContainerUtil {
    public static <T> @Nullable T first(@NotNull Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public static <T> @Nullable T second(@NotNull Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        iterator.next();
        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }

    public static <T1, T2, T3> @Nullable T3 getFromTable(Map<T1, Map<T2, T3>> table, T1 row, T2 column) {
        var tmpRow = table.get(row);
        if (tmpRow == null) return null;

        return tmpRow.get(column);
    }

    /**
     * Should be used once. You should close given stream by your own, after iteration
     * @return iterable, wrapping this stream.
     */
    public static <T> @NotNull Iterable<T> iterate(Stream<T> stream) {
        return stream::iterator;
    }
}
