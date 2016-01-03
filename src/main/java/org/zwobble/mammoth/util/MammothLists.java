package org.zwobble.mammoth.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class MammothLists {
    public static <T> List<T> list() {
        return ImmutableList.of();
    }

    public static <T> List<T> list(T value1) {
        return ImmutableList.of(value1);
    }

    public static <T> List<T> list(T value1, T value2) {
        return ImmutableList.of(value1, value2);
    }
}
