package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.Map;

import static org.zwobble.mammoth.internal.util.Lists.eagerMap;
import static org.zwobble.mammoth.internal.util.Maps.entry;
import static org.zwobble.mammoth.internal.util.Maps.toMap;

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
        return new Relationship(relationshipId, target);
    }
}
