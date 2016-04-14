namespace Mammoth.Couscous.java.util.function
{
    internal interface BiFunction<T1, T2, U> {
        U apply(T1 first, T2 second);
    }
}