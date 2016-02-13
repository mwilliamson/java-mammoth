package org.zwobble.mammoth.tests.docx;

import org.zwobble.mammoth.docx.DocxFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class InMemoryDocxFile implements DocxFile {
    private final Map<String, String> entries;

    public InMemoryDocxFile(Map<String, String> entries) {
        this.entries = entries;
    }

    @Override
    public InputStream getInputStream(String name) throws IOException {
        String value = Optional.ofNullable(entries.get(name)).get();
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
    }
}
