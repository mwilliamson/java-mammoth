package org.zwobble.mammoth.docx;

import com.google.common.io.Files;

import java.util.Map;
import java.util.Optional;

public class ContentTypes {
    private final Map<String, String> extensionDefaults;

    public ContentTypes(Map<String, String> extensionDefaults) {
        this.extensionDefaults = extensionDefaults;
    }

    public Optional<String> findContentType(String path) {
        String extension = Files.getFileExtension(path);
        return Optional.ofNullable(extensionDefaults.get(extension));
    }
}
