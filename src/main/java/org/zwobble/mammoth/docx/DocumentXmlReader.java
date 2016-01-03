package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.xml.XmlElement;

public class DocumentXmlReader {
    private final BodyXmlReader bodyReader;

    public DocumentXmlReader(BodyXmlReader bodyReader) {
        this.bodyReader = bodyReader;
    }

    public Document readElement(XmlElement element) {
        XmlElement body = element.findChild("w:body");
        return new Document(bodyReader.readElements(body.children()));
    }
}
