package org.zwobble.mammoth.internal;

import org.zwobble.mammoth.images.ImageConverter;
import org.zwobble.mammoth.internal.documents.Image;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;
import org.zwobble.mammoth.internal.util.PassThroughException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Lists.list;

public class InternalImageConverter {
    private final ImageConverter.ImgElement imgElement;

    public static InternalImageConverter imgElement(ImageConverter.ImgElement imgElement) {
        return new InternalImageConverter(imgElement);
    }

    private InternalImageConverter(ImageConverter.ImgElement imgElement) {
        this.imgElement = imgElement;
    }

    public List<HtmlNode> convert(Image internalImage) throws IOException {
        // TODO: handle empty content type
        return PassThroughException.unwrap(() -> {
            return internalImage.getContentType()
                .map(contentType -> {
                    org.zwobble.mammoth.images.Image image = new org.zwobble.mammoth.images.Image() {
                        @Override
                        public Optional<String> getAltText() {
                            return internalImage.getAltText();
                        }

                        @Override
                        public String getContentType() {
                            return contentType;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return internalImage.open();
                        }
                    };

                    Map<String, String> attributes = new HashMap<>(PassThroughException.wrap(() -> imgElement.convert(image)));
                    internalImage.getAltText().ifPresent(altText -> attributes.put("alt", altText));
                    return list(Html.element("img", attributes));
                })
                .orElse(list());
        });
    }
}
