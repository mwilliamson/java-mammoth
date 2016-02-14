package org.zwobble.mammoth.tests.documents;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import org.zwobble.mammoth.documents.*;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.natpryce.makeiteasy.Property.newProperty;
import static org.zwobble.mammoth.util.MammothLists.list;

public class DocumentElementMakers {
    public static final Property<HasChildren, List<DocumentElement>> CHILDREN = newProperty();

    public static final Instantiator<Paragraph> PARAGRAPH =
        propertyLookup -> new Paragraph(
            Optional.empty(),
            Optional.empty(),
            propertyLookup.valueOf(CHILDREN, list()));

    public static final Instantiator<Run> RUN =
        propertyLookup -> new Run(
            false,
            false,
            false,
            false,
            VerticalAlignment.BASELINE,
            Optional.empty(),
            propertyLookup.valueOf(CHILDREN, list()));

    public static Run runWithText(String text) {
        return make(a(RUN, with(CHILDREN, list(new Text(text)))));
    }
}
