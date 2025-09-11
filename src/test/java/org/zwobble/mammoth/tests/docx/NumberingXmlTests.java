package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.documents.NumberingStyle;
import org.zwobble.mammoth.internal.docx.Numbering;
import org.zwobble.mammoth.internal.docx.Styles;
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
        Numbering numbering = readNumbering(element("w:numbering"));
        assertEquals(Optional.empty(), numbering.findLevel("47", "0"));
    }

    @Test
    public void levelIncludesLevelIndex() {
        Numbering numbering = readNumbering(SAMPLE_NUMBERING_XML);
        assertEquals("0", numbering.findLevel("47", "0").get().getLevelIndex());
        assertEquals("1", numbering.findLevel("47", "1").get().getLevelIndex());
    }

    @Test
    public void listIsNotOrderedIfFormattedAsBullet() {
        Numbering numbering = readNumbering(SAMPLE_NUMBERING_XML);
        assertEquals(false, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void listIsOrderedIfFormattedAsDecimal() {
        Numbering numbering = readNumbering(SAMPLE_NUMBERING_XML);
        assertEquals(true, numbering.findLevel("47", "1").get().isOrdered());
    }

    @Test
    public void listIsOrderedIfThereIsNotExplicitFormat() {
        XmlElement element = element("w:numbering", list(
            element("w:abstractNum", map("w:abstractNumId", "42"), list(
                element("w:lvl", map("w:ilvl", "0"))
            )),
            element("w:num", map("w:numId", "47"), list(
                element("w:abstractNumId", map("w:val", "42"))
            ))
        ));

        Numbering numbering = readNumbering(element);

        assertEquals(true, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void numReferencingNonExistentAbstractNumIsIgnored() {
        XmlElement element = element("w:numbering", list(
            element("w:num", map("w:numId", "47"), list(
                element("w:abstractNumId", map("w:val", "42"))
            ))
        ));

        Numbering numbering = readNumbering(element);

        assertEquals(Optional.empty(), numbering.findLevel("47", "0"));
    }

    @Test
    public void givenNoOtherLevelsWithIndexOf0WhenLevelIsMissingIlvlThenLevelIndexIs0() {
        XmlElement element = element("w:numbering", list(
            element("w:abstractNum", map("w:abstractNumId", "42"), list(
                element("w:lvl", map(), list(
                    element("w:numFmt", map("w:val", "decimal"))
                ))
            )),
            element("w:num", map("w:numId", "47"), list(
                element("w:abstractNumId", map("w:val", "42"))
            ))
        ));

        Numbering numbering = readNumbering(element);

        assertEquals(true, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void givenPreviousOtherLevelWithIndexOf0WhenLevelIsMissingIlvlThenLevelIsIgnored() {
        XmlElement element = element("w:numbering", list(
            element("w:abstractNum", map("w:abstractNumId", "42"), list(
                element("w:lvl", map("w:ilvl", "0"), list(
                    element("w:numFmt", map("w:val", "bullet"))
                )),
                element("w:lvl", map(), list(
                    element("w:numFmt", map("w:val", "decimal"))
                ))
            )),
            element("w:num", map("w:numId", "47"), list(
                element("w:abstractNumId", map("w:val", "42"))
            ))
        ));

        Numbering numbering = readNumbering(element);

        assertEquals(false, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void givenSubsequentOtherLevelWithIndexOf0WhenLevelIsMissingIlvlThenLevelIsIgnored() {
        XmlElement element = element("w:numbering", list(
            element("w:abstractNum", map("w:abstractNumId", "42"), list(
                element("w:lvl", map(), list(
                    element("w:numFmt", map("w:val", "decimal"))
                )),
                element("w:lvl", map("w:ilvl", "0"), list(
                    element("w:numFmt", map("w:val", "bullet"))
                ))
            )),
            element("w:num", map("w:numId", "47"), list(
                element("w:abstractNumId", map("w:val", "42"))
            ))
        ));

        Numbering numbering = readNumbering(element);

        assertEquals(false, numbering.findLevel("47", "0").get().isOrdered());
    }

    @Test
    public void whenAbstractNumHasNumStyleLinkThenStyleIsUsedToFindNum() {
        Numbering numbering = readNumberingXmlElement(
            element("w:numbering", list(
                element("w:abstractNum", map("w:abstractNumId", "100"), list(
                    element("w:lvl", map("w:ilvl", "0"), list(
                        element("w:numFmt", map("w:val", "decimal"))
                    ))
                )),
                element("w:abstractNum", map("w:abstractNumId", "101"), list(
                    element("w:numStyleLink", map("w:val", "List1"))
                )),
                element("w:num", map("w:numId", "200"), list(
                    element("w:abstractNumId", map("w:val", "100"))
                )),
                element("w:num", map("w:numId", "201"), list(
                    element("w:abstractNumId", map("w:val", "101"))
                ))
            )),
            new Styles(
                map(),
                map(),
                map(),
                map("List1", new NumberingStyle(Optional.of("200")))
            )
        );
        assertEquals(true, numbering.findLevel("201", "0").get().isOrdered());
    }

    // See: 17.9.23 pStyle (Paragraph Style's Associated Numbering Level) in ECMA-376, 4th Edition
    @Test
    public void numberingLevelCanBeFoundByParagraphStyleId() {
        Numbering numbering = readNumbering(
            element("w:numbering", list(
                element("w:abstractNum", map("w:abstractNumId", "42"), list(
                    element("w:lvl", map("w:ilvl", "0"), list(
                        element("w:numFmt", map("w:val", "bullet"))
                    ))
                )),
                element("w:abstractNum", map("w:abstractNumId", "43"), list(
                    element("w:lvl", map("w:ilvl", "0"), list(
                        element("w:pStyle", map("w:val", "List")),
                        element("w:numFmt", map("w:val", "decimal"))
                    ))
                ))
            ))
        );
        assertEquals(true, numbering.findLevelByParagraphStyleId("List").get().isOrdered());
        assertEquals(false, numbering.findLevelByParagraphStyleId("Paragraph").isPresent());
    }


    private Numbering readNumbering(XmlElement element) {
        return readNumberingXmlElement(element, Styles.EMPTY);
    }
}
