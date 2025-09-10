package org.zwobble.mammoth.internal.docx;

import java.io.IOException;
import java.io.InputStream;

public class DisabledFileReader implements FileReader {
    @Override
    public InputStream getInputStream(String uri) throws IOException {
        throw new IOException("could not open external image '" + uri + "': external file access is disabled");
    }
}
