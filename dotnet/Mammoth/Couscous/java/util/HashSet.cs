using System.Collections.Generic;

namespace Mammoth.Couscous.java.util {
    internal class HashSet<T> : Set<T> {
        private readonly ISet<T> _set;

        internal HashSet() : this(new System.Collections.Generic.HashSet<T>()) {
        }
        
        internal HashSet(ISet<T> set) {
            _set = set;
        }
        
        public Iterator<T> iterator() {
            return ToJava.EnumeratorToIterator(_set.GetEnumerator());
        }
        
        public bool isEmpty() {
            return _set.Count == 0;
        }
        
        public int size() {
            return _set.Count;
        }

        public bool contains(object value) {
            return value is T && _set.Contains((T)value);
        }
        
        public void add(T value) {
            _set.Add(value);
        }
    }
}
