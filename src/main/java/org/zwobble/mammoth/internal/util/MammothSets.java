package org.zwobble.mammoth.internal.util;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class MammothSets {
    public static <T> Set<T> set() {
        return ImmutableSet.of();
    }

    public static <T> Set<T> set(T value1) {
        return ImmutableSet.of(value1);
    }
}
