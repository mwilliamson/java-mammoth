package org.zwobble.mammoth.tests.docx;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import org.zwobble.mammoth.docx.*;

import java.io.IOException;
import java.io.InputStream;

import static com.natpryce.makeiteasy.Property.newProperty;

public class BodyXmlReaderMakers {
    public static final Property<BodyXmlReader, Styles> STYLES = newProperty();
    public static final Property<BodyXmlReader, Numbering> NUMBERING = newProperty();
    public static final Property<BodyXmlReader, Relationships> RELATIONSHIPS = newProperty();
    public static final Property<BodyXmlReader, ContentTypes> CONTENT_TYPES = newProperty();
    public static final Property<BodyXmlReader, DocxFile> DOCX_FILE = newProperty();
    public static final Property<BodyXmlReader, FileReader> FILE_READER = newProperty();

    public static final Instantiator<BodyXmlReader> bodyReader =
        propertyLookup -> new BodyXmlReader(
            propertyLookup.valueOf(STYLES, Styles.EMPTY),
            propertyLookup.valueOf(NUMBERING, Numbering.EMPTY),
            propertyLookup.valueOf(RELATIONSHIPS, Relationships.EMPTY),
            propertyLookup.valueOf(CONTENT_TYPES, ContentTypes.DEFAULT),
            propertyLookup.valueOf(DOCX_FILE, new DocxFile() {
                @Override
                public InputStream getInputStream(String name) throws IOException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void close() throws IOException {
                }
            }),
            propertyLookup.valueOf(FILE_READER, new FileReader() {
                @Override
                public InputStream getInputStream(String uri) throws IOException {
                    throw new UnsupportedOperationException();
                }
            }));
}
