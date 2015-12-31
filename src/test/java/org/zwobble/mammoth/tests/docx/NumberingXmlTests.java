package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.zwobble.mammoth.docx.Numbering;
import org.zwobble.mammoth.xml.XmlElement;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.zwobble.mammoth.docx.NumberingXml.readNumberingXmlElement;
import static org.zwobble.mammoth.xml.XmlNodes.element;

public class NumberingXmlTests {
    private final static XmlElement SAMPLE_NUMBERING_XML = element("w:numbering", ImmutableList.of(
        element("w:abstractNum", ImmutableMap.of("w:abstractNumId", "42"), ImmutableList.of(
            element("w:lvl", ImmutableMap.of("w:ilvl", "0"), ImmutableList.of(
                element("w:numFmt", ImmutableMap.of("w:val", "bullet")))),
            element("w:lvl", ImmutableMap.of("w:ilvl", "1"), ImmutableList.of(
                element("w:numFmt", ImmutableMap.of("w:val", "decimal")))))),
        element("w:num", ImmutableMap.of("w:numId", "47"), ImmutableList.of(
            element("w:abstractNumId", ImmutableMap.of("w:val", "42"))))));

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
