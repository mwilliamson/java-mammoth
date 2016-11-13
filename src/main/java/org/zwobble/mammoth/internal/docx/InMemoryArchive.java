package org.zwobble.mammoth.internal.docx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.zwobble.mammoth.internal.util.Maps.eagerMapValues;
import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Streams.toByteArray;

public class InMemoryArchive implements Archive {
    public static Archive fromStream(InputStream stream) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(stream);
        Map<String, byte[]> entries = new HashMap<>();
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            entries.put(entry.getName(), toByteArray(zipStream));
        }
        return new InMemoryArchive(entries);
    }

    public static Archive fromStrings(Map<String, String> entries) {
        return new InMemoryArchive(eagerMapValues(entries, value -> value.getBytes(StandardCharsets.UTF_8)));
    }

    private final Map<String, byte[]> entries;

    public InMemoryArchive(Map<String, byte[]> entries) {
        this.entries = entries;
    }

    @Override
    public Optional<InputStream> tryGetInputStream(String name) throws IOException {
        return lookup(entries, name)
            .map(ByteArrayInputStream::new);
    }

    @Override
    public void close() throws IOException {
    }
}
