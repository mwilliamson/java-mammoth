package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.docx.Styles;
import org.zwobble.mammoth.internal.xml.XmlElement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zwobble.mammoth.internal.docx.StylesXml.readStylesXmlElement;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.internal.xml.XmlNodes.element;

public class StylesXmlTests {
    @Test
    public void paragraphStyleIsNoneIfNoStyleWithThatIdExists() {
        XmlElement element = element("w:styles");

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.empty(), styles.findParagraphStyleById("Heading1"));
    }

    @Test
    public void paragraphStyleCanBeFoundById() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "paragraph", "w:styleId", "Heading1"), list(
                nameElement("Heading 1")))));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("Heading 1"), styles.findParagraphStyleById("Heading1").get().getName());
    }

    @Test
    public void characterStyleCanBeFoundById() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "character", "w:styleId", "Heading1Char"), list(
                nameElement("Heading 1 Char")))));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("Heading 1 Char"), styles.findCharacterStyleById("Heading1Char").get().getName());
    }

    @Test
    public void tableStyleCanBeFoundById() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "table", "w:styleId", "TableNormal"), list(
                nameElement("Normal Table")))));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("Normal Table"), styles.findTableStyleById("TableNormal").get().getName());
    }

    @Test
    public void paragraphAndCharacterStylesAreDistinct() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "paragraph", "w:styleId", "Heading1"), list(
                nameElement("Heading 1"))),
            element("w:style", map("w:type", "character", "w:styleId", "Heading1Char"), list(
                nameElement("Heading 1 Char")))));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.empty(), styles.findCharacterStyleById("Heading1"));
        assertEquals(Optional.empty(), styles.findParagraphStyleById("Heading1Char"));
    }

    @Test
    public void styleNameIsNoneIfNameElementDoesNotExist() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "paragraph", "w:styleId", "Heading1")),
            element("w:style", map("w:type", "character", "w:styleId", "Heading1Char"))));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.empty(), styles.findParagraphStyleById("Heading1").get().getName());
        assertEquals(Optional.empty(), styles.findCharacterStyleById("Heading1Char").get().getName());
    }

    @Test
    public void numberingStyleIsNoneIfNoStyleWithThatIdExists() {
        XmlElement element = element("w:styles");

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.empty(), styles.findNumberingStyleById("List1"));
    }

    @Test
    public void numberingStyleHasNoneNumIdIfStyleHasNoParagraphProperties() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "numbering", "w:styleId", "List1"))
        ));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.empty(), styles.findNumberingStyleById("List1").get().getNumId());
    }

    @Test
    public void numberingStyleHasNumIdReadFromParagraphProperties() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "numbering", "w:styleId", "List1"), list(
                element("w:pPr", list(
                    element("w:numPr", list(
                        element("w:numId", map("w:val", "42"))
                    ))
                ))
            ))
        ));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("42"), styles.findNumberingStyleById("List1").get().getNumId());
    }

    @Test
    public void whenMultipleNonNumberingStyleElementsHaveSameStyleIdThenOnlyFirstElementIsUsed() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "table", "w:styleId", "TableNormal"), list(
                nameElement("Normal Table")
            )),
            element("w:style", map("w:type", "table", "w:styleId", "TableNormal"), list(
                nameElement("Table Normal")
            ))
        ));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("Normal Table"), styles.findTableStyleById("TableNormal").get().getName());
    }

    @Test
    public void whenMultipleNumberingStyleElementsHaveSameStyleIdThenOnlyFirstElementIsUsed() {
        XmlElement element = element("w:styles", list(
            element("w:style", map("w:type", "numbering", "w:styleId", "List1"), list(
                element("w:pPr", list(
                    element("w:numPr", list(
                        element("w:numId", map("w:val", "42"))
                    ))
                ))
            )),
            element("w:style", map("w:type", "numbering", "w:styleId", "List1"), list(
                element("w:pPr", list(
                    element("w:numPr", list(
                        element("w:numId", map("w:val", "43"))
                    ))
                ))
            ))
        ));

        Styles styles = readStylesXmlElement(element);

        assertEquals(Optional.of("42"), styles.findNumberingStyleById("List1").get().getNumId());
    }

    private XmlElement nameElement(String name) {
        return element("w:name", map("w:val", name));
    }
}
