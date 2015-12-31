package org.zwobble.mammoth.docx;

import com.google.common.io.Files;

import java.util.Map;
import java.util.Optional;

public class ContentTypes {
    private final Map<String, String> extensionDefaults;
    private final Map<String, String> overrides;

    public ContentTypes(Map<String, String> extensionDefaults, Map<String, String> overrides) {
        this.extensionDefaults = extensionDefaults;
        this.overrides = overrides;
    }

    public Optional<String> findContentType(String path) {
        if (overrides.containsKey(path)) {
            return Optional.ofNullable(overrides.get(path));
        } else {
            String extension = Files.getFileExtension(path);
            return Optional.ofNullable(extensionDefaults.get(extension));
        }
    }
}
