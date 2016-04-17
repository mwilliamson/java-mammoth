using System.Collections.Generic;
using System.Linq;
using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.util;
using Mammoth.Couscous.java.io;

namespace Mammoth.Couscous {
    internal static class FromJava {
        internal static IEnumerable<T> IterableToEnumerable<T>(Iterable<T> iterable) {
            Iterator<T> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                yield return iterator.next();
            }
        }
        
        internal static IDictionary<TKey, TValue> MapToDictionary<TKey, TValue>(Map<TKey, TValue> map) {
            return map.AsDictionary();
        }
    }
    
    internal static class ToJava {
        internal static Iterable<T> EnumerableToIterable<T>(IEnumerable<T> enumerator) {
            return new IterableWrapper<T>(enumerator);
        }
        
        private class IterableWrapper<T> : Iterable<T> {
            private readonly IEnumerable<T> _enumerable;
            
            internal IterableWrapper(IEnumerable<T> enumerable) {
                _enumerable = enumerable;
            }
            
            public Iterator<T> iterator() {
                return EnumeratorToIterator(_enumerable.GetEnumerator());
            }
        }
        
        internal static Iterator<T> EnumeratorToIterator<T>(IEnumerator<T> enumerator) {
            return new EnumeratorToIteratorAdapter<T>(enumerator);
        }
        
        private class EnumeratorToIteratorAdapter<T> : Iterator<T> {
            private readonly IEnumerator<T> _enumerator;
            private bool _ready;
            private bool _hasNext;
            
            internal EnumeratorToIteratorAdapter(IEnumerator<T> enumerator) {
                _enumerator = enumerator;
                _ready = false;
            }
            
            public bool hasNext() {
                MakeReady();
                return _hasNext;
            }
            
            public T next() {
                MakeReady();
                if (_hasNext) {
                    _ready = false;
                    return _enumerator.Current;
                } else {
                    throw new NoSuchElementException();
                }
            }
            
            private void MakeReady() {
                if (!_ready) {
                    MoveNext();
                }
            }
            
            private void MoveNext() {
                _hasNext = _enumerator.MoveNext();
                _ready = true;
            }
        }
        
        internal static Map<TKey, TValue> DictionaryToMap<TKey, TValue>(IDictionary<TKey, TValue> dictionary) {
            return new HashMap<TKey, TValue>(dictionary);
        }
        
        internal static java.util.List<T> ListToList<T>(IList<T> list) {
            return new ArrayList<T>(list);
        }
        
        internal static java.io.InputStream StreamToInputStream(System.IO.Stream stream) {
            return new StreamToInputStreamAdapter(stream);
        }
        
        private class StreamToInputStreamAdapter : InputStream {
            public System.IO.Stream Stream { get; }
            
            internal StreamToInputStreamAdapter(System.IO.Stream stream) {
                Stream = stream;
            }
        }
    }
}
