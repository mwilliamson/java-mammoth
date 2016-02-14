package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.documents.Document;
import org.zwobble.mammoth.documents.Note;
import org.zwobble.mammoth.documents.NoteType;
import org.zwobble.mammoth.documents.Notes;
import org.zwobble.mammoth.docx.DocumentXmlReader;
import org.zwobble.mammoth.results.Result;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlNodes;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.results.Result.success;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;
import static org.zwobble.mammoth.tests.documents.DocumentElementMakers.paragraphWithText;
import static org.zwobble.mammoth.tests.docx.BodyXmlReaderMakers.bodyReader;
import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class DocumentXmlTests {

    @Test
    public void canReadTextWithinDocument() {
        XmlElement documentElement = element("w:document", list(
            element("w:body", list(
                element("w:p", list(
                    element("w:r", list(
                        element("w:t", list(
                            XmlNodes.text("Hello!")))))))))));

        DocumentXmlReader reader = new DocumentXmlReader(make(a(bodyReader)), Notes.EMPTY);
        Result<Document> document = reader.readElement(documentElement);

        assertThat(
            document,
            deepEquals(success(new Document(
                list(paragraphWithText("Hello!")),
                Notes.EMPTY))));
    }

    @Test
    public void notesOfDocumentAreIncludedInDocument() {
        Note note = new Note(NoteType.FOOTNOTE, "4", list(paragraphWithText("Hello")));
        Notes notes = new Notes(list(note));
        DocumentXmlReader reader = new DocumentXmlReader(make(a(bodyReader)), notes);

        XmlElement documentElement = element("w:document", list(element("w:body")));
        Result<Document> document = reader.readElement(documentElement);

        assertThat(
            document.getValue().getNotes().findNote(NoteType.FOOTNOTE, "4").get(),
            deepEquals(note));
    }
}
