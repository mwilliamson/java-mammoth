package org.zwobble.mammoth.tests.documents;

import org.zwobble.mammoth.internal.documents.*;
import org.zwobble.mammoth.tests.Argument;
import org.zwobble.mammoth.tests.ArgumentKey;
import org.zwobble.mammoth.tests.Arguments;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.tests.Argument.arg;

public class DocumentElementMakers {
    private static final ArgumentKey<Optional<Style>> STYLE = new ArgumentKey<>("style");
    private static final ArgumentKey<Optional<NumberingLevel>> NUMBERING = new ArgumentKey<>("numbering");
    private static final ArgumentKey<Optional<String>> HIGHLIGHT = new ArgumentKey<>("highlight");
    private static final ArgumentKey<Boolean> BOLD = new ArgumentKey<>("bold");
    private static final ArgumentKey<Boolean> ITALIC = new ArgumentKey<>("italic");
    private static final ArgumentKey<Boolean> UNDERLINE = new ArgumentKey<>("underline");
    private static final ArgumentKey<Boolean> STRIKETHROUGH = new ArgumentKey<>("strikethrough");
    private static final ArgumentKey<Boolean> ALL_CAPS = new ArgumentKey<>("allCaps");
    private static final ArgumentKey<Boolean> SMALL_CAPS = new ArgumentKey<>("smallCaps");
    private static final ArgumentKey<Optional<Alignment>> ALIGNMENT = new ArgumentKey<>("Alignment");
    private static final ArgumentKey<VerticalAlignment> VERTICAL_ALIGNMENT = new ArgumentKey<>("verticalAlignment");
    private static final ArgumentKey<List<DocumentElement>> CHILDREN = new ArgumentKey<>("children");
    private static final ArgumentKey<Boolean> IS_HEADER = new ArgumentKey<>("isHeader");
    private static final ArgumentKey<Integer> COLSPAN = new ArgumentKey<>("colspan");
    private static final ArgumentKey<Integer> ROWSPAN = new ArgumentKey<>("rowspan");
    private static final ArgumentKey<List<Comment>> COMMENTS = new ArgumentKey<>("comments");
    private static final ArgumentKey<Optional<String>> HREF = new ArgumentKey<>("href");
    private static final ArgumentKey<Optional<String>> ANCHOR = new ArgumentKey<>("anchor");
    private static final ArgumentKey<Optional<String>> TARGET_FRAME = new ArgumentKey<>("targetFrame");

    public static Argument<Optional<Style>> withStyle(Style style) {
        return arg(STYLE, Optional.of(style));
    }

    public static Argument<Optional<NumberingLevel>> withNumbering(NumberingLevel numbering) {
        return arg(NUMBERING, Optional.of(numbering));
    }

    public static Argument<Boolean> withBold(boolean bold) {
        return arg(BOLD, bold);
    }

    public static Argument<Boolean> withItalic(boolean italic) {
        return arg(ITALIC, italic);
    }

    public static Argument<Boolean> withUnderline(boolean underline) {
        return arg(UNDERLINE, underline);
    }

    public static Argument<Boolean> withStrikethrough(boolean strikethrough) {
        return arg(STRIKETHROUGH, strikethrough);
    }

    public static Argument<Optional<Alignment>> withAlignment(Optional<Alignment> alignment) {
        return arg(ALIGNMENT, alignment);
    }

    public static Argument<Boolean> withAllCaps(boolean allCaps) {
        return arg(ALL_CAPS, allCaps);
    }

    public static Argument<Boolean> withSmallCaps(boolean smallCaps) {
        return arg(SMALL_CAPS, smallCaps);
    }

    public static Argument<Optional<String>> withHighlight(String highlight) {
        return arg(HIGHLIGHT, Optional.of(highlight));
    }

    public static Argument<VerticalAlignment> withVerticalAlignment(VerticalAlignment verticalAlignment) {
        return arg(VERTICAL_ALIGNMENT, verticalAlignment);
    }

    public static Argument<List<DocumentElement>> withChildren(DocumentElement... children) {
        return arg(CHILDREN, asList(children));
    }

    public static Argument<Boolean> withIsHeader(boolean isHeader) {
        return arg(IS_HEADER, isHeader);
    }

    public static Argument<Integer> withRowspan(Integer rowspan) {
        return arg(ROWSPAN, rowspan);
    }

    public static Argument<Integer> withColspan(Integer colspan) {
        return arg(COLSPAN, colspan);
    }

    public static Argument<List<Comment>> withComments(Comment... comments) {
        return arg(COMMENTS, asList(comments));
    }

    public static Document document(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Document(
            arguments.get(CHILDREN, list()),
            arguments.get(Notes.class, Notes.EMPTY),
            arguments.get(COMMENTS, list())
        );
    }

    public static Paragraph paragraph(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Paragraph(
            arguments.get(STYLE, Optional.empty()),
            arguments.get(ALIGNMENT, Optional.empty()),
            arguments.get(NUMBERING, Optional.empty()),
            new ParagraphIndent(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            ),
            arguments.get(CHILDREN, list())
        );
    }

    public static Paragraph paragraphWithText(String text) {
        return paragraph(withChildren(runWithText(text)));
    }

    public static Run run(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Run(
            arguments.get(HIGHLIGHT, Optional.empty()),
            arguments.get(BOLD, false),
            arguments.get(ITALIC, false),
            arguments.get(UNDERLINE, false),
            arguments.get(STRIKETHROUGH, false),
            arguments.get(ALL_CAPS, false),
            arguments.get(SMALL_CAPS, false),
            arguments.get(VERTICAL_ALIGNMENT, VerticalAlignment.BASELINE),
            arguments.get(STYLE, Optional.empty()),
            arguments.get(CHILDREN, list())
        );
    }

    public static Run runWithText(String text) {
        return run(withChildren(new Text(text)));
    }

    public static Table table(List<DocumentElement> rows, Object... args) {
        Arguments arguments = new Arguments(args);
        return new Table(arguments.get(STYLE, Optional.empty()), rows);
    }

    public static TableRow tableRow(List<DocumentElement> cells, Object... args) {
        Arguments arguments = new Arguments(args);
        return new TableRow(cells, arguments.get(IS_HEADER, false));
    }

    public static TableCell tableCell(Object... args) {
        Arguments arguments = new Arguments(args);
        return new TableCell(
            arguments.get(ROWSPAN, 1),
            arguments.get(COLSPAN, 1),
            arguments.get(CHILDREN, list())
        );
    }

    public static Comment comment(String commentId, List<DocumentElement> body) {
        return new Comment(commentId, body, Optional.empty(), Optional.empty());
    }

    public static Hyperlink hyperlink(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Hyperlink(
            arguments.get(HREF, Optional.empty()),
            arguments.get(ANCHOR, Optional.empty()),
            arguments.get(TARGET_FRAME, Optional.empty()),
            arguments.get(CHILDREN, list())
        );
    }

    public static Argument<Optional<String>> withHref(String href) {
        return arg(HREF, Optional.of(href));
    }

    public static Argument<Optional<String>> withAnchor(String anchor) {
        return arg(ANCHOR, Optional.of(anchor));
    }

    public static Argument<Optional<String>> withTargetFrame(String targetFrame) {
        return arg(TARGET_FRAME, Optional.of(targetFrame));
    }

    public static Checkbox checkbox(boolean checked) {
        return new Checkbox(checked);
    }
}
