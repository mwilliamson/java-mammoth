package org.zwobble.mammoth.internal.docx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

public class PathRelativeFileReader implements FileReader {
    public static FileReader relativeTo(Optional<Path> path) {
        return new PathRelativeFileReader(path);
    }

    private final Optional<Path> path;

    private PathRelativeFileReader(Optional<Path> path) {
        this.path = path;
    }

    @Override
    public InputStream getInputStream(String uri) throws IOException {
        try {
            Optional<URI> absoluteUri = asAbsoluteUri(uri);
            if (absoluteUri.isPresent()) {
                return open(absoluteUri.get());
            } else if (path.isPresent()) {
                return open(path.get().toUri().resolve(uri));
            } else {
                throw new IOException("path of document is unknown, but is required for relative URI");
            }
        } catch (IOException exception) {
            throw new IOException("could not open external image '" + uri + "': " + exception.getMessage());
        }
    }

    private static InputStream open(URI uri) throws IOException {
        return uri.toURL().openStream();
    }

    private static Optional<URI> asAbsoluteUri(String uriString) {
        try {
            URI uri = new URI(uriString);
            return uri.isAbsolute() ? Optional.of(uri) : Optional.empty();
        } catch (URISyntaxException exception) {
            return Optional.empty();
        }
    }
}
