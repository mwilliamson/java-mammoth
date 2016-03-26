package org.zwobble.mammoth.tests.docx;

import org.zwobble.mammoth.internal.docx.FileReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.zwobble.mammoth.internal.util.Maps.lookup;

public class InMemoryFileReader implements FileReader {
    private final Map<String, String> entries;

    public InMemoryFileReader(Map<String, String> entries) {
        this.entries = entries;
    }

    @Override
    public InputStream getInputStream(String name) throws IOException {
        String value = lookup(entries, name).get();
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
