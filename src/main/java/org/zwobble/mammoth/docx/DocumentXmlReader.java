package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.xml.XmlElement;

public class DocumentXmlReader {
    private final BodyXmlReader bodyReader;

    public DocumentXmlReader(BodyXmlReader bodyReader) {
        this.bodyReader = bodyReader;
    }

    public Result<Document> readElement(XmlElement element) {
        XmlElement body = element.findChild("w:body");
        return bodyReader.readElements(body.children())
            .toResult()
            .map(Document::new);
    }
}
