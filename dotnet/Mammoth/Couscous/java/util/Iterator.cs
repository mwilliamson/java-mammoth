namespace Mammoth.Couscous.java.util {
    internal interface Iterator<out T> {
        bool hasNext();
        T next();
    }
}
