package org.zwobble.mammoth.docx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface DocxFile extends Closeable {
    InputStream getInputStream(String name) throws IOException;
}
