package org.zwobble.mammoth.tests.documents;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import org.zwobble.mammoth.documents.DocumentElement;
import org.zwobble.mammoth.documents.HasChildren;
import org.zwobble.mammoth.documents.Paragraph;

import java.util.List;
import java.util.Optional;

import static com.natpryce.makeiteasy.Property.newProperty;

public class DocumentElementMakers {
    public static final Property<HasChildren, List<DocumentElement>> CHILDREN = newProperty();

    public static final Instantiator<Paragraph> PARAGRAPH = new Instantiator<Paragraph>() {
        @Override
        public Paragraph instantiate(PropertyLookup<Paragraph> propertyLookup) {
            return new Paragraph(
                Optional.empty(),
                propertyLookup.valueOf(CHILDREN, ImmutableList.of()));
        }
    };
}
