using System;

namespace Mammoth.Couscous {
    internal static class JavaStringExtensions {
        internal static char charAt(this string value, int index) {
            return value[index];
        }
        
        internal static bool equalsIgnoreCase(this string first, string second) {
            return first.Equals(second, StringComparison.InvariantCultureIgnoreCase);
        }
        
        internal static string replace(this string original, string search, string replacement) {
            return original.Replace(search, replacement);
        }
        
        internal static bool isEmpty(this string value) {
            return value.Length == 0;
        }
        
        internal static bool startsWith(this string value, string prefix) {
            return value.StartsWith(prefix);
        }
        
        internal static string trim(this string value) {
            return value.Trim();
        }
        
        internal static string[] split(this string value, string separator) {
            return System.Text.RegularExpressions.Regex.Split(value, separator);
        }
    }
}
