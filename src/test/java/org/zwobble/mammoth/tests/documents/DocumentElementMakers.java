package org.zwobble.mammoth.tests.documents;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import org.zwobble.mammoth.internal.documents.*;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.natpryce.makeiteasy.Property.newProperty;
import static org.zwobble.mammoth.internal.util.Lists.list;

public class
DocumentElementMakers {
    public static final Property<DocumentElement, Optional<Style>> STYLE = newProperty();
    public static final Property<Paragraph, Optional<NumberingLevel>> NUMBERING = newProperty();
    public static final Property<Run, Boolean> BOLD = newProperty();
    public static final Property<Run, Boolean> ITALIC = newProperty();
    public static final Property<Run, Boolean> UNDERLINE = newProperty();
    public static final Property<Run, Boolean> STRIKETHROUGH = newProperty();
    public static final Property<Run, VerticalAlignment> VERTICAL_ALIGNMENT = newProperty();
    public static final Property<HasChildren, List<DocumentElement>> CHILDREN = newProperty();
    public static final Property<TableCell, Integer> COLSPAN = newProperty();

    public static final Instantiator<Paragraph> PARAGRAPH =
        propertyLookup -> new Paragraph(
            propertyLookup.valueOf(STYLE, Optional.empty()),
            propertyLookup.valueOf(NUMBERING, Optional.empty()),
            propertyLookup.valueOf(CHILDREN, list()));

    public static final Instantiator<Run> RUN =
        propertyLookup -> new Run(
            propertyLookup.valueOf(BOLD, false),
            propertyLookup.valueOf(ITALIC, false),
            propertyLookup.valueOf(UNDERLINE, false),
            propertyLookup.valueOf(STRIKETHROUGH, false),
            propertyLookup.valueOf(VERTICAL_ALIGNMENT, VerticalAlignment.BASELINE),
            propertyLookup.valueOf(STYLE, Optional.empty()),
            propertyLookup.valueOf(CHILDREN, list()));

    public static final Instantiator<TableCell> TABLE_CELL =
        propertyLookup -> new TableCell(
            1,
            propertyLookup.valueOf(COLSPAN, 1),
            propertyLookup.valueOf(CHILDREN, list()));

    public static TableCell tableCell(List<DocumentElement> children) {
        return make(a(TABLE_CELL, with(CHILDREN, children)));
    }

    public static Paragraph paragraphWithText(String text) {
        return make(a(PARAGRAPH, with(CHILDREN, list(runWithText(text)))));
    }

    public static Run runWithText(String text) {
        return make(a(RUN, with(CHILDREN, list(new Text(text)))));
    }
}
