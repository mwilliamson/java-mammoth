package org.zwobble.mammoth.docx;

import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.util.MammothOptionals;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementLike;
import org.zwobble.mammoth.xml.XmlNode;

import java.util.Optional;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.mammoth.docx.ReadResult.EMPTY_SUCCESS;
import static org.zwobble.mammoth.docx.ReadResult.success;

public class BodyXmlReader {
    private final Styles styles;
    private final Numbering numbering;

    public BodyXmlReader(Styles styles, Numbering numbering) {
        this.styles = styles;
        this.numbering = numbering;
    }

    public ReadResult readElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return success(new Text(element.innerText()));
            case "w:r":
                return readElements(element.children()).map(Run::new);
            case "w:p":
                return readParagraph(element);

            case "w:pPr":
                return EMPTY_SUCCESS;

            default:
                // TODO: emit warning
                return EMPTY_SUCCESS;
        }
    }

    public ReadResult readElements(Iterable<XmlNode> nodes) {
        return ReadResult.concat(
            transform(
                filter(nodes, XmlElement.class),
                this::readElement));
    }

    private ReadResult readParagraph(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:pPr");
        Optional<Style> paragraphStyle = readParagraphStyle(properties);
        Optional<NumberingLevel> numbering = readNumbering(properties);
        return readElements(element.children())
            .map(children -> new Paragraph(paragraphStyle, numbering, children));
    }

    private Optional<Style> readParagraphStyle(XmlElementLike properties) {
        return readVal(properties, "w:pStyle")
            .map(styleId -> styles.findParagraphStyleById(styleId)
                .orElse(new Style(styleId, Optional.empty())));
    }

    private Optional<NumberingLevel> readNumbering(XmlElementLike properties) {
        XmlElementLike numberingProperties = properties.findChildOrEmpty("w:numPr");
        return MammothOptionals.flatMap(
            readVal(numberingProperties, "w:numId"),
            readVal(numberingProperties, "w:ilvl"),
            numbering::findLevel);
    }

    private Optional<String> readVal(XmlElementLike element, String name) {
        return element.findChildOrEmpty(name).getAttributeOrNone("w:val");
    }
}
