package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Comment;
import org.zwobble.mammoth.internal.documents.Document;
import org.zwobble.mammoth.internal.documents.Notes;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.List;
import java.util.Optional;

public class DocumentXmlReader {
    private final BodyXmlReader bodyReader;
    private final Notes notes;
    private final List<Comment> comments;

    public DocumentXmlReader(BodyXmlReader bodyReader, Notes notes, List<Comment> comments) {
        this.bodyReader = bodyReader;
        this.notes = notes;
        this.comments = comments;
    }

    public InternalResult<Document> readElement(XmlElement element) {
        Optional<XmlElement> body = element.findChild("w:body");

        if (!body.isPresent()) {
            throw new IllegalArgumentException("Could not find the body element: are you sure this is a docx file?");
        }

        return bodyReader.readElements(body.get().getChildren())
            .toResult()
            .map(children -> new Document(children, notes, comments));
    }
}
