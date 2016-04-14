using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.util;
using Mammoth.Couscous.java.util.function;
using System.Linq;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util {
    internal static class Lists {
        internal static List<T> list<T>(params T[] values) {
            return ToJava.ListToList(values);
        }
        
        internal static List<T> toList<T>(Iterable<T> iterable) {
            return ToJava.ListToList(FromJava.IterableToEnumerable(iterable).ToList());
        }
        
        internal static List<U> eagerMap<T, U>(Iterable<T> iterable, Function<T, U> function) {
            var result = FromJava.IterableToEnumerable(iterable)
                .Select(function.apply)
                .ToList();
            return ToJava.ListToList(result);
        }
        
        internal static List<U> eagerFlatMap<T, U>(Iterable<T> iterable, Function<T, Iterable<U>> function) {
            var result = FromJava.IterableToEnumerable(iterable)
                .SelectMany(element => FromJava.IterableToEnumerable(function.apply(element)))
                .ToList();
            return ToJava.ListToList(result);
        }
        
        internal static List<T> cons<T>(T head, Iterable<T> tail) {
            var result = new T[] { head }
				.Concat(FromJava.IterableToEnumerable(tail))
                .ToList();
			return ToJava.ListToList(result);
        }
        
        internal static List<T> eagerConcat<T>(Iterable<T> first, Iterable<T> second) {
            var result = FromJava.IterableToEnumerable(first)
				.Concat(FromJava.IterableToEnumerable(second))
                .ToList();
			return ToJava.ListToList(result);
        }
        
        internal static List<T> orderedBy<T, R>(Iterable<T> iterable, Function<T, R> getKey) {
            var result = FromJava.IterableToEnumerable(iterable)
                .OrderBy(getKey.apply)
                .ToList();
            return ToJava.ListToList(result);
        }
        
        internal static Optional<T> tryGetLast<T>(List<T> list) {
            if (list.isEmpty()) {
                return new None<T>();
            } else {
                return new Some<T>(list.get(list.size() - 1));
            }
        }
        
        internal static Iterable<T> reversed<T>(List<T> list) {
            return new ReversedList<T>(list);
        }
        
        private class ReversedList<T> : Iterable<T> {
            private readonly List<T> _list;
            
            internal ReversedList(List<T> list) {
                _list = list;
            }
            
            public Iterator<T> iterator() {
                return new ReverseIterator<T>(_list);
            }
        }
        
        private class ReverseIterator<T> : Iterator<T> {
            private readonly List<T> _list;
            private int _nextIndex;
            
            internal ReverseIterator(List<T> list) {
                _list = list;
                _nextIndex = _list.size() - 1;
            }
            
            public bool hasNext() {
                return _nextIndex >= 0;
            }
            
            public T next() {
                if (hasNext()) {
                    return _list.get(_nextIndex--);
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
    }
}
