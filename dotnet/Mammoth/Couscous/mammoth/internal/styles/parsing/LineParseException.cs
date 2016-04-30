using Mammoth.Couscous.java.lang;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.styles.parsing {
    internal class LineParseException : RuntimeException {
        private readonly Token _token;
        
        internal LineParseException(Token token, string message) : base(message) {
            _token = token;
        }
        
        internal Token getToken() {
            return _token;
        }
    }
}
