package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementList;

import java.util.AbstractMap;
import java.util.Map;

import static org.zwobble.mammoth.util.MammothMaps.toMap;

public class ContentTypesXml {
    public static ContentTypes readContentTypesXmlElement(XmlElement element) {
        return new ContentTypes(readDefaults(element.findChildren("content-types:Default")));
    }

    private static Map<String, String> readDefaults(XmlElementList children) {
        return toMap(children, ContentTypesXml::readDefault);
    }

    private static Map.Entry<String, String> readDefault(XmlElement element) {
        return new AbstractMap.SimpleImmutableEntry<>(
            element.getAttribute("Extension"),
            element.getAttribute("ContentType"));
    }
}
