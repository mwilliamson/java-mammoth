package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.NoteType;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.documents.Note;
import org.zwobble.mammoth.xml.XmlElement;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class NotesXmlReader {
    public static NotesXmlReader footnote(BodyXmlReader bodyReader) {
        return new NotesXmlReader(bodyReader, "footnote", NoteType.FOOTNOTE);
    }

    private final BodyXmlReader bodyReader;
    private final String tagName;
    private final NoteType noteType;

    private NotesXmlReader(BodyXmlReader bodyReader, String tagName, NoteType noteType) {
        this.bodyReader = bodyReader;
        this.tagName = tagName;
        this.noteType = noteType;
    }

    public Result<List<Note>> readElement(XmlElement element) {
        Iterable<XmlElement> elements = filter(element.findChildren("w:" + tagName), this::isNoteElement);
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
                noteType,
                element.getAttribute("w:id"),
                children));
    }
}
