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
    private static final ArgumentKey<Boolean> BOLD = new ArgumentKey<>("bold");
    private static final ArgumentKey<Boolean> ITALIC = new ArgumentKey<>("italic");
    private static final ArgumentKey<Boolean> UNDERLINE = new ArgumentKey<>("underline");
    private static final ArgumentKey<Boolean> STRIKETHROUGH = new ArgumentKey<>("strikethrough");
    private static final ArgumentKey<VerticalAlignment> VERTICAL_ALIGNMENT = new ArgumentKey<>("verticalAlignment");
    private static final ArgumentKey<List<DocumentElement>> CHILDREN = new ArgumentKey<>("children");
    private static final ArgumentKey<Integer> COLSPAN = new ArgumentKey<>("colspan");
    private static final ArgumentKey<Integer> ROWSPAN = new ArgumentKey<>("rowspan");

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

    public static Argument<VerticalAlignment> withVerticalAlignment(VerticalAlignment verticalAlignment) {
        return arg(VERTICAL_ALIGNMENT, verticalAlignment);
    }

    public static Argument<List<DocumentElement>> withChildren(DocumentElement... children) {
        return arg(CHILDREN, asList(children));
    }

    public static Argument<Integer> withRowspan(Integer rowspan) {
        return arg(ROWSPAN, rowspan);
    }

    public static Argument<Integer> withColspan(Integer colspan) {
        return arg(COLSPAN, colspan);
    }

    public static Document document(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Document(
            arguments.get(CHILDREN, list()),
            arguments.get(Notes.class, Notes.EMPTY),
            list()
        );

    }

    public static Paragraph paragraph(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Paragraph(
            arguments.get(STYLE, Optional.empty()),
            arguments.get(NUMBERING, Optional.empty()),
            arguments.get(CHILDREN, list())
        );
    }

    public static Run run(Object... args) {
        Arguments arguments = new Arguments(args);
        return new Run(
            arguments.get(BOLD, false),
            arguments.get(ITALIC, false),
            arguments.get(UNDERLINE, false),
            arguments.get(STRIKETHROUGH, false),
            arguments.get(VERTICAL_ALIGNMENT, VerticalAlignment.BASELINE),
            arguments.get(STYLE, Optional.empty()),
            arguments.get(CHILDREN, list())
        );
    }

    public static TableCell tableCell(Object... args) {
        Arguments arguments = new Arguments(args);
        return new TableCell(
            arguments.get(ROWSPAN, 1),
            arguments.get(COLSPAN, 1),
            arguments.get(CHILDREN, list())
        );
    }

    public static Paragraph paragraphWithText(String text) {
        return paragraph(withChildren(runWithText(text)));
    }

    public static Run runWithText(String text) {
        return run(withChildren(new Text(text)));
    }
}
