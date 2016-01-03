package org.zwobble.mammoth.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class MammothMaps {
    public static <K, V> Map<K, V> map() {
        return ImmutableMap.of();
    }

    public static <K, V> Map<K, V> map(K key1, V value1) {
        return ImmutableMap.of(key1, value1);
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2) {
        return ImmutableMap.of(key1, value1, key2, value2);
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        return ImmutableMap.of(key1, value1, key2, value2, key3, value3);
    }

    public static <T, K, V> Map<K, V> toMap(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (T element : iterable) {
            builder.put(function.apply(element));
        }
        return builder.build();
    }
}
