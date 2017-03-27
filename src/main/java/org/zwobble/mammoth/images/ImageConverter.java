package org.zwobble.mammoth.images;

import java.io.IOException;
import java.util.Map;

public class ImageConverter {
    private ImageConverter() {
    }

    public interface ImgElement {
        Map<String, String> convert(Image image) throws IOException;
    }
}
