package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.util.MammothOptionals;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementLike;
import org.zwobble.mammoth.xml.XmlNode;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Iterables.*;
import static org.zwobble.mammoth.util.MammothLists.list;

public class BodyXmlReader {
    private final Styles styles;
    private final Numbering numbering;

    public BodyXmlReader(Styles styles, Numbering numbering) {
        this.styles = styles;
        this.numbering = numbering;
    }

    public List<DocumentElement> readElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return list(new Text(element.innerText()));
            case "w:r":
                return list(new Run(readElements(element.children())));
            case "w:p":
                return list(readParagraph(element));

            case "w:pPr":
                return list();

            default:
                // TODO: emit warning
                return list();
        }
    }

    public List<DocumentElement> readElements(Iterable<XmlNode> nodes) {
        return ImmutableList.copyOf(
            concat(
                transform(
                    filter(nodes, XmlElement.class),
                    this::readElement)));
    }

    private Paragraph readParagraph(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:pPr");
        return new Paragraph(
            readParagraphStyle(properties),
            readNumbering(properties),
            readElements(element.children()));
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
