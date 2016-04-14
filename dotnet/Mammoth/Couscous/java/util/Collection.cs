using Mammoth.Couscous.java.lang;

namespace Mammoth.Couscous.java.util {
    interface Collection<T> : Iterable<T> {
        bool isEmpty();
        int size();
        bool contains(object value);
        
        void add(T value);
    }
}
