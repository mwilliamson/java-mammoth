namespace Mammoth.Couscous.java.lang {
    internal partial class String {
        internal static string join(string separator, Iterable<string> values) {
            return string.Join(separator, FromJava.IterableToEnumerable(values));
        }
    }
}
