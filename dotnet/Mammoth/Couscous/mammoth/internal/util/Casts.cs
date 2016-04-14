using Mammoth.Couscous.java.util;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util {
    internal static class Casts {
        internal static Optional<T> tryCast<T>(System.Type type, object value) {
            if (value is T) {
                return new Some<T>((T) value);
            } else {
                return new None<T>();
            }
        }
    }
}
