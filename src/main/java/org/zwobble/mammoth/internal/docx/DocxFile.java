package org.zwobble.mammoth.internal.docx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface DocxFile extends Closeable {
    Optional<InputStream> tryGetInputStream(String name) throws IOException;
}
