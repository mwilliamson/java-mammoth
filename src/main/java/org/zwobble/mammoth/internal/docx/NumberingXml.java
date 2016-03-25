package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementList;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothMaps.entry;
import static org.zwobble.mammoth.internal.util.MammothMaps.lookup;
import static org.zwobble.mammoth.internal.util.MammothMaps.toMap;

public class NumberingXml {
    public static Numbering readNumberingXmlElement(XmlElement element) {
        Map<String, Map<String, NumberingLevel>> abstractNums = readAbstractNums(element.findChildren("w:abstractNum"));
        return new Numbering(readNums(element.findChildren("w:num"), abstractNums));
    }

    private static Map<String, Map<String, NumberingLevel>> readAbstractNums(XmlElementList children) {
        return toMap(children, NumberingXml::readAbstractNum);
    }

    private static Map.Entry<String, Map<String, NumberingLevel>> readAbstractNum(XmlElement element) {
        // TODO: in python-mammoth, we allow None here. Check whether that's actually possible or not
        String abstractNumId = element.getAttribute("w:abstractNumId");
        return entry(abstractNumId, readAbstractNumLevels(element));
    }

    private static Map<String, NumberingLevel> readAbstractNumLevels(XmlElement element) {
        return toMap(element.findChildren("w:lvl"), NumberingXml::readAbstractNumLevel);
    }

    private static Map.Entry<String, NumberingLevel> readAbstractNumLevel(XmlElement element) {
        String levelIndex = element.getAttribute("w:ilvl");
        Optional<String> numFmt = element.findChildOrEmpty("w:numFmt").getAttributeOrNone("w:val");
        boolean isOrdered = !numFmt.equals(Optional.of("bullet"));
        return entry(levelIndex, new NumberingLevel(levelIndex, isOrdered));
    }

    private static Map<String, Map<String, NumberingLevel>> readNums(
        XmlElementList numElements,
        Map<String, Map<String, NumberingLevel>> abstractNums
    ) {
        return toMap(numElements, numElement -> readNum(numElement, abstractNums));
    }

    private static Map.Entry<String,Map<String,NumberingLevel>> readNum(
        XmlElement numElement,
        Map<String, Map<String, NumberingLevel>> abstractNums
    ) {
        // TODO: in python-mammoth, we allow None here. Check whether that's actually possible or not
        String numId = numElement.getAttribute("w:numId");
        String abstractNumId = numElement.findChild("w:abstractNumId").getAttribute("w:val");
        return entry(numId, lookup(abstractNums, abstractNumId).get());
    }
}
