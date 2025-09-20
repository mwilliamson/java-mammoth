package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.Archives;
import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.internal.results.InternalResult;
import org.zwobble.mammoth.internal.util.*;
import org.zwobble.mammoth.internal.xml.*;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zwobble.mammoth.internal.docx.ReadResult.*;
import static org.zwobble.mammoth.internal.docx.Uris.uriToZipEntryName;
import static org.zwobble.mammoth.internal.util.Casts.tryCast;
import static org.zwobble.mammoth.internal.util.Iterables.lazyFilter;
import static org.zwobble.mammoth.internal.util.Iterables.tryGetLast;
import static org.zwobble.mammoth.internal.util.Lists.eagerConcat;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.entry;
import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Sets.set;
import static org.zwobble.mammoth.internal.util.Strings.codepointToString;

class StatefulBodyXmlReader {
    private static final Set<String> IMAGE_TYPES_SUPPORTED_BY_BROWSERS = set(
        "image/png", "image/gif", "image/jpeg", "image/svg+xml", "image/tiff");

    private final Styles styles;
    private final Numbering numbering;
    private final Relationships relationships;
    private final ContentTypes contentTypes;
    private final Archive file;
    private final FileReader fileReader;
    private final StringBuilder currentInstrText;
    private final Queue<ComplexField> complexFieldStack;
    private List<XmlNode> deletedParagraphContents;

    private interface ComplexField {
        ComplexField UNKNOWN = new ComplexField() {};

        static ComplexField begin(XmlElement fldChar) {
            return new BeginComplexField(fldChar);
        }

        static ComplexField hyperlink(Function<List<DocumentElement>, Hyperlink> childrenToHyperlink) {
            return new HyperlinkComplexField(childrenToHyperlink);
        }

        static ComplexField checkbox(boolean checked) {
            return new CheckboxComplexField(checked);
        }
    }

    private static class BeginComplexField implements ComplexField {
        private final XmlElement fldChar;

        private BeginComplexField(XmlElement fldChar) {
            this.fldChar = fldChar;
        }
    }

    private static class HyperlinkComplexField implements ComplexField {
        private final Function<List<DocumentElement>, Hyperlink> childrenToHyperlink;

        private HyperlinkComplexField(Function<List<DocumentElement>, Hyperlink> childrenToHyperlink) {
            this.childrenToHyperlink = childrenToHyperlink;
        }
    }

    private static class CheckboxComplexField implements ComplexField {
        private final boolean checked;

        private CheckboxComplexField(boolean checked) {
            this.checked = checked;
        }
    }

    StatefulBodyXmlReader(
        Styles styles,
        Numbering numbering,
        Relationships relationships,
        ContentTypes contentTypes,
        Archive file,
        FileReader fileReader
    )
    {
        this.styles = styles;
        this.numbering = numbering;
        this.relationships = relationships;
        this.contentTypes = contentTypes;
        this.file = file;
        this.fileReader = fileReader;
        this.currentInstrText = new StringBuilder();
        this.complexFieldStack = Queues.stack();
        this.deletedParagraphContents = new ArrayList<>();
    }

    ReadResult readElement(XmlElement element) {
        switch (element.getName()) {
            case "w:t":
                return success(new Text(element.innerText()));
            case "w:r":
                return readRun(element);
            case "w:p":
                return readParagraph(element);

            case "w:fldChar":
                return readFieldChar(element);
            case "w:instrText":
                return readInstrText(element);

            case "w:tab":
                return success(Tab.TAB);
            case "w:noBreakHyphen":
                return success(new Text("\u2011"));
            case "w:softHyphen":
                return success(new Text("\u00ad"));
            case "w:sym":
                return readSymbol(element);
            case "w:br":
                return readBreak(element);

            case "w:tbl":
                return readTable(element);
            case "w:tr":
                return readTableRow(element);
            case "w:tc":
                return readTableCell(element);

            case "w:hyperlink":
                return readHyperlink(element);
            case "w:bookmarkStart":
                return readBookmark(element);
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
            case "w:object":
            case "w:smartTag":
            case "w:drawing":
            case "v:group":
            case "v:rect":
            case "v:roundrect":
            case "v:shape":
            case "v:textbox":
            case "w:txbxContent":
                return readElements(element.getChildren());

            case "office-word:wrap":
            case "v:shadow":
            case "v:shapetype":
            case "w:bookmarkEnd":
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
            case "w:trPr":
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
            (style, children) -> {
                Optional<HyperlinkComplexField> hyperlinkComplexField = currentHyperlinkComplexField();
                if (hyperlinkComplexField.isPresent()) {
                    children = list(hyperlinkComplexField.get().childrenToHyperlink.apply(children));
                }

                return new Run(
                    readHighlight(properties),
                    isBold(properties),
                    isItalic(properties),
                    isUnderline(properties),
                    isStrikethrough(properties),
                    isAllCaps(properties),
                    isSmallCaps(properties),
                    readVerticalAlignment(properties),
                    style,
                    children
                );
            }
        );
    }

    private Optional<HyperlinkComplexField> currentHyperlinkComplexField() {
        return tryGetLast(lazyFilter(this.complexFieldStack, HyperlinkComplexField.class));
    }

    private Optional<String> readHighlight(XmlElementLike properties) {
        return readVal(properties, "w:highlight")
            .filter(value -> !value.isEmpty() && !value.equals("none"));
    }

    private boolean isBold(XmlElementLike properties) {
        return readBooleanElement(properties, "w:b");
    }

    private boolean isItalic(XmlElementLike properties) {
        return readBooleanElement(properties, "w:i");
    }

    private boolean isUnderline(XmlElementLike properties) {
        return properties.findChild("w:u")
            .flatMap(child -> child.getAttributeOrNone("w:val"))
            .map(value -> !value.equals("false") && !value.equals("0") && !value.equals("none"))
            .orElse(false);
    }

    private boolean isStrikethrough(XmlElementLike properties) {
        return readBooleanElement(properties, "w:strike");
    }

    private boolean isAllCaps(XmlElementLike properties) {
        return readBooleanElement(properties, "w:caps");
    }

    private boolean isSmallCaps(XmlElementLike properties) {
        return readBooleanElement(properties, "w:smallCaps");
    }

    private boolean readBooleanElement(XmlElementLike properties, String tagName) {
        return properties.findChild(tagName)
            .map(child -> readBooleanAttributeValue(child.getAttributeOrNone("w:val")))
            .orElse(false);
    }

    private boolean readBooleanAttributeValue(Optional<String> valAttributeValue) {
        return valAttributeValue
            .map(value -> !value.equals("false") && !value.equals("0"))
            .orElse(true);
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

    ReadResult readElements(Iterable<XmlNode> nodes) {
        return ReadResult.flatMap(lazyFilter(nodes, XmlElement.class), this::readElement);
    }

    private ReadResult readParagraph(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:pPr");

        boolean isDeleted = properties
            .findChildOrEmpty("w:rPr")
            .findChild("w:del")
            .isPresent();

        if (isDeleted) {
            deletedParagraphContents.addAll(element.getChildren());
            return ReadResult.success(list());
        } else {
            ParagraphIndent indent = readParagraphIndent(properties);
            Optional<Alignment> alignment = readParagraphAlignment(properties);
            List<XmlNode> childrenXml = element.getChildren();
            if (!deletedParagraphContents.isEmpty()) {
                childrenXml = eagerConcat(deletedParagraphContents, childrenXml);
                deletedParagraphContents = new ArrayList<>();
            }
            return ReadResult.map(
                readParagraphStyle(properties),
                readElements(childrenXml),
                (style, children) -> new Paragraph(style, alignment, readNumbering(style, properties), indent, children)).appendExtra();
        }
    }

    private ReadResult readFieldChar(XmlElement element) {
        String type = element.getAttributeOrNone("w:fldCharType").orElse("");
        if (type.equals("begin")) {
            complexFieldStack.add(ComplexField.begin(element));
            currentInstrText.setLength(0);
        } else if (type.equals("end")) {
            ComplexField complexField = complexFieldStack.remove();
            if (complexField instanceof BeginComplexField) {
                complexField = parseCurrentInstrText(complexField);
            }
            if (complexField instanceof CheckboxComplexField) {
                return success(new Checkbox(((CheckboxComplexField) complexField).checked));
            }
        } else if (type.equals("separate")) {
            ComplexField complexFieldSeparate = complexFieldStack.remove();
            ComplexField complexField = parseCurrentInstrText(complexFieldSeparate);
            complexFieldStack.add(complexField);
        }
        return ReadResult.EMPTY_SUCCESS;
    }

    private ComplexField parseCurrentInstrText(ComplexField complexField) {
        String instrText = currentInstrText.toString();

        XmlElementLike fldChar = complexField instanceof BeginComplexField
            ? ((BeginComplexField) complexField).fldChar
            : NullXmlElement.INSTANCE;

        return parseInstrText(instrText, fldChar);
    }

    private ComplexField parseInstrText(String instrText, XmlElementLike fldChar) {
        Pattern externalLinkPattern = Pattern.compile("\\s*HYPERLINK \"(.*)\"");
        Matcher externalLinkMatcher = externalLinkPattern.matcher(instrText);
        if (externalLinkMatcher.lookingAt()) {
            String href = externalLinkMatcher.group(1);
            return ComplexField.hyperlink(children -> Hyperlink.href(href, Optional.empty(), children));
        }

        Pattern internalLinkPattern = Pattern.compile("\\s*HYPERLINK\\s+\\\\l\\s+\"(.*)\"");
        Matcher internalLinkMatcher = internalLinkPattern.matcher(instrText);
        if (internalLinkMatcher.lookingAt()) {
            String anchor = internalLinkMatcher.group(1);
            return ComplexField.hyperlink(children -> Hyperlink.anchor(anchor, Optional.empty(), children));
        }

        Pattern checkboxPattern = Pattern.compile("\\s*FORMCHECKBOX\\s*");
        Matcher checkboxMatcher = checkboxPattern.matcher(instrText);
        if (checkboxMatcher.lookingAt()) {
            XmlElementLike checkboxElement = fldChar
                .findChildOrEmpty("w:ffData")
                .findChildOrEmpty("w:checkBox");

            boolean checked = checkboxElement.hasChild("w:checked")
                ? readBooleanElement(checkboxElement, "w:checked")
                : readBooleanElement(checkboxElement, "w:default");

            return ComplexField.checkbox(checked);
        }

        return ComplexField.UNKNOWN;
    }

    private ReadResult readInstrText(XmlElement element) {
        currentInstrText.append(element.innerText());
        return ReadResult.EMPTY_SUCCESS;
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

    private Optional<NumberingLevel> readNumbering(Optional<Style> style, XmlElementLike properties) {
        XmlElementLike numberingProperties = properties.findChildOrEmpty("w:numPr");
        Optional<String> numId = readVal(numberingProperties, "w:numId");
        Optional<String> levelIndex = readVal(numberingProperties, "w:ilvl");
        if (numId.isPresent() && levelIndex.isPresent()) {
            return numbering.findLevel(numId.get(), levelIndex.get());
        }

        if (style.isPresent()) {
            String styleId = style.get().getStyleId();
            Optional<NumberingLevel> level = numbering.findLevelByParagraphStyleId(styleId);
            if (level.isPresent()) {
                return level;
            }
        }

        // Some malformed documents define numbering levels without an index, and
        // reference the numbering using a w:numPr element without a w:ilvl child.
        // To handle such cases, we assume a level of 0 as a fallback.
        if (numId.isPresent()) {
            return numbering.findLevel(numId.get(), "0");
        }

        return Optional.empty();
    }

    private ParagraphIndent readParagraphIndent(XmlElementLike properties) {
        XmlElementLike indent = properties.findChildOrEmpty("w:ind");
        return new ParagraphIndent(
            Optionals.first(
                indent.getAttributeOrNone("w:start"),
                indent.getAttributeOrNone("w:left")
            ),
            Optionals.first(
                indent.getAttributeOrNone("w:end"),
                indent.getAttributeOrNone("w:right")
            ),
            indent.getAttributeOrNone("w:firstLine"),
            indent.getAttributeOrNone("w:hanging")
        );
    }

    private Optional<Alignment> readParagraphAlignment(XmlElementLike properties) {
        String align = properties
        .findChild("w:jc")
        .map(jc -> jc.getAttributeOrNone("w:val").orElse(""))
        .orElse("");

        switch (align) {
            case "left": return Optional.of(new Alignment("left"));
            case "center": return Optional.of(new Alignment("center"));
            case "right": return Optional.of(new Alignment("right"));
            case "both": return Optional.of(new Alignment("justify"));
            default: return Optional.empty();
        }
    }

    private ReadResult readSymbol(XmlElement element) {
        Optional<String> font = element.getAttributeOrNone("w:font");
        Optional<String> charValue = element.getAttributeOrNone("w:char");
        if (font.isPresent() && charValue.isPresent()) {
            Optional<Integer> dingbat = Dingbats.findDingbat(font.get(), Integer.parseInt(charValue.get(), 16));

            if (!dingbat.isPresent() && Pattern.matches("F0..", charValue.get())) {
                dingbat = Dingbats.findDingbat(font.get(), Integer.parseInt(charValue.get().substring(2), 16));
            }

            if (dingbat.isPresent()) {
                return ReadResult.success(new Text(codepointToString(dingbat.get())));
            }
        }
        return emptyWithWarning(
            "A w:sym element with an unsupported character was ignored: char " +
                charValue.orElse("null") + " in font " + font.orElse("null")
        );
    }

    private ReadResult readBreak(XmlElement element) {
        String breakType = element.getAttributeOrNone("w:type").orElse("textWrapping");
        switch (breakType) {
            case "textWrapping":
                return success(Break.LINE_BREAK);
            case "page":
                return success(Break.PAGE_BREAK);
            case "column":
                return success(Break.COLUMN_BREAK);
            default:
                return ReadResult.emptyWithWarning("Unsupported break type: " + breakType);
        }
    }

    private ReadResult readTable(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:tblPr");
        return ReadResult.map(
            readTableStyle(properties),
            readElements(element.getChildren())
                .flatMap(this::calculateRowspans),

            Table::new
        );
    }

    private InternalResult<Optional<Style>> readTableStyle(XmlElementLike properties) {
        return readStyle(properties, "w:tblStyle", "Table", styles::findTableStyleById);
    }

    private ReadResult calculateRowspans(List<DocumentElement> rows) {
        Optional<String> error = checkTableRows(rows);
        if (error.isPresent()) {
            rows = removeUnmergedTableCells(rows);
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

            return new TableRow(mergedCells, row.isHeader());
        }));
    }

    private Optional<String> checkTableRows(List<DocumentElement> rows) {
        for (DocumentElement rowElement : rows) {
            Optional<TableRow> row = tryCast(TableRow.class, rowElement);
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

    private List<DocumentElement> removeUnmergedTableCells(List<DocumentElement> rows) {
        return Lists.eagerMap(
            rows,
            transformElementsOfType(
                UnmergedTableCell.class,
                cell -> new TableCell(1, cell.colspan, cell.children)
            )
        );
    }

    private ReadResult readTableRow(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:trPr");

        // See 17.13.5.12 del (Deleted Table Row) of ECMA-376 4th edition Part 1
        boolean deleted = properties.hasChild("w:del");
        if (deleted) {
            return ReadResult.EMPTY_SUCCESS;
        }

        boolean isHeader = properties.hasChild("w:tblHeader");
        return readElements(element.getChildren())
            .map(children -> list(new TableRow(children, isHeader)));
    }

    private ReadResult readTableCell(XmlElement element) {
        XmlElementLike properties = element.findChildOrEmpty("w:tcPr");
        Optional<String> gridSpan = properties
            .findChildOrEmpty("w:gridSpan")
            .getAttributeOrNone("w:val");
        int colspan = gridSpan.map(Integer::parseInt).orElse(1);
        return readElements(element.getChildren())
            .map(children -> list(new UnmergedTableCell(readVmerge(properties), colspan, children)));
    }

    private boolean readVmerge(XmlElementLike properties) {
        return properties.findChild("w:vMerge")
            .map(element -> element.getAttributeOrNone("w:val").map(val -> val.equals("continue")).orElse(true))
            .orElse(false);
    }

    private static class UnmergedTableCell implements DocumentElement, HasChildren {
        private final boolean vmerge;
        private final int colspan;
        private final List<DocumentElement> children;

        private UnmergedTableCell(boolean vmerge, int colspan, List<DocumentElement> children) {
            this.vmerge = vmerge;
            this.colspan = colspan;
            this.children = children;
        }

        @Override
        public List<DocumentElement> getChildren() {
            return children;
        }

        @Override
        public DocumentElement replaceChildren(List<DocumentElement> newChildren) {
            return new UnmergedTableCell(this.vmerge, this.colspan, newChildren);
        }

        @Override
        public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
            return visitor.visit(new TableCell(1, colspan, children), context);
        }
    }

    private ReadResult readHyperlink(XmlElement element) {
        Optional<String> relationshipId = element.getAttributeOrNone("r:id");
        Optional<String> anchor = element.getAttributeOrNone("w:anchor");
        Optional<String> targetFrame = element.getAttributeOrNone("w:tgtFrame")
            .filter(value -> !value.isEmpty());
        ReadResult childrenResult = readElements(element.getChildren());

        if (relationshipId.isPresent()) {
            String targetHref = relationships.findTargetByRelationshipId(relationshipId.get());
            String href = anchor.map(fragment -> Uris.replaceFragment(targetHref, anchor.get()))
                .orElse(targetHref);
            return childrenResult.map(children ->
                list(Hyperlink.href(href, targetFrame, children))
            );
        } else if (anchor.isPresent()) {
            return childrenResult.map(children ->
                list(Hyperlink.anchor(anchor.get(), targetFrame, children))
            );
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
                return readImage(imagePath, title, () -> Archives.getInputStream(file, imagePath));
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
            return readImage(imagePath, altText, () -> Archives.getInputStream(file, imagePath));
        } else if (linkRelationshipId.isPresent()) {
            String imagePath = relationships.findTargetByRelationshipId(linkRelationshipId.get());
            return readImage(imagePath, altText, () -> fileReader.getInputStream(imagePath));
        } else {
            return ReadResult.emptyWithWarning("Could not find image file for a:blip element");
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
        ReadResult contentResult = readElements(
            element.findChildOrEmpty("w:sdtContent").getChildren()
        );
        return contentResult.map(content -> {
            // From the WordML standard: https://learn.microsoft.com/en-us/openspecs/office_standards/ms-docx/3350cb64-931f-41f7-8824-f18b2568ce66
            //
            // > A CT_SdtCheckbox element that specifies that the parent
            // > structured document tag is a checkbox when displayed in the
            // > document. The parent structured document tag contents MUST
            // > contain a single character and optionally an additional
            // > character in a deleted run.

            Optional<XmlElement> checkbox = element
                .findChildOrEmpty("w:sdtPr")
                .findChild("wordml:checkbox");

            if (!checkbox.isPresent()) {
                return content;
            }

            Optional<XmlElement> checkedElement = checkbox.get().findChild("wordml:checked");
            boolean isChecked = checkedElement.isPresent() &&
                readBooleanAttributeValue(checkedElement.get().getAttributeOrNone("wordml:val"));
            Checkbox documentCheckbox = new Checkbox(isChecked);

            MutableBoolean hasCheckbox = new MutableBoolean(false);
            List<DocumentElement> replacedContent = Lists.eagerMap(
                content,
                transformElementsOfType(Text.class, text -> {
                    if (text.getValue().length() > 0 && !hasCheckbox.get()) {
                        hasCheckbox.set(true);
                        return documentCheckbox;
                    } else {
                        return text;
                    }
                })
            );

            if (hasCheckbox.get()) {
                return replacedContent;
            } else {
                return list(documentCheckbox);
            }
        });
    }

    private <T extends DocumentElement> Function<DocumentElement, DocumentElement> transformElementsOfType(
        Class<T> elementClass,
        Function<T, DocumentElement> transform
    ) {
        return element -> {
            if (element instanceof HasChildren) {
                element = ((HasChildren) element).replaceChildren(
                    Lists.eagerMap(((HasChildren) element).getChildren(), transformElementsOfType(elementClass, transform))
                );
            }

            return tryCast(elementClass, element)
                .map(transform)
                .orElse(element);
        };
    }

    private String relationshipIdToDocxPath(String relationshipId) {
        String target = relationships.findTargetByRelationshipId(relationshipId);
        return uriToZipEntryName("word", target);
    }

    private Optional<String> readVal(XmlElementLike element, String name) {
        return element.findChildOrEmpty(name).getAttributeOrNone("w:val");
    }
}
