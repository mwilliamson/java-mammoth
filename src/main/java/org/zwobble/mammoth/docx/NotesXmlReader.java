package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.documents.Note;
import org.zwobble.mammoth.xml.XmlElement;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class NotesXmlReader {
    private final BodyXmlReader bodyReader;
    private final String noteType;

    public NotesXmlReader(BodyXmlReader bodyReader, String noteType) {
        this.bodyReader = bodyReader;
        this.noteType = noteType;
    }

    public Result<List<Note>> readElement(XmlElement element) {
        Iterable<XmlElement> elements = filter(element.findChildren("w:" + noteType), this::isNoteElement);
        return Result.concat(transform(elements, this::readNoteElement));
    }

    private boolean isNoteElement(XmlElement element) {
        return element.getAttributeOrNone("w:type")
            .map(type -> !isSeparatorType(type))
            .orElse(true);
    }

    private boolean isSeparatorType(String type) {
        return type.equals("continuationSeparator") || type.equals("separator");
    }

    private Result<Note> readNoteElement(XmlElement element) {
        return bodyReader.readElements(element.children())
            .toResult()
            .map(children -> new Note(
                element.getAttribute("w:id"),
                children));
    }
}
