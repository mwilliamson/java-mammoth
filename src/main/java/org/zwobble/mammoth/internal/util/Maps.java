package org.zwobble.mammoth.internal.util;

import java.util.*;
import java.util.function.Function;

public class Maps {
    public static <K, V> Map<K, V> map() {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> map(K key1, V value1) {
        return Collections.singletonMap(key1, value1);
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    public static <K, V> Map<K, V> mutableMap(K key1, V value1) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        return map;
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
        Map<K, V> map = new HashMap<>();
        for (T element : iterable) {
            Map.Entry<K, V> entry = function.apply(element);
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Convert an iterable to map using the given mapping function. If the
     * mapping function generates entries with the same key, only the first
     * entry will be used, and subsequent entries will be ignored.
     */
    public static <T, K, V> Map<K, V> toMapPreferFirst(Iterable<T> iterable, Function<T, Map.Entry<K, V>> function) {
        Map<K, V> map = new HashMap<>();
        for (T element : iterable) {
            Map.Entry<K, V> entry = function.apply(element);
            if (!map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
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

    public static <K1, K2, V> Map<K2, V> eagerMapKeys(Map<K1, V> map, Function<K1, K2> function) {
        Map<K2, V> result = new HashMap<>();
        for (Map.Entry<K1, V> element : map.entrySet()) {
            result.put(function.apply(element.getKey()), element.getValue());
        }
        return result;
    }

    public static <K, V1, V2> Map<K, V2> eagerMapValues(Map<K, V1> map, Function<V1, V2> function) {
        Map<K, V2> result = new HashMap<>();
        for (Map.Entry<K, V1> element : map.entrySet()) {
            result.put(element.getKey(), function.apply(element.getValue()));
        }
        return result;
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {
        private final Map<K, V> values = new HashMap<>();

        public Builder<K, V> put(K key, V value) {
            values.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return values;
        }
    }
}
