package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.xml.XmlElement;

import java.util.Map;

import static com.google.common.collect.Maps.immutableEntry;
import static org.zwobble.mammoth.util.MammothMaps.toMap;

public class RelationshipsXml {
    public static Relationships readRelationshipsXmlElement(XmlElement element) {
        return new Relationships(toMap(
            element.findChildren("relationships:Relationship"),
            RelationshipsXml::readRelationship));
    }

    private static Map.Entry<String, Relationship> readRelationship(XmlElement element) {
        return immutableEntry(
            element.getAttribute("Id"),
            new Relationship(element.getAttribute("Target")));
    }
}
