package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.NoteType;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.documents.Note;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static org.zwobble.mammoth.internal.util.MammothIterables.lazyMap;

public class NotesXmlReader {
    public static NotesXmlReader footnote(BodyXmlReader bodyReader) {
        return new NotesXmlReader(bodyReader, "footnote", NoteType.FOOTNOTE);
    }

    public static NotesXmlReader endnote(BodyXmlReader bodyReader) {
        return new NotesXmlReader(bodyReader, "endnote", NoteType.ENDNOTE);
    }

    private final BodyXmlReader bodyReader;
    private final String tagName;
    private final NoteType noteType;

    private NotesXmlReader(BodyXmlReader bodyReader, String tagName, NoteType noteType) {
        this.bodyReader = bodyReader;
        this.tagName = tagName;
        this.noteType = noteType;
    }

    public InternalResult<List<Note>> readElement(XmlElement element) {
        Iterable<XmlElement> elements = filter(element.findChildren("w:" + tagName), this::isNoteElement);
        return InternalResult.concat(lazyMap(elements, this::readNoteElement));
    }

    private boolean isNoteElement(XmlElement element) {
        return element.getAttributeOrNone("w:type")
            .map(type -> !isSeparatorType(type))
            .orElse(true);
    }

    private boolean isSeparatorType(String type) {
        return type.equals("continuationSeparator") || type.equals("separator");
    }

    private InternalResult<Note> readNoteElement(XmlElement element) {
        return bodyReader.readElements(element.children())
            .toResult()
            .map(children -> new Note(
                noteType,
                element.getAttribute("w:id"),
                children));
    }
}
