package org.zwobble.mammoth.images;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface Image {
    Optional<String> getAltText();
    String getPath();
    String getContentType();
    InputStream getInputStream() throws IOException;
}
