using Mammoth.Couscous.java.util;
using Mammoth.Couscous.org.zwobble.mammoth.@internal.documents;
using Mammoth.Couscous.org.zwobble.mammoth.@internal.styles.parsing;
using System.Linq;

namespace Mammoth.Couscous {
    internal static class HorribleHacks {
        internal static bool equals(this char first, int second) {
            return first == second;
        }
        
        internal static bool equals(this Optional<string> first, Optional<string> second) {
            return (!first.isPresent() && !second.isPresent()) || (first.isPresent() && second.isPresent() && first.get() == second.get());
        }
        
        internal static bool equals(this Map<string, string> firstMap, Map<string, string> secondMap) {
            var first = FromJava.MapToDictionary(firstMap);
            var second = FromJava.MapToDictionary(secondMap);
            return first.Count == second.Count && first.All(item => second.ContainsKey(item.Key) && second[item.Key] == item.Value);
        }
        
        internal static bool equals(this NoteType first, NoteType second) {
            return first == second;
        }
        
        internal static bool equals(this TokenType first, TokenType second) {
            return first == second;
        }
        
        internal static string getMessage(this System.Exception exception) {
            return exception.Message;
        }
    }
}
