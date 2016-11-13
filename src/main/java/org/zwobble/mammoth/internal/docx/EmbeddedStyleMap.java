package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.archives.Archive;
import org.zwobble.mammoth.internal.archives.MutableArchive;
import org.zwobble.mammoth.internal.util.Streams;

import java.io.*;
import java.util.Optional;

public class EmbeddedStyleMap {
    private static final String PATH = "mammoth/style-map";

    public static Optional<String> readStyleMap(Archive file) throws IOException {
        return file.tryGetInputStream(PATH).map(Streams::toString);
    }

    public static void embedStyleMap(MutableArchive archive, String styleMap) {
        archive.writeEntry(PATH, styleMap);
    }
}
