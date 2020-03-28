package com.github.zinoviy23.tapaal;

import java.awt.*;
import java.util.function.Supplier;

public class TapaalHeadlessService {
    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    public static void handleHeadless(Runnable runnable) {
        if (!isHeadless()) {
            runnable.run();
        }
    }

    public static <T> T computeIfNotHeadless(Supplier<T> supplier) {
        if (!isHeadless()) {
            return supplier.get();
        }
        return null;
    }
}
