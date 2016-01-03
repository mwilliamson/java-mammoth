package org.zwobble.mammoth.tests.documents;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.HasChildren;
import org.zwobble.mammoth.documents.Paragraph;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.Property.newProperty;
import static org.zwobble.mammoth.util.MammothLists.list;

public class DocumentElementMakers {
    public static final Property<HasChildren, List<DocumentElement>> CHILDREN = newProperty();

    public static final Instantiator<Paragraph> PARAGRAPH =
        propertyLookup -> new Paragraph(
            Optional.empty(),
            propertyLookup.valueOf(CHILDREN, list()));
}
