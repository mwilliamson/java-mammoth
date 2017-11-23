package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.docx.Numbering;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zwobble.mammoth.internal.docx.NumberingXml.readNumberingXmlElement;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class NumberingXmlTests {
    private final static XmlElement SAMPLE_NUMBERING_XML = element("w:numbering", list(
        element("w:abstractNum", map("w:abstractNumId", "42"), list(
            element("w:lvl", map("w:ilvl", "0"), list(
                element("w:numFmt", map("w:val", "bullet")))),
            element("w:lvl", map("w:ilvl", "1"), list(
                element("w:numFmt", map("w:val", "decimal")))))),
        element("w:num", map("w:numId", "47"), list(
            element("w:abstractNumId", map("w:val", "42"))))));

    @Test
    public void findLevelReturnsNoneIfNumWithIdCannotBeFound() {
        Numbering numbering = readNumberingXmlElement(element("w:numbering"));
        assertEquals(Optional.empty(), numbering.findLevel("47", "0"));
    }

    @Test
    public void levelIncludesLevelIndex() {
        Numbering numbering = readNumberingXmlElement(SAMPLE_NUMBERING_XML);
        assertEquals("0", numbering.findLevel("47", "0").get().getLevelIndex());
        assertEquals("1", numbering.findLevel("47", "1").get().getLevelIndex());
    }

    @Test
    public void listIsNotOrderedIfFormattedAsBullet() {
        Numbering numbering = readNumberingXmlElement(SAMPLE_NUMBERING_XML);
        assertEquals(false, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void listIsOrderedIfFormattedAsDecimal() {
        Numbering numbering = readNumberingXmlElement(SAMPLE_NUMBERING_XML);
        assertEquals(true, numbering.findLevel("47", "1").get().isOrdered());
    }
}
