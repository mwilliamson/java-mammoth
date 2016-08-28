package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Comment;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.List;
import java.util.Optional;

public class CommentXmlReader {
    private final BodyXmlReader bodyReader;

    public CommentXmlReader(BodyXmlReader bodyReader) {
        this.bodyReader = bodyReader;
    }

    public InternalResult<List<Comment>> readElement(XmlElement element) {
        return InternalResult.flatMap(
            element.findChildren("w:comment"),
            this::readCommentElement
        );
    }

    private InternalResult<Comment> readCommentElement(XmlElement element) {
        String commentId = element.getAttribute("w:id");
        return bodyReader.readElements(element.getChildren())
            .toResult()
            .map(children -> new Comment(
                commentId,
                children,
                readOptionalAttribute(element, "w:author"),
                readOptionalAttribute(element, "w:initials")
            ));
    }

    private Optional<String> readOptionalAttribute(XmlElement element, String name) {
        String value = element.getAttributeOrNone(name).orElse("").trim();
        if (value.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }
}
