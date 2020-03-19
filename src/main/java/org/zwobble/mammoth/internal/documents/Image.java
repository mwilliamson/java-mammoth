package org.zwobble.mammoth.internal.documents;

import org.zwobble.mammoth.internal.util.InputStreamSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Image implements DocumentElement {

    private final Optional<String> altText;
    private String path = "";
    private final Optional<String> contentType;
    private final InputStreamSupplier open;

    public Image(Optional<String> altText, String path, Optional<String> contentType, InputStreamSupplier open) {
        this.altText = altText;
        if(path != null) {
          this.path = path;
        }
        this.contentType = contentType;
        this.open = open;
    }

    public Optional<String> getAltText() {
        return altText;
    }

    public Optional<String> getContentType() {
        return contentType;
    }
    
    public String getPath() {
      return path;
    }

    public InputStream open() throws IOException {
        return open.open();
    }

    @Override
    public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
        return visitor.visit(this, context);
    }
}
