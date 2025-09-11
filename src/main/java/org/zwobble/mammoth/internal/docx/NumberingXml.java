package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementList;

import java.util.HashMap;
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
        Map<String, Numbering.AbstractNumLevel> levels = new HashMap<>();

        // Some malformed documents define numbering levels without an index, and
        // reference the numbering using a w:numPr element without a w:ilvl child.
        // To handle such cases, we assume a level of 0 as a fallback.
        Optional<Numbering.AbstractNumLevel> levelWithoutIndex = Optional.empty();

        for (XmlElement levelElement : element.findChildren("w:lvl")) {
            Map.Entry<Optional<String>, Numbering.AbstractNumLevel> entry =
                readAbstractNumLevel(levelElement);

            if (entry.getKey().isPresent()) {
                levels.put(entry.getKey().get(), entry.getValue());
            } else {
                levelWithoutIndex = Optional.of(entry.getValue());
            }
        }

        if (levelWithoutIndex.isPresent() && !levels.containsKey(levelWithoutIndex.get().levelIndex())) {
            levels.put(levelWithoutIndex.get().levelIndex(), levelWithoutIndex.get());
        }

        return levels;
    }

    private static Map.Entry<Optional<String>, Numbering.AbstractNumLevel> readAbstractNumLevel(XmlElement element) {
        Optional<String> levelIndex = element.getAttributeOrNone("w:ilvl");
        Optional<String> numFmt = element.findChildOrEmpty("w:numFmt").getAttributeOrNone("w:val");
        boolean isOrdered = !numFmt.equals(Optional.of("bullet"));
        Optional<String> paragraphStyleId = element.findChildOrEmpty("w:pStyle").getAttributeOrNone("w:val");
        Numbering.AbstractNumLevel abstractNumLevel = new Numbering.AbstractNumLevel(
            levelIndex.orElse("0"),
            isOrdered,
            paragraphStyleId
        );
        return entry(levelIndex, abstractNumLevel);
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
