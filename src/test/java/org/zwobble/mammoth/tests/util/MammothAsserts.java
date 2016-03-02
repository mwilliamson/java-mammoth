package org.zwobble.mammoth.tests.util;

import org.zwobble.mammoth.internal.util.Casts;

public class MammothAsserts {
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Action action) throws T {
        try {
            action.run();
            throw new AssertionError("Expected exception");
        } catch (RuntimeException exception) {
            return Casts.tryCast(exceptionType, exception)
                .orElseThrow(() -> exception);
        }
    }
}
