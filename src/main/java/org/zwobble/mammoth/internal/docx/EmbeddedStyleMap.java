package org.zwobble.mammoth.internal.docx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmbeddedStyleMap {
    private static final String PATH = "mammoth/style-map";

    public static Optional<String> readStyleMap(Archive file) throws IOException {
        return file.tryGetInputStream(PATH)
            .map(inputStream -> readInputStream(inputStream));
    }

    private static String readInputStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines()
            .collect(Collectors.joining("\n"));
    }
}
