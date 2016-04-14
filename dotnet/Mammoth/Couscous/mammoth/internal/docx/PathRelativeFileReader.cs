using Mammoth.Couscous.java.util;
using Mammoth.Couscous.java.io;
using Mammoth.Couscous.java.nio.file;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.docx {
    internal class PathRelativeFileReader : FileReader {
        private readonly Optional<Path> _path;
        
        internal PathRelativeFileReader(Optional<Path> path) {
            _path = path;
        }
        
        public InputStream getInputStream(string uri) {
            throw new System.NotImplementedException();
        }
    }
}
