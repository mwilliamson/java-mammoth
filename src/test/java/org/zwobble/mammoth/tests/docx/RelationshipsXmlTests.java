package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.docx.Relationships;
import org.zwobble.mammoth.internal.xml.XmlElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zwobble.mammoth.internal.docx.RelationshipsXml.readRelationshipsXmlElement;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class RelationshipsXmlTests {
    @Test
    public void relationshipTargetsCanBeFoundById() {
        XmlElement element = element("relationship:Relationships", list(
            element("relationships:Relationship", map(
                "Id", "rId8",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
                "Target", "http://example.com"
            )),
            element("relationships:Relationship", map(
                "Id", "rId2",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
                "Target", "http://example.net"
            ))
        ));

        Relationships relationships = readRelationshipsXmlElement(element);

        assertEquals("http://example.com", relationships.findTargetByRelationshipId("rId8"));
    }

    @Test
    public void relationshipTargetsCanBeFoundByType() {
        XmlElement element = element("relationship:Relationships", list(
            element("relationships:Relationship", map(
                "Id", "rId2",
                "Target", "docProps/core.xml",
                "Type", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties"
            )),
            element("relationships:Relationship", map(
                "Id", "rId1",
                "Target", "word/document.xml",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
            )),
            element("relationships:Relationship", map(
                "Id", "rId3",
                "Target", "word/document2.xml",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
            ))
        ));

        Relationships relationships = readRelationshipsXmlElement(element);

        assertEquals(
            list("word/document.xml", "word/document2.xml"),
            relationships.findTargetsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument")
        );
    }

    @Test
    public void whenThereAreNoRelationshipsOfRequestedTypeThenEmptyListIsReturned() {

        XmlElement element = element("relationship:Relationships");

        Relationships relationships = readRelationshipsXmlElement(element);

        assertEquals(
            list(),
            relationships.findTargetsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument")
        );
    }
}
