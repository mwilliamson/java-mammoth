package org.zwobble.mammoth.tests.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.docx.*;
import org.zwobble.mammoth.tests.Arguments;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class BodyXmlReaderMakers {
    public static BodyXmlReader bodyReader(Object... args) {
        Arguments arguments = new Arguments(args);
        return new BodyXmlReader(
            arguments.get(Styles.class, Styles.EMPTY),
            arguments.get(Numbering.class, Numbering.EMPTY),
            arguments.get(Relationships.class, Relationships.EMPTY),
            arguments.get(ContentTypes.class, ContentTypes.DEFAULT),
            arguments.get(Archive.class, new Archive() {
                @Override
                public Optional<InputStream> tryGetInputStream(String name) throws IOException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void close() throws IOException {
                }
            }),
            arguments.get(FileReader.class, new FileReader() {
                @Override
                public InputStream getInputStream(String uri) throws IOException {
                    throw new UnsupportedOperationException();
                }
            })
        );
    }
}
