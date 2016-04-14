using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.util.function;

namespace Mammoth.Couscous {
    internal static class IterableExtensions {
        public static void forEach<T>(this Iterable<T> iterable, Consumer<T> consumer) {
            var iterator = iterable.iterator();
            while (iterator.hasNext()) {
                consumer.accept(iterator.next());
            }
        }
    }
}
