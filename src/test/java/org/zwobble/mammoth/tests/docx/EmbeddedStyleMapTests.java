package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.docx.*;
import org.zwobble.mammoth.internal.util.Streams;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class EmbeddedStyleMapTests {
    private static final String ORIGINAL_RELATIONSHIPS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings\" Target=\"settings.xml\"/>" +
        "</Relationships>";

    private static final String ORIGINAL_CONTENT_TYPES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
        "<Default Extension=\"png\" ContentType=\"image/png\"/>" +
        "</Types>";

    @Test
    public void embeddedStyleMapPreservesUnrelatedFiles() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertEquals("placeholder text", Streams.toString(Archives.getInputStream(archive, "placeholder")));
    }

    @Test
    public void embeddedStyleMapCanBeReadAfterBeingEmbedded() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertEquals(Optional.of("p => h1"), EmbeddedStyleMap.readStyleMap(archive));
    }

    private static MutableArchive normalDocx() {
        return InMemoryArchive.fromStrings(map(
            "placeholder", "placeholder text",
            "word/_rels/document.xml.rels", ORIGINAL_RELATIONSHIPS_XML,
            "[Content_Types].xml", ORIGINAL_CONTENT_TYPES_XML
        ));
    }

}
