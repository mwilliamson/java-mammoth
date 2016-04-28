using System.IO;
using Mammoth.Couscous;
using Mammoth.Couscous.org.zwobble.mammoth.@internal;
using Mammoth.Couscous.org.zwobble.mammoth.@internal.conversion;

namespace Mammoth {
    public class DocumentConverter {
        private readonly DocumentToHtmlOptions options;

        public DocumentConverter() : this(DocumentToHtmlOptions._DEFAULT) {
        }

        private DocumentConverter(DocumentToHtmlOptions options) {
            this.options = options;
        }

        public DocumentConverter IdPrefix(string idPrefix) {
            return new DocumentConverter(options.idPrefix(idPrefix));
        }

        public DocumentConverter PreserveEmptyParagraphs() {
            return new DocumentConverter(options.preserveEmptyParagraphs());
        }

        public DocumentConverter AddStyleMap(string styleMap) {
            return new DocumentConverter(options.addStyleMap(styleMap));
        }

        public DocumentConverter DisableDefaultStyleMap() {
            return new DocumentConverter(options.disableDefaultStyleMap());
        }

        public IResult<string> ConvertToHtml(Stream stream) {
            return new InternalDocumentConverter(options)
                .convertToHtml(ToJava.StreamToInputStream(stream))
                .ToResult();
        }

        public IResult<string> ConvertToHtml(string path) {
            return new InternalDocumentConverter(options)
                .convertToHtml(new Couscous.java.io.File(path))
                .ToResult();
        }

        public IResult<string> ExtractRawText(Stream stream) {
            return new InternalDocumentConverter(options)
                .extractRawText(ToJava.StreamToInputStream(stream))
                .ToResult();
        }

        public IResult<string> ExtractRawText(string path) {
            return new InternalDocumentConverter(options)
                .extractRawText(new Couscous.java.io.File(path))
                .ToResult();
        }
    }
}
