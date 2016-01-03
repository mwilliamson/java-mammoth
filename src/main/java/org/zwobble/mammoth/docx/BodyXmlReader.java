package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableList;
import org.zwobble.mammoth.documents.*;
import org.zwobble.mammoth.xml.XmlElement;
import org.zwobble.mammoth.xml.XmlElementLike;
import org.zwobble.mammoth.xml.XmlNode;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Iterables.*;
import static org.zwobble.mammoth.util.MammothLists.list;

public class BodyXmlReader {
    private final Styles styles;

    public BodyXmlReader(Styles styles) {
        this.styles = styles;
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
        return new Paragraph(readParagraphStyle(element), readElements(element.children()));
    }

    private Optional<Style> readParagraphStyle(XmlElement paragraph) {
        XmlElementLike properties = paragraph.findChildOrEmpty("w:pPr");
        return properties.findChildOrEmpty("w:pStyle").getAttributeOrNone("w:val")
            .map(styleId -> styles.findParagraphStyleById(styleId).orElse(new Style(styleId, Optional.empty())));
    }
}
