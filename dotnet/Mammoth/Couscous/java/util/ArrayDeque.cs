namespace Mammoth.Couscous.java.util {
    internal class ArrayDeque<T> : Deque<T> {
        private readonly System.Collections.Generic.IList<T> _list = new System.Collections.Generic.List<T>();
        
        public int size() {
            return _list.Count;
        }
        
        public T getFirst() {
            if (_list.Count > 0) {
                return _list[0];
            } else {
                throw new NoSuchElementException();
            }
        }
        
        public T getLast() {
            if (_list.Count > 0) {
                return _list[_list.Count - 1];
            } else {
                throw new NoSuchElementException();
            }
        }
        
        public void add(T value) {
            _list.Add(value);
        }
        
        public T removeLast() {
            T value = getLast();
            _list.RemoveAt(_list.Count - 1);
            return value;
        }
    }
}
