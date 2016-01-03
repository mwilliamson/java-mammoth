package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
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

    public List<Note> readElement(XmlElement element) {
        Iterable<XmlElement> elements = filter(element.findChildren("w:" + noteType), this::isNoteElement);
        return ImmutableList.copyOf(transform(elements, this::readNoteElement));
    }

    private boolean isNoteElement(XmlElement element) {
        return element.getAttributeOrNone("w:type")
            .map(type -> !isSeparatorType(type))
            .orElse(true);
    }

    private boolean isSeparatorType(String type) {
        return type.equals("continuationSeparator") || type.equals("separator");
    }

    private Note readNoteElement(XmlElement element) {
        return new Note(
            element.getAttribute("w:id"),
            bodyReader.readElements(element.children()));
    }
}
