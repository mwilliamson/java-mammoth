package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.zwobble.mammoth.documents.Note;
import org.zwobble.mammoth.documents.Paragraph;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.NotesXmlReader;
import org.zwobble.mammoth.xml.XmlElement;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class NotesXmlReaderTests {
    @Test
    public void idAndBodyOfFootnoteAreRead() {
        XmlElement element = element("w:footnotes", ImmutableList.of(
            element("w:footnote", ImmutableMap.of("w:id", "1"), ImmutableList.of(
                element("w:p")))));

        NotesXmlReader reader = new NotesXmlReader(new BodyXmlReader(null), "footnote");
        List<Note> notes = reader.readElement(element);

        assertThat(
            notes,
            deepEquals(ImmutableList.of(new Note("1", ImmutableList.of(new Paragraph(Optional.empty(), ImmutableList.of()))))));
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
        XmlElement element = element("w:footnotes", ImmutableList.of(
            element("w:footnote", ImmutableMap.of("w:id", "1", "w:type", noteType), ImmutableList.of(
                element("w:p")))));

        NotesXmlReader reader = new NotesXmlReader(new BodyXmlReader(null), "footnote");
        List<Note> notes = reader.readElement(element);

        assertThat(notes, deepEquals(ImmutableList.of()));
    }
}
