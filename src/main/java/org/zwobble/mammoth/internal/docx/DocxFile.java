package org.zwobble.mammoth.internal.docx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface DocxFile extends Closeable {
    static InputStream getInputStream(DocxFile file, String name) throws IOException {
        return file.tryGetInputStream(name)
            .orElseThrow(() -> new IOException("Missing entry in file: " + name));
    }

    Optional<InputStream> tryGetInputStream(String name) throws IOException;
}
