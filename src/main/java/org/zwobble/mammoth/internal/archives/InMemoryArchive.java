package org.zwobble.mammoth.internal.archives;

import org.zwobble.mammoth.internal.util.PassThroughException;
import org.zwobble.mammoth.internal.util.Streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.zwobble.mammoth.internal.util.Maps.eagerMapValues;
import static org.zwobble.mammoth.internal.util.Maps.lookup;

public class InMemoryArchive implements MutableArchive {
    public static InMemoryArchive fromStream(InputStream stream) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(stream);
        Map<String, byte[]> entries = new HashMap<>();
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            entries.put(entry.getName(), Streams.toByteArray(zipStream));
        }
        return new InMemoryArchive(entries);
    }

    public static InMemoryArchive fromStrings(Map<String, String> entries) {
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
    public boolean exists(String name) {
        return entries.containsKey(name);
    }

    @Override
    public void writeEntry(String path, String content) {
        entries.put(path, content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
    }

    public byte[] toByteArray() {
        return PassThroughException.wrap(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipStream = new ZipOutputStream(outputStream)) {
                for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                    zipStream.putNextEntry(new ZipEntry(entry.getKey()));
                    zipStream.write(entry.getValue());
                }
            }
            return outputStream.toByteArray();
        });
    }
}
