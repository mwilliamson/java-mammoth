package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.zwobble.mammoth.docx.Relationships;
import org.zwobble.mammoth.xml.XmlElement;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.docx.RelationshipsXml.readRelationshipsXmlElement;
import static org.zwobble.mammoth.util.MammothMaps.map;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class RelationshipsXmlTests {
    @Test
    public void targetIsReadFromRelationshipElement() {
        XmlElement element = element("relationship:Relationships", ImmutableList.of(
            element("relationships:Relationship", map(
                "Id", "rId8",
                "Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
                "Target", "http://example.com"))));

        Relationships relationships = readRelationshipsXmlElement(element);

        assertEquals("http://example.com", relationships.findRelationshipById("rId8").getTarget());
    }
}
