package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.archives.Archives;
import org.zwobble.mammoth.internal.archives.InMemoryArchive;
import org.zwobble.mammoth.internal.archives.MutableArchive;
import org.zwobble.mammoth.internal.docx.EmbeddedStyleMap;
import org.zwobble.mammoth.internal.util.Streams;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlNode;
import org.zwobble.mammoth.internal.xml.XmlNodes;
import org.zwobble.mammoth.internal.xml.parsing.XmlParser;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class EmbeddedStyleMapTests {
    private static final String ORIGINAL_RELATIONSHIPS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings\" Target=\"settings.xml\"/>" +
        "</Relationships>";

    private static final XmlElement EXPECTED_RELATIONSHIPS_XML = XmlNodes.element("Relationships", list(
        XmlNodes.element("Relationship", map(
            "Id", "rId3",
            "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings",
            "Target", "settings.xml"
        )),
        XmlNodes.element("Relationship", map(
            "Id", "rMammothStyleMap",
            "Type", "http://schemas.zwobble.org/mammoth/style-map",
            "Target", "/mammoth/style-map"
        ))
    ));

    private static final String ORIGINAL_CONTENT_TYPES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
        "<Default Extension=\"png\" ContentType=\"image/png\"/>" +
        "</Types>";

    private static final XmlElement EXPECTED_CONTENT_TYPES_XML = XmlNodes.element("Types", list(
        XmlNodes.element("Default", map("Extension", "png", "ContentType", "image/png")),
        XmlNodes.element("Override", map("PartName", "/mammoth/style-map", "ContentType", "text/prs.mammoth.style-map"))
    ));

    @Test
    public void embeddedStyleMapPreservesUnrelatedFiles() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertEquals("placeholder text", readString(archive, "placeholder"));
    }

    @Test
    public void embeddedStyleMapCanBeReadAfterBeingEmbedded() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertEquals(Optional.of("p => h1"), EmbeddedStyleMap.readStyleMap(archive));
    }

    @Test
    public void embeddedStyleMapIsReferencedInRelationships() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertThat(readRelationships(archive), deepEquals(EXPECTED_RELATIONSHIPS_XML));
    }

    @Test
    public void embeddedStyleMapHasOverrideContentTypeInContentTypesXml() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertThat(readContentTypes(archive), deepEquals(EXPECTED_CONTENT_TYPES_XML));
    }

    @Test
    public void canOverwriteExistingStyleMap() throws IOException {
        MutableArchive archive = normalDocx();
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        EmbeddedStyleMap.embedStyleMap(archive, "p => h1");
        assertEquals(Optional.of("p => h1"), EmbeddedStyleMap.readStyleMap(archive));
        assertThat(readRelationships(archive), deepEquals(EXPECTED_RELATIONSHIPS_XML));
        assertThat(readContentTypes(archive), deepEquals(EXPECTED_CONTENT_TYPES_XML));
    }

    private XmlNode readRelationships(MutableArchive archive) throws IOException {
        return new XmlParser(EmbeddedStyleMap.RELATIONSHIPS_NAMESPACES)
            .parseString(readString(archive, "word/_rels/document.xml.rels"));
    }

    private XmlNode readContentTypes(MutableArchive archive) throws IOException {
        return new XmlParser(EmbeddedStyleMap.CONTENT_TYPES_NAMESPACES)
            .parseString(readString(archive, "[Content_Types].xml"));
    }

    private static MutableArchive normalDocx() {
        return InMemoryArchive.fromStrings(map(
            "placeholder", "placeholder text",
            "word/_rels/document.xml.rels", ORIGINAL_RELATIONSHIPS_XML,
            "[Content_Types].xml", ORIGINAL_CONTENT_TYPES_XML
        ));
    }

    private String readString(MutableArchive archive, String path) throws IOException {
        return Streams.toString(Archives.getInputStream(archive, path));
    }
}
