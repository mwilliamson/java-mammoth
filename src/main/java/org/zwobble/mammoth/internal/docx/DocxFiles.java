package org.zwobble.mammoth.internal.docx;

import java.io.IOException;
import java.io.InputStream;

public class DocxFiles {
    static InputStream getInputStream(Archive file, String name) throws IOException {
        return file.tryGetInputStream(name)
            .orElseThrow(() -> new IOException("Missing entry in file: " + name));
    }
}
