namespace Mammoth.Couscous.java.util {
    internal interface Deque<T> {
        int size();
        T getFirst();
        T getLast();
        
        void add(T value);
        T removeLast();
    }
}
