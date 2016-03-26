package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.InputStreamSupplier;
import org.zwobble.mammoth.internal.util.MammothOptionals;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementLike;
import org.zwobble.mammoth.internal.xml.XmlElementList;
import org.zwobble.mammoth.internal.xml.XmlNode;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.docx.ReadResult.EMPTY_SUCCESS;
import static org.zwobble.mammoth.internal.docx.ReadResult.success;
import static org.zwobble.mammoth.internal.util.MammothIterables.lazyFilter;
import static org.zwobble.mammoth.internal.util.MammothLists.list;
import static org.zwobble.mammoth.internal.util.MammothSets.set;
import static org.zwobble.mammoth.internal.util.MammothStrings.trimLeft;

public class BodyXmlReader {
    private static final Set<String> IMAGE_TYPES_SUPPORTED_BY_BROWSERS = set(
        "image/png", "image/gif", "image/jpeg", "image/svg+xml", "image/tiff");

    private final Styles styles;
    private final Numbering numbering;
    private final Relationships relationships;
    private final ContentTypes contentTypes;
    private final DocxFile file;
    private final FileReader fileReader;

    public BodyXmlReader(
        Styles styles,
        Numbering numbering,
        Relationships relationships,
        ContentTypes contentTypes,
        DocxFile file,
        FileReader fileReader)
    {
        this.styles = styles;
        this.numbering = numbering;
        this.relationships = relationships;
        this.contentTypes = contentTypes;
        this.file = file;
        this.fileReader = fileReader;
    }

    public ReadResult readElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return success(new Text(element.innerText()));
            case "w:r":
                return readRun(element);
            case "w:p":
                return readParagraph(element);

            case "w:tab":
                return success(Tab.TAB);
            case "w:br":
                return readBreak(element);

            case "w:tbl":
                return readElements(element.children()).map(Table::new);
            case "w:tr":
                return readElements(element.children()).map(TableRow::new);
            case "w:tc":
                return readElements(element.children()).map(TableCell::new);

            case "w:hyperlink":
                return readHyperlink(element);
            case "w:bookmarkStart":
                return readBookmark(element);
            case "w:footnoteReference":
                return readNoteReference(NoteType.FOOTNOTE, element);
            case "w:endnoteReference":
                return readNoteReference(NoteType.ENDNOTE, element);

            case "w:pict":
                return readPict(element);

            case "v:imagedata":
                return readImagedata(element);

            case "wp:inline":
            case "wp:anchor":
                return readInline(element);

            case "w:ins":
            case "w:smartTag":
            case "w:drawing":
            case "v:roundrect":
            case "v:shape":
            case "v:textbox":
            case "w:txbxContent":
                return readElements(element.children());

            case "office-word:wrap":
            case "v:shadow":
            case "v:shapetype":
            case "w:bookmarkEnd":
            case "w:sectPr":
            case "w:proofErr":
            case "w:lastRenderedPageBreak":
            case "w:commentRangeStart":
            case "w:commentRangeEnd":
            case "w:commentReference":
            case "w:del":
            case "w:footnoteRef":
            case "w:endnoteRef":
            case "w:pPr":
            case "w:rPr":
            case "w:tblPr":
            case "w:tblGrid":
            case "w:tcPr":
                return EMPTY_SUCCESS;

            default:
                String warning = "An unrecognised element was ignored: " + element.getName();
                return ReadResult.emptyWithWarning(warning);
        }
    }

    private ReadResult readRun(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:rPr");
        return ReadResult.map(
            readRunStyle(properties),
            readElements(element.children()),
            (style, children) -> new Run(
                isBold(properties),
                isItalic(properties),
                isUnderline(properties),
                isStrikethrough(properties),
                readVerticalAlignment(properties),
                style,
                children));
    }

    private boolean isBold(XmlElementLike properties) {
        return properties.hasChild("w:b");
    }

    private boolean isItalic(XmlElementLike properties) {
        return properties.hasChild("w:i");
    }

    private boolean isUnderline(XmlElementLike properties) {
        return properties.hasChild("w:u");
    }

    private boolean isStrikethrough(XmlElementLike properties) {
        return properties.hasChild("w:strike");
    }

    private VerticalAlignment readVerticalAlignment(XmlElementLike properties) {
        String verticalAlignment = readVal(properties, "w:vertAlign").orElse("");
        switch (verticalAlignment) {
            case "superscript":
                return VerticalAlignment.SUPERSCRIPT;
            case "subscript":
                return VerticalAlignment.SUBSCRIPT;
            default:
                // TODO: warn if set?
                return VerticalAlignment.BASELINE;
        }
    }

    private InternalResult<Optional<Style>> readRunStyle(XmlElementLike properties) {
        return readStyle(properties, "w:rStyle", "Run", styles::findCharacterStyleById);
    }

    public ReadResult readElements(Iterable<XmlNode> nodes) {
        return ReadResult.flatMap(lazyFilter(nodes, XmlElement.class), this::readElement);
    }

    private ReadResult readParagraph(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:pPr");
        Optional<NumberingLevel> numbering = readNumbering(properties);
        return ReadResult.map(
            readParagraphStyle(properties),
            readElements(element.children()),
            (style, children) -> new Paragraph(style, numbering, children)).appendExtra();
    }

    private InternalResult<Optional<Style>> readParagraphStyle(XmlElementLike properties) {
        return readStyle(properties, "w:pStyle", "Paragraph", styles::findParagraphStyleById);
    }

    private InternalResult<Optional<Style>> readStyle(
        XmlElementLike properties,
        String styleTagName,
        String styleType,
        Function<String, Optional<Style>> findStyleById)
    {
        return readVal(properties, styleTagName)
            .map(styleId -> findStyleById(styleType, styleId, findStyleById))
            .orElse(InternalResult.empty());
    }

    private InternalResult<Optional<Style>> findStyleById(
        String styleType,
        String styleId,
        Function<String, Optional<Style>> findStyleById)
    {
        Optional<Style> style = findStyleById.apply(styleId);
        if (style.isPresent()) {
            return InternalResult.success(style);
        } else {
            return new InternalResult<>(
                Optional.of(new Style(styleId, Optional.empty())),
                list(styleType + " style with ID " + styleId + " was referenced but not defined in the document"));
        }

    }

    private Optional<NumberingLevel> readNumbering(XmlElementLike properties) {
        XmlElementLike numberingProperties = properties.findChildOrEmpty("w:numPr");
        return MammothOptionals.flatMap(
            readVal(numberingProperties, "w:numId"),
            readVal(numberingProperties, "w:ilvl"),
            numbering::findLevel);
    }

    private ReadResult readBreak(XmlElement element) {
        String breakType = element.getAttributeOrNone("w:type").orElse("");
        if (breakType.equals("")) {
            return success(LineBreak.LINE_BREAK);
        } else {
            return ReadResult.emptyWithWarning("Unsupported break type: " + breakType);
        }
    }

    private ReadResult readHyperlink(XmlElement element) {
        Optional<String> relationshipId = element.getAttributeOrNone("r:id");
        Optional<String> anchor = element.getAttributeOrNone("w:anchor");
        ReadResult childrenResult = readElements(element.children());
        if (relationshipId.isPresent()) {
            return childrenResult.map(children -> Hyperlink.href(
                relationships.findRelationshipById(relationshipId.get()).getTarget(), children));
        } else if (anchor.isPresent()) {
            return childrenResult.map(children -> Hyperlink.anchor(
                anchor.get(), children));
        } else {
            return childrenResult;
        }
    }

    private ReadResult readBookmark(XmlElement element) {
        String name = element.getAttribute("w:name");
        if (name.equals("_GoBack")) {
            return ReadResult.EMPTY_SUCCESS;
        } else {
            return success(new Bookmark(name));
        }
    }

    private ReadResult readNoteReference(NoteType noteType, XmlElement element) {
        String noteId = element.getAttribute("w:id");
        return success(new NoteReference(noteType, noteId));
    }

    private ReadResult readPict(XmlElement element) {
        return readElements(element.children()).toExtra();
    }

    private ReadResult readImagedata(XmlElement element) {
        Optional<String> title = element.getAttributeOrNone("o:title");
        String relationshipId = element.getAttribute("r:id");
        String imagePath = relationshipIdToDocxPath(relationshipId);
        return readImage(imagePath, title, () -> DocxFile.getInputStream(file, imagePath));
    }

    private ReadResult readInline(XmlElement element) {
        Optional<String> altText = element.findChildOrEmpty("wp:docPr").getAttributeOrNone("descr");
        XmlElementList blips = element.findChildren("a:graphic")
            .findChildren("a:graphicData")
            .findChildren("pic:pic")
            .findChildren("pic:blipFill")
            .findChildren("a:blip");
        return readBlips(blips, altText);
    }

    private ReadResult readBlips(XmlElementList blips, Optional<String> altText) {
        return ReadResult.flatMap(blips, blip -> readBlip(blip, altText));
    }

    private ReadResult readBlip(XmlElement blip, Optional<String> altText) {
        Optional<String> embedRelationshipId = blip.getAttributeOrNone("r:embed");
        Optional<String> linkRelationshipId = blip.getAttributeOrNone("r:link");
        if (embedRelationshipId.isPresent()) {
            String imagePath = relationshipIdToDocxPath(embedRelationshipId.get());
            return readImage(imagePath, altText, () -> DocxFile.getInputStream(file, imagePath));
        } else if (linkRelationshipId.isPresent()) {
            String imagePath = relationships.findRelationshipById(linkRelationshipId.get()).getTarget();
            return readImage(imagePath, altText, () -> fileReader.getInputStream(imagePath));
        } else {
            // TODO: emit warning
            return ReadResult.EMPTY_SUCCESS;
        }
    }

    private ReadResult readImage(String imagePath, Optional<String> altText, InputStreamSupplier open) {
        Optional<String> contentType = contentTypes.findContentType(imagePath);
        Image image = new Image(altText, contentType, open);

        String contentTypeString = contentType.orElse("(unknown)");
        if (IMAGE_TYPES_SUPPORTED_BY_BROWSERS.contains(contentTypeString)) {
            return success(image);
        } else {
            return ReadResult.withWarning(image, "Image of type " + contentTypeString + " is unlikely to display in web browsers");
        }
    }

    private String relationshipIdToDocxPath(String relationshipId) {
        Relationship relationship = relationships.findRelationshipById(relationshipId);
        return "word/" + trimLeft(relationship.getTarget(), '/');
    }

    private Optional<String> readVal(XmlElementLike element, String name) {
        return element.findChildOrEmpty(name).getAttributeOrNone("w:val");
    }
}
