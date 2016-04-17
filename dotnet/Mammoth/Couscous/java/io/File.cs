using Mammoth.Couscous.java.nio.file;

namespace Mammoth.Couscous.java.io {
    internal class File {
        internal string Path { get; }
        
        internal File(string path) {
            Path = path;
        }
        
        internal Path toPath() {
            return new Path(Path);
        }
    }
}
