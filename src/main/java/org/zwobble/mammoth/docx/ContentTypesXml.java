package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementList;

import java.util.Map;

import static com.google.common.collect.Maps.immutableEntry;
import static org.zwobble.mammoth.util.MammothMaps.toMap;
import static org.zwobble.mammoth.util.MammothStrings.trimLeft;

public class ContentTypesXml {
    public static ContentTypes readContentTypesXmlElement(XmlElement element) {
        return new ContentTypes(
            readDefaults(element.findChildren("content-types:Default")),
            readOverrides(element.findChildren("content-types:Override")));
    }

    private static Map<String, String> readDefaults(XmlElementList children) {
        return toMap(children, ContentTypesXml::readDefault);
    }

    private static Map.Entry<String, String> readDefault(XmlElement element) {
        return immutableEntry(
            element.getAttribute("Extension"),
            element.getAttribute("ContentType"));
    }

    private static Map<String, String> readOverrides(XmlElementList children) {
        return toMap(children, ContentTypesXml::readOverride);
    }

    private static Map.Entry<String, String> readOverride(XmlElement element) {
        return immutableEntry(
            trimLeft(element.getAttribute("PartName"), '/'),
            element.getAttribute("ContentType"));
    }
}
