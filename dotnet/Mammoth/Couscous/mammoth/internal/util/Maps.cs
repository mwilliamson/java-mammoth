using System.Linq;
using Mammoth.Couscous.java.util;
using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.util.function;
using Mammoth.Couscous;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util
{
    internal static class Maps
    {
        internal static Map<K, V> map<K, V>() {
            return ToJava.DictionaryToMap(new System.Collections.Generic.Dictionary<K, V>());
        }
        
        internal static Map<K, V> map<K, V>(K key1, V value1) {
            return ToJava.DictionaryToMap(new System.Collections.Generic.Dictionary<K, V> {
                {key1, value1}
            });
        }
        
        internal static Map<K, V> map<K, V>(K key1, V value1, K key2, V value2) {
            return ToJava.DictionaryToMap(new System.Collections.Generic.Dictionary<K, V> {
                {key1, value1},
                {key2, value2}
            });
        }
        
        internal static Map<K, V> toMap<T, K, V>(Iterable<T> iterable, Function<T, Map__Entry<K, V>> function) {
            var dictionary = FromJava.IterableToEnumerable(iterable)
                .Select(function.apply)
                .ToDictionary(entry => entry.getKey(), entry => entry.getValue());
            return ToJava.DictionaryToMap(dictionary);
            
        }
        
        internal static Map<K, T> toMapWithKey<T, K>(Iterable<T> iterable, Function<T, K> function) {
            var dictionary = FromJava.IterableToEnumerable(iterable)
                .ToDictionary(function.apply);
            return ToJava.DictionaryToMap(dictionary);
        }
        
        internal static Map<K, java.util.List<T>> toMultiMapWithKey<T, K>(Iterable<T> iterable, Function<T, K> function) {
            var dictionary = FromJava.IterableToEnumerable(iterable)
                .GroupBy(value => function.apply(value))
                .ToDictionary(
                    grouping => grouping.Key,
                    grouping => ToJava.ListToList(grouping.ToList()));
            return ToJava.DictionaryToMap(dictionary);
        }
        
        internal static Optional<V> lookup<K, V>(Map<K, V> map, K key) {
            return map._lookup(key);
        }
        
        internal static Map<K2, V> eagerMapKeys<K1, K2, V>(Map<K1, V> map, Function<K1, K2> function) {
            var dictionary = FromJava.MapToDictionary(map)
                .ToDictionary(
                    entry => function.apply(entry.Key),
                    entry => entry.Value);
            return ToJava.DictionaryToMap(dictionary);
        }
        
        internal static Map<K, V2> eagerMapValues<K, V1, V2>(Map<K, V1> map, Function<V1, V2> function) {
            var dictionary = FromJava.MapToDictionary(map)
                .ToDictionary(
                    entry => entry.Key,
                    entry => function.apply(entry.Value));
            return ToJava.DictionaryToMap(dictionary);
        }
        
        internal static Map__Entry<K, V> entry<K, V>(K key, V value) {
            return new Entry<K, V>(key, value);
        }
        
        private class Entry<K, V> : Map__Entry<K, V> {
            private readonly K _key;
            private readonly V _value;
            
            internal Entry(K key, V value) {
                this._key = key;
                this._value = value;
            }
            
            public K getKey() {
                return _key;
            }
            
            public V getValue() {
                return _value;
            }
        }
        
        internal static Builder<K, V> builder<K, V>() {
            return new Builder<K, V>();
        }
        
        internal class Builder<K, V> {
            private readonly System.Collections.Generic.IDictionary<K, V> _values = new System.Collections.Generic.Dictionary<K, V>();
            
            internal Builder<K, V> put(K key, V value) {
                _values[key] = value;
                return this;
            }
            
            internal Map<K, V> build() {
                return ToJava.DictionaryToMap(_values);
            }
        }
    }
}
