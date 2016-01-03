package org.zwobble.mammoth.tests.docx;

import com.google.common.collect.ImmutableMap;
import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import org.zwobble.mammoth.docx.BodyXmlReader;
import org.zwobble.mammoth.docx.Styles;

import static com.natpryce.makeiteasy.Property.newProperty;

public class BodyXmlReaderMakers {
    public static final Property<BodyXmlReader, Styles> STYLES = newProperty();

    public static final Instantiator<BodyXmlReader> bodyReader = new Instantiator<BodyXmlReader>() {
        @Override
        public BodyXmlReader instantiate(PropertyLookup<BodyXmlReader> propertyLookup) {
            return new BodyXmlReader(
                propertyLookup.valueOf(STYLES, new Styles(ImmutableMap.of(), ImmutableMap.of())));
        }
    };
}
