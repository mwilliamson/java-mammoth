package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.documents.Notes;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementLike;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class DocumentXmlReader {
    private final BodyXmlReader bodyReader;
    private final Notes notes;

    public DocumentXmlReader(BodyXmlReader bodyReader, Notes notes) {
        this.bodyReader = bodyReader;
        this.notes = notes;
    }

    public InternalResult<Document> readElement(XmlElement element) {
        XmlElementLike body = element.findChildOrEmpty("w:body");
        return bodyReader.readElements(body.getChildren())
            .toResult()
            .map(children -> new Document(children, notes, list()));
    }
}
