package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.Style;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementList;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Iterables.filter;
import static org.zwobble.mammoth.internal.util.MammothMaps.entry;
import static org.zwobble.mammoth.internal.util.MammothMaps.toMap;

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
        return entry(styleId, new Style(styleId, styleName));
    }
}
