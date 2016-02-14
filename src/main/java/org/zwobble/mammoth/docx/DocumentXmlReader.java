package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.documents.Notes;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.xml.XmlElement;

public class DocumentXmlReader {
    private final BodyXmlReader bodyReader;
    private final Notes notes;

    public DocumentXmlReader(BodyXmlReader bodyReader, Notes notes) {
        this.bodyReader = bodyReader;
        this.notes = notes;
    }

    public Result<Document> readElement(XmlElement element) {
        XmlElement body = element.findChild("w:body");
        return bodyReader.readElements(body.children())
            .toResult()
            .map(children -> new Document(children, notes));
    }
}
