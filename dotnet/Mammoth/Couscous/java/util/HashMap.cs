using System.Collections.Generic;
using System.Linq;
using Mammoth.Couscous.org.zwobble.mammoth.@internal.util;
using Mammoth.Couscous.java.lang;

namespace Mammoth.Couscous.java.util {
    internal class HashMap<TKey, TValue> : Map<TKey, TValue> {
        private readonly IDictionary<TKey, TValue> _dictionary;
        
        internal HashMap() : this(new Dictionary<TKey, TValue>()) {
        }
        
        internal HashMap(IDictionary<TKey, TValue> dictionary) {
            _dictionary = dictionary;
        }
        
        public void put(TKey key, TValue value) {
            _dictionary[key] = value;
        }
        
        public bool containsKey(TKey key) {
            return _dictionary.ContainsKey(key);
        }
        
        public Set<Map__Entry<TKey, TValue>> entrySet() {
            return new EntrySet(_dictionary);
        }
        
        public Optional<TValue> _lookup(TKey key) {
            if (_dictionary.ContainsKey(key)) {
                return new Some<TValue>(_dictionary[key]);
            } else {
                return new None<TValue>();
            }
        }
        
        public IDictionary<TKey, TValue> AsDictionary() {
            return _dictionary;
        }
        
        internal class EntrySet : Set<Map__Entry<TKey, TValue>> {
            private readonly IDictionary<TKey, TValue> _dictionary;
            
            internal EntrySet(IDictionary<TKey, TValue> dictionary) {
                _dictionary = dictionary;
            }
            
            public Iterator<Map__Entry<TKey, TValue>> iterator() {
                return ToJava.EnumeratorToIterator(_dictionary.Select(entry => Maps.entry(entry.Key, entry.Value)).GetEnumerator());
            }
            
            public bool isEmpty() {
                return _dictionary.Count == 0;
            }
            
            public int size() {
                return _dictionary.Count;
            }
            
            public bool contains(object value) {
                return value is TKey && _dictionary.ContainsKey((TKey) value);
            }
            
            public void add(Map__Entry<TKey, TValue> value) {
                throw new UnsupportedOperationException();
            }
        }
    }
}
