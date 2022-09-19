package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementList;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Maps.entry;
import static org.zwobble.mammoth.internal.util.Maps.toMap;

public class NumberingXml {
    public static Numbering readNumberingXmlElement(XmlElement element, Styles styles) {
        Map<String, Numbering.AbstractNum> abstractNums = readAbstractNums(element.findChildren("w:abstractNum"));
        Map<String, Numbering.Num> nums = readNums(element.findChildren("w:num"));
        return new Numbering(abstractNums, nums, styles);
    }

    private static Map<String, Numbering.AbstractNum> readAbstractNums(XmlElementList children) {
        return toMap(children, NumberingXml::readAbstractNum);
    }

    private static Map.Entry<String, Numbering.AbstractNum> readAbstractNum(XmlElement element) {
        // TODO: in python-mammoth, we allow None here. Check whether that's actually possible or not
        String abstractNumId = element.getAttribute("w:abstractNumId");
        Numbering.AbstractNum abstractNum = new Numbering.AbstractNum(
            readAbstractNumLevels(element),
            element.findChildOrEmpty("w:numStyleLink").getAttributeOrNone("w:val")
        );
        return entry(abstractNumId, abstractNum);
    }

    private static Map<String, Numbering.AbstractNumLevel> readAbstractNumLevels(XmlElement element) {
      return toMap(element.findChildren("w:lvl"), levelElement -> readAbstractNumLevel(element, levelElement));
    }

    private static Map.Entry<String, Numbering.AbstractNumLevel> readAbstractNumLevel(XmlElement parentAbstractNum, XmlElement levelElement) {
      String levelIndex = levelElement.getAttribute("w:ilvl");
      Optional<String> numFmt = levelElement.findChildOrEmpty("w:numFmt").getAttributeOrNone("w:val");
      boolean isOrdered = !numFmt.equals(Optional.of("bullet"));
      Optional<String> paragraphStyleId = levelElement.findChildOrEmpty("w:pStyle").getAttributeOrNone("w:val");
      return entry(levelIndex, new Numbering.AbstractNumLevel(parentAbstractNum.getAttributeOrNone("w:abstractNumId"), levelIndex, isOrdered, paragraphStyleId));
    }

    private static Map<String, Numbering.Num> readNums(XmlElementList numElements) {
        return toMap(numElements, NumberingXml::readNum);
    }

    private static Map.Entry<String, Numbering.Num> readNum(XmlElement numElement) {
        // TODO: in python-mammoth, we allow None here. Check whether that's actually possible or not
        String numId = numElement.getAttribute("w:numId");
        Optional<String> abstractNumId = numElement.findChildOrEmpty("w:abstractNumId").getAttributeOrNone("w:val");
        return entry(numId, new Numbering.Num(abstractNumId));
    }
}
