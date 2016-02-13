package org.zwobble.mammoth.documents;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Image implements DocumentElement {
    @FunctionalInterface
    public interface InputStreamSupplier {
        InputStream open() throws IOException;
    }

    private final Optional<String> altText;
    private final Optional<String> contentType;
    private final InputStreamSupplier open;

    public Image(Optional<String> altText, Optional<String> contentType, InputStreamSupplier open) {
        this.altText = altText;
        this.contentType = contentType;
        this.open = open;
    }

    public Optional<String> getAltText() {
        return altText;
    }

    public Optional<String> getContentType() {
        return contentType;
    }

    public InputStream open() throws IOException {
        return open.open();
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
