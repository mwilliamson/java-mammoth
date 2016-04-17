using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.io;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util {
    internal static class Base64Encoding {
        internal static string streamToBase64(SupplierWithException<InputStream, IOException> streamSupplier) {
            using (var stream = streamSupplier.get().Stream) {
                return streamToBase64(stream);
            }
        }
        
        internal static string streamToBase64(System.IO.Stream stream) {
            var memoryStream = new System.IO.MemoryStream();
            stream.CopyTo(memoryStream);
            return System.Convert.ToBase64String(memoryStream.ToArray());
        }
    }
}
