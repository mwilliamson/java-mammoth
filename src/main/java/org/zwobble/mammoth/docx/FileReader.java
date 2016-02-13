package org.zwobble.mammoth.docx;

import java.io.IOException;
import java.io.InputStream;

public interface FileReader {
    InputStream getInputStream(String uri) throws IOException;
}
