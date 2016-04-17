using Mammoth.Couscous.java.io;
using Mammoth.Couscous.java.util;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.docx {
    internal class InMemoryDocxFile : DocxFile {
        internal static DocxFile fromStream(InputStream stream) {
            return new InMemoryDocxFile();
        }
        
        public Optional<InputStream> tryGetInputStream(string name) {
            throw new System.NotImplementedException();
        }
        
        public void close() {
        }
    }
}
