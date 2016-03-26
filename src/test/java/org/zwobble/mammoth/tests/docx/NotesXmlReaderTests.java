package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.documents.Note;
import org.zwobble.mammoth.internal.documents.NoteType;
import org.zwobble.mammoth.internal.docx.BodyXmlReader;
import org.zwobble.mammoth.internal.docx.NotesXmlReader;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;
import static org.zwobble.mammoth.tests.ResultMatchers.isInternalSuccess;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.PARAGRAPH;

public class NotesXmlReaderTests {
    private static final BodyXmlReader bodyReader = make(a(BodyXmlReaderMakers.bodyReader));

    @Test
    public void idAndBodyOfFootnoteAreRead() {
        XmlElement element = element("w:footnotes", list(
            element("w:footnote", map("w:id", "1"), list(
                element("w:p")))));

        NotesXmlReader reader = NotesXmlReader.footnote(bodyReader);
        InternalResult<List<Note>> notes = reader.readElement(element);

        assertThat(
            notes,
            isInternalSuccess(list(new Note(NoteType.FOOTNOTE, "1", list(make(a(PARAGRAPH)))))));
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

        NotesXmlReader reader = NotesXmlReader.footnote(bodyReader);
        InternalResult<List<Note>> notes = reader.readElement(element);

        assertThat(notes, isInternalSuccess(list()));
    }
}
