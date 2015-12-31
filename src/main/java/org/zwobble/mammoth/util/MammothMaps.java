package org.zwobble.mammoth.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class MammothMaps {
    public static <T, K, V> Map<K, V> toMap(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (T element : iterable) {
            builder.put(function.apply(element));
        }
        return builder.build();
    }
}
