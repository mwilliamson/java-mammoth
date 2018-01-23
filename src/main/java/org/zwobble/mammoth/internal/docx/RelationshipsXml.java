package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.XmlElement;

import static org.zwobble.mammoth.internal.util.Lists.eagerMap;

public class RelationshipsXml {
    public static Relationships readRelationshipsXmlElement(XmlElement element) {
        return new Relationships(eagerMap(
            element.findChildren("relationships:Relationship"),
            RelationshipsXml::readRelationship
        ));
    }

    private static Relationship readRelationship(XmlElement element) {
        String relationshipId = element.getAttribute("Id");
        String target = element.getAttribute("Target");
        String type = element.getAttribute("Type");
        return new Relationship(relationshipId, target, type);
    }
}
