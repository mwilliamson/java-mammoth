using Mammoth.Couscous.java.io;
using Mammoth.Couscous.java.util;
using System.IO;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.docx {
    internal class InMemoryDocxFile {
        internal static DocxFile fromStream(InputStream stream) {
            var memoryStream = new MemoryStream();
            stream.Stream.CopyTo(memoryStream);
            return new ZippedDocxFile(memoryStream);
        }
    }
}
