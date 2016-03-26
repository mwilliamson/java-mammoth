package org.zwobble.mammoth.tests.docx;

import org.junit.Test;
import org.zwobble.mammoth.internal.docx.Relationships;
import org.zwobble.mammoth.internal.xml.XmlElement;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.internal.docx.RelationshipsXml.readRelationshipsXmlElement;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class RelationshipsXmlTests {
    @Test
    public void targetIsReadFromRelationshipElement() {
        XmlElement element = element("relationship:Relationships", list(
            element("relationships:Relationship", map(
                "Id", "rId8",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
                "Target", "http://example.com"))));

        Relationships relationships = readRelationshipsXmlElement(element);

        assertEquals("http://example.com", relationships.findRelationshipById("rId8").getTarget());
    }
}
