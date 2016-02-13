package org.zwobble.mammoth.tests.docx;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.Numbering;
import org.zwobble.mammoth.docx.Relationships;
import org.zwobble.mammoth.docx.Styles;

import static com.natpryce.makeiteasy.Property.newProperty;

public class BodyXmlReaderMakers {
    public static final Property<BodyXmlReader, Styles> STYLES = newProperty();
    public static final Property<BodyXmlReader, Numbering> NUMBERING = newProperty();
    public static final Property<BodyXmlReader, Relationships> RELATIONSHIPS = newProperty();

    public static final Instantiator<BodyXmlReader> bodyReader =
        propertyLookup -> new BodyXmlReader(
            propertyLookup.valueOf(STYLES, Styles.EMPTY),
            propertyLookup.valueOf(NUMBERING, Numbering.EMPTY),
            propertyLookup.valueOf(RELATIONSHIPS, Relationships.EMPTY));
}
