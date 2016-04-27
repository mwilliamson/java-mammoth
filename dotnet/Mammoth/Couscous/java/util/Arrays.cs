namespace Mammoth.Couscous.java.util {
    internal static class Arrays {
        internal static List<T> asList<T>(T[] elements) {
            return ToJava.ListToList(elements);
        }
    }
}
