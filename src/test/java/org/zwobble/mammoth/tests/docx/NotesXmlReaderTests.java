package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.documents.Note;
import org.zwobble.mammoth.internal.documents.NoteType;
import org.zwobble.mammoth.internal.docx.BodyXmlReader;
import org.zwobble.mammoth.internal.docx.NotesXmlReader;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalSuccess;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.paragraph;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;

public class NotesXmlReaderTests {
    private static final BodyXmlReader BODY_READER = bodyReader();

    @Test
    public void idAndBodyOfFootnoteAreRead() {
        XmlElement element = element("w:footnotes", list(
            element("w:footnote", map("w:id", "1"), list(
                element("w:p")))));

        NotesXmlReader reader = NotesXmlReader.footnote(BODY_READER);
        InternalResult<List<Note>> notes = reader.readElement(element);

        assertThat(
            notes,
            isInternalSuccess(list(new Note(NoteType.FOOTNOTE, "1", list(paragraph())))));
    }

    @Test
    public void continuationSeparatorIsIgnored() {
        assertFootnoteTypeIsIgnored("continuationSeparator");
    }

    @Test
    public void separatorIsIgnored() {
        assertFootnoteTypeIsIgnored("separator");
    }

    private void assertFootnoteTypeIsIgnored(String noteType) {
        XmlElement element = element("w:footnotes", list(
            element("w:footnote", map("w:id", "1", "w:type", noteType), list(
                element("w:p")))));

        NotesXmlReader reader = NotesXmlReader.footnote(BODY_READER);
        InternalResult<List<Note>> notes = reader.readElement(element);

        assertThat(notes, isInternalSuccess(list()));
    }
}
