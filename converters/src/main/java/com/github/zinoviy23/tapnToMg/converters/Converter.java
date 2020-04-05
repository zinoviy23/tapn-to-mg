package com.github.zinoviy23.tapnToMg.converters;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Converter<TFrom, TTo> extends Function<TFrom, TTo> {
    @NotNull TTo convert(@NotNull TFrom from);

    @Override
    default TTo apply(TFrom from) {
        if (from == null) {
            return null;
        }
        return convert(from);
    }
}
