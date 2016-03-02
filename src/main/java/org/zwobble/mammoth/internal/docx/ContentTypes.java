package org.zwobble.mammoth.internal.docx;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothMaps.lookup;
import static org.zwobble.mammoth.internal.util.MammothMaps.map;

public class ContentTypes {
    public static final ContentTypes DEFAULT = new ContentTypes(map(), map());

    private static final Map<String, String> imageExtensions = ImmutableMap.<String, String>builder()
        .put("png", "png")
        .put("gif", "gif")
        .put("jpeg", "jpeg")
        .put("jpg", "jpeg")
        .put("bmp", "bmp")
        .put("tif", "tiff")
        .put("tiff", "tiff")
        .build();

    private final Map<String, String> extensionDefaults;
    private final Map<String, String> overrides;

    public ContentTypes(Map<String, String> extensionDefaults, Map<String, String> overrides) {
        this.extensionDefaults = extensionDefaults;
        this.overrides = overrides;
    }

    public Optional<String> findContentType(String path) {
        if (overrides.containsKey(path)) {
            return lookup(overrides, path);
        } else {
            String extension = Files.getFileExtension(path);
            if (extensionDefaults.containsKey(extension)) {
                return lookup(extensionDefaults, extension);
            } else {
                return lookup(imageExtensions, extension.toLowerCase())
                    .map(imageType -> "image/" + imageType);
            }
        }
    }
}
