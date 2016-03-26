package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.Map;

import static org.zwobble.mammoth.internal.util.Maps.entry;
import static org.zwobble.mammoth.internal.util.Maps.toMap;

public class RelationshipsXml {
    public static Relationships readRelationshipsXmlElement(XmlElement element) {
        return new Relationships(toMap(
            element.findChildren("relationships:Relationship"),
            RelationshipsXml::readRelationship));
    }

    private static Map.Entry<String, Relationship> readRelationship(XmlElement element) {
        return entry(
            element.getAttribute("Id"),
            new Relationship(element.getAttribute("Target")));
    }
}
