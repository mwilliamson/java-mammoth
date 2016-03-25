package org.zwobble.mammoth.internal.util;

import com.google.common.collect.ImmutableMap;

import java.util.*;
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

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

    public static <K, V> Optional<V> lookup(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    public static <T, K> Map<K, T> toMapWithKey(Iterable<T> iterable, Function<T, K> function) {
        return toMap(iterable, element -> entry(function.apply(element), element));
    }

    public static <T, K, V> Map<K, V> toMap(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (T element : iterable) {
            builder.put(function.apply(element));
        }
        return builder.build();
    }

    public static <T, K> Map<K, List<T>> toMultiMapWithKey(Iterable<T> iterable, Function<T, K> function) {
        return toMultiMap(iterable, element -> entry(function.apply(element), element));
    }

    public static <T, K, V> Map<K, List<V>> toMultiMap(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        Map<K, List<V>> map = new HashMap<>();
        for (T element : iterable) {
            Map.Entry<K, V> pair = function.apply(element);
            map.computeIfAbsent(pair.getKey(), key -> new ArrayList<V>())
                .add(pair.getValue());
        }
        return map;
    }

    public static <K, V1, V2> Map<K, V2> eagerMapValues(Map<K, V1> map, Function<V1, V2> function) {
        Map<K, V2> result = new HashMap<>();
        for (Map.Entry<K, V1> element : map.entrySet()) {
            result.put(element.getKey(), function.apply(element.getValue()));
        }
        return result;
    }
}
