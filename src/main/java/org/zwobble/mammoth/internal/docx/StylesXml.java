package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.NumberingStyle;
import org.zwobble.mammoth.internal.documents.Style;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementList;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Iterables.lazyFilter;
import static org.zwobble.mammoth.internal.util.Maps.*;

public class StylesXml {
    public static Styles readStylesXmlElement(XmlElement element) {
        XmlElementList styleElements = element.findChildren("w:style");
        return new Styles(
            readStyles(styleElements, "paragraph"),
            readStyles(styleElements, "character"),
            readStyles(styleElements, "table"),
            readNumberingStyles(styleElements)
        );
    }

    private static Map<String, Style> readStyles(XmlElementList styleElements, String styleType) {
        return toMapPreferFirst(
            styleElementsOfType(styleElements, styleType),
            StylesXml::readStyle
        );
    }

    private static Map.Entry<String, Style> readStyle(XmlElement element) {
        String styleId = readStyleId(element);
        Optional<String> styleName = element.findChildOrEmpty("w:name").getAttributeOrNone("w:val");
        return entry(styleId, new Style(styleId, styleName));
    }

    private static Map<String, NumberingStyle> readNumberingStyles(XmlElementList styleElements) {
        return toMapPreferFirst(
            styleElementsOfType(styleElements, "numbering"),
            StylesXml::readNumberingStyle
        );
    }

    private static Map.Entry<String, NumberingStyle> readNumberingStyle(XmlElement element) {
        String styleId = readStyleId(element);
        Optional<String> numId = element
            .findChildOrEmpty("w:pPr")
            .findChildOrEmpty("w:numPr")
            .findChildOrEmpty("w:numId")
            .getAttributeOrNone("w:val");
        return entry(styleId, new NumberingStyle(numId));
    }

    private static String readStyleId(XmlElement element) {
        return element.getAttribute("w:styleId");
    }

    private static Iterable<XmlElement> styleElementsOfType(XmlElementList styleElements, String styleType) {
        return lazyFilter(styleElements, styleElement -> isStyleType(styleElement, styleType));
    }

    private static boolean isStyleType(XmlElement styleElement, String styleType) {
        return styleElement.getAttribute("w:type").equals(styleType);
    }
}
