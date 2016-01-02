package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.Style;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementList;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.immutableEntry;
import static org.zwobble.mammoth.util.MammothMaps.toMap;

public class StylesXml {
    public static Styles readStylesXmlElement(XmlElement element) {
        XmlElementList styleElements = element.findChildren("w:style");
        return new Styles(
            readStyles(styleElements, "paragraph"),
            readStyles(styleElements, "character"));
    }

    private static Map<String, Style> readStyles(XmlElementList styleElements, String styleType) {
        return toMap(
            filter(styleElements, styleElement -> styleElement.getAttribute("w:type").equals(styleType)),
            StylesXml::readStyle);
    }

    private static Map.Entry<String, Style> readStyle(XmlElement element) {
        String styleId = element.getAttribute("w:styleId");
        Optional<String> styleName = element.findChildOrEmpty("w:name").getAttributeOrNone("w:val");
        return immutableEntry(styleId, new Style(styleId, styleName));
    }
}
