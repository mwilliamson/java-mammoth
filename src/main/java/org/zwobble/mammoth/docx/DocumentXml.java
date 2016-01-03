package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.xml.XmlElement;

public class DocumentXml {
    private final BodyXml bodyReader;

    public DocumentXml(BodyXml bodyReader) {

        this.bodyReader = bodyReader;
    }

    public Document readDocumentXmlElement(XmlElement element) {
        XmlElement body = element.findChild("w:body");
        return new Document(bodyReader.readElements(body.children()));
    }
}
