using System.Net;
using Mammoth.Couscous.java.io;

namespace Mammoth.Couscous.java.net {
    internal class URL {
        private readonly string _url;
        
        internal URL(string url) {
            _url = url;
        }
        
        internal InputStream openStream() {
            try {
                var response = WebRequest.Create(_url).GetResponse();
                try {
                    return ToJava.StreamToInputStream(response.GetResponseStream());
                } catch {
                    response.Close();
                    throw;
                }
            } catch (System.UriFormatException) {
                return ToJava.StreamToInputStream(System.IO.File.OpenRead(_url));
            } catch (System.Net.WebException exception) {
                throw new java.io.IOException(exception.Message);
            }
        }
    }
}
