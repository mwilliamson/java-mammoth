using Mammoth.Couscous.java.lang;

namespace Mammoth.Couscous.java.util {
    interface List<T> : Collection<T> {
        T get(int index);
        T remove(int index);
    }
}
