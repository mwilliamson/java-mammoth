package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.Casts;
import org.zwobble.mammoth.internal.util.InputStreamSupplier;
import org.zwobble.mammoth.internal.util.Lists;
import org.zwobble.mammoth.internal.util.Optionals;
import org.zwobble.mammoth.internal.xml.XmlElement;
import org.zwobble.mammoth.internal.xml.XmlElementLike;
import org.zwobble.mammoth.internal.xml.XmlElementList;
import org.zwobble.mammoth.internal.xml.XmlNode;

import java.util.*;
import java.util.function.Function;

import static org.zwobble.mammoth.internal.docx.ReadResult.EMPTY_SUCCESS;
import static org.zwobble.mammoth.internal.docx.ReadResult.success;
import static org.zwobble.mammoth.internal.util.Iterables.lazyFilter;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.entry;
import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Sets.set;
import static org.zwobble.mammoth.internal.util.Strings.trimLeft;

public class BodyXmlReader {
    private static final Set<String> IMAGE_TYPES_SUPPORTED_BY_BROWSERS = set(
        "image/png", "image/gif", "image/jpeg", "image/svg+xml", "image/tiff");

    private final Styles styles;
    private final Numbering numbering;
    private final Relationships relationships;
    private final ContentTypes contentTypes;
    private final DocxFile file;
    private final FileReader fileReader;
    private final Map<String, String> bookmarkIdsToNames;

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
        this.bookmarkIdsToNames = new HashMap<>();
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
                return readTable(element);
            case "w:tr":
                return readElements(element.getChildren()).map(TableRow::new);
            case "w:tc":
                return readTableCell(element);

            case "w:hyperlink":
                return readHyperlink(element);
            case "w:bookmarkStart":
                return readBookmarkStart(element);
            case "w:bookmarkEnd":
                return readBookmarkEnd(element);
            case "w:footnoteReference":
                return readNoteReference(NoteType.FOOTNOTE, element);
            case "w:endnoteReference":
                return readNoteReference(NoteType.ENDNOTE, element);
            case "w:commentReference":
                return readCommentReference(element);

            case "w:pict":
                return readPict(element);

            case "v:imagedata":
                return readImagedata(element);

            case "wp:inline":
            case "wp:anchor":
                return readInline(element);

            case "w:sdt":
                return readSdt(element);

            case "w:ins":
            case "w:smartTag":
            case "w:drawing":
            case "v:roundrect":
            case "v:shape":
            case "v:textbox":
            case "w:txbxContent":
                return readElements(element.getChildren());

            case "office-word:wrap":
            case "v:shadow":
            case "v:shapetype":
            case "w:sectPr":
            case "w:proofErr":
            case "w:lastRenderedPageBreak":
            case "w:commentRangeStart":
            case "w:commentRangeEnd":
            case "w:del":
            case "w:footnoteRef":
            case "w:endnoteRef":
            case "w:annotationRef":
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
            readElements(element.getChildren()),
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
        return readBooleanElement(properties, "w:b");
    }

    private boolean isItalic(XmlElementLike properties) {
        return readBooleanElement(properties, "w:i");
    }

    private boolean isUnderline(XmlElementLike properties) {
        return readBooleanElement(properties, "w:u");
    }

    private boolean isStrikethrough(XmlElementLike properties) {
        return readBooleanElement(properties, "w:strike");
    }

    private boolean readBooleanElement(XmlElementLike properties, String tagName) {
        return properties.findChild(tagName)
            .map(child -> child.getAttributeOrNone("w:val")
                .map(value -> !value.equals("false") && !value.equals("0"))
                .orElse(true))
            .orElse(false);
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
            readElements(element.getChildren()),
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
        return Optionals.flatMap(
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

    private ReadResult readTable(XmlElement element) {
        return readElements(element.getChildren())
            .flatMap(this::calculateRowspans)
            .map(Table::new);
    }

    private ReadResult calculateRowspans(List<DocumentElement> rows) {
        Optional<String> error = checkTableRows(rows);
        if (error.isPresent()) {
            return ReadResult.withWarning(rows, error.get());
        }

        Map<Map.Entry<Integer, Integer>, Integer> rowspans = new HashMap<>();
        Set<Map.Entry<Integer, Integer>> merged = new HashSet<>();

        Map<Integer, Map.Entry<Integer, Integer>> lastCellForColumn = new HashMap<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex += 1) {
            TableRow row = (TableRow) rows.get(rowIndex);
            int columnIndex = 0;
            for (int cellIndex = 0; cellIndex < row.getChildren().size(); cellIndex += 1) {
                UnmergedTableCell cell = (UnmergedTableCell) row.getChildren().get(cellIndex);
                Optional<Map.Entry<Integer, Integer>> spanningCell = lookup(lastCellForColumn, columnIndex);
                Map.Entry<Integer, Integer> position = entry(rowIndex, cellIndex);
                if (cell.vmerge && spanningCell.isPresent()) {
                    rowspans.put(spanningCell.get(), lookup(rowspans, spanningCell.get()).get() + 1);
                    merged.add(position);
                } else {
                    lastCellForColumn.put(columnIndex, position);
                    rowspans.put(position, 1);
                }
                columnIndex += cell.colspan;
            }
        }

        return success(Lists.eagerMapWithIndex(rows, (rowIndex, rowElement) -> {
            TableRow row = (TableRow) rowElement;

            List<DocumentElement> mergedCells = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < row.getChildren().size(); cellIndex += 1) {
                UnmergedTableCell cell = (UnmergedTableCell) row.getChildren().get(cellIndex);
                Map.Entry<Integer, Integer> position = entry(rowIndex, cellIndex);
                if (!merged.contains(position)) {
                    mergedCells.add(new TableCell(
                        lookup(rowspans, position).get(),
                        cell.colspan,
                        cell.children
                    ));
                }
            }
            
            return new TableRow(mergedCells);
        }));
    }

    private Optional<String> checkTableRows(List<DocumentElement> rows) {
        for (DocumentElement rowElement : rows) {
            Optional<TableRow> row = Casts.tryCast(TableRow.class, rowElement);
            if (!row.isPresent()) {
                return Optional.of("unexpected non-row element in table, cell merging may be incorrect");
            } else {
                for (DocumentElement cell : row.get().getChildren()) {
                    if (!(cell instanceof UnmergedTableCell)) {
                        return Optional.of("unexpected non-cell element in table row, cell merging may be incorrect");
                    }
                }
            }
        }
        return Optional.empty();
    }

    private ReadResult readTableCell(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:tcPr");
        Optional<String> gridSpan = properties
            .findChildOrEmpty("w:gridSpan")
            .getAttributeOrNone("w:val");
        int colspan = gridSpan.map(Integer::parseInt).orElse(1);
        return readElements(element.getChildren())
            .map(children -> new UnmergedTableCell(readVmerge(properties), colspan, children));
    }

    private boolean readVmerge(XmlElementLike properties) {
        return properties.findChild("w:vMerge")
            .map(element -> element.getAttributeOrNone("w:val").map(val -> val.equals("continue")).orElse(true))
            .orElse(false);
    }

    private static class UnmergedTableCell implements DocumentElement {
        private final boolean vmerge;
        private final int colspan;
        private final List<DocumentElement> children;

        private UnmergedTableCell(boolean vmerge, int colspan, List<DocumentElement> children) {
            this.vmerge = vmerge;
            this.colspan = colspan;
            this.children = children;
        }

        @Override
        public <T> T accept(DocumentElementVisitor<T> visitor) {
            return visitor.visit(new TableCell(1, colspan, children));
        }
    }

    private ReadResult readHyperlink(XmlElement element) {
        Optional<String> relationshipId = element.getAttributeOrNone("r:id");
        Optional<String> anchor = element.getAttributeOrNone("w:anchor");
        ReadResult childrenResult = readElements(element.getChildren());
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

    private ReadResult readBookmarkStart(XmlElement element) {
        String name = element.getAttribute("w:name");
        String id = element.getAttribute("w:id");
        bookmarkIdsToNames.put(id, name);
        if (name.equals("_GoBack")) {
            return ReadResult.EMPTY_SUCCESS;
        } else {
            return success(new BookmarkStart(name));
        }
    }

    private ReadResult readBookmarkEnd(XmlElement element) {
        String id = element.getAttribute("w:id");
        String name = bookmarkIdsToNames.get(id);
        if (name == null || name.equals("_GoBack")) {
            return ReadResult.EMPTY_SUCCESS;
        } else {
            return success(new BookmarkEnd(name));
        }
    }

    private ReadResult readNoteReference(NoteType noteType, XmlElement element) {
        String noteId = element.getAttribute("w:id");
        return success(new NoteReference(noteType, noteId));
    }

    private ReadResult readCommentReference(XmlElement element) {
        String commentId = element.getAttribute("w:id");
        return success(new CommentReference(commentId));
    }

    private ReadResult readPict(XmlElement element) {
        return readElements(element.getChildren()).toExtra();
    }

    private ReadResult readImagedata(XmlElement element) {
        return element.getAttributeOrNone("r:id")
            .map(relationshipId -> {
                Optional<String> title = element.getAttributeOrNone("o:title");
                String imagePath = relationshipIdToDocxPath(relationshipId);
                return readImage(imagePath, title, () -> DocxFiles.getInputStream(file, imagePath));
            })
            .orElse(ReadResult.emptyWithWarning("A v:imagedata element without a relationship ID was ignored"));
    }

    private ReadResult readInline(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("wp:docPr");
        Optional<String> altText = Optionals.first(
            properties.getAttributeOrNone("descr").filter(description -> !description.trim().isEmpty()),
            properties.getAttributeOrNone("title")
        );
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
            return readImage(imagePath, altText, () -> DocxFiles.getInputStream(file, imagePath));
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

    private ReadResult readSdt(XmlElement element) {
        return readElements(element.findChildOrEmpty("w:sdtContent").getChildren());
    }

    private String relationshipIdToDocxPath(String relationshipId) {
        Relationship relationship = relationships.findRelationshipById(relationshipId);
        return "word/" + trimLeft(relationship.getTarget(), '/');
    }

    private Optional<String> readVal(XmlElementLike element, String name) {
        return element.findChildOrEmpty(name).getAttributeOrNone("w:val");
    }
}
