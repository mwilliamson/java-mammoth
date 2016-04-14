using System;

namespace Mammoth.Couscous {
    internal static class JavaStringExtensions {
        internal static bool equalsIgnoreCase(this string first, string second) {
            return first.Equals(second, StringComparison.InvariantCultureIgnoreCase);
        }
        
        internal static string replace(this string original, string search, string replacement) {
            return original.Replace(search, replacement);
        }
        
        internal static bool isEmpty(this string value) {
            return value.Length == 0;
        }
    }
}
