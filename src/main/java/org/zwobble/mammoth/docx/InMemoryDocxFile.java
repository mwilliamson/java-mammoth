package org.zwobble.mammoth.docx;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InMemoryDocxFile implements DocxFile {
    public static DocxFile fromStream(InputStream stream) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(stream);
        ImmutableMap.Builder<String, byte[]> entries = ImmutableMap.builder();
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            entries.put(entry.getName(), ByteStreams.toByteArray(zipStream));
        }
        return new InMemoryDocxFile(entries.build());
    }

    public static DocxFile fromStrings(Map<String, String> entries) {
        return new InMemoryDocxFile(Maps.transformValues(entries, value -> value.getBytes(StandardCharsets.UTF_8)));
    }

    private final Map<String, byte[]> entries;

    public InMemoryDocxFile(Map<String, byte[]> entries) {
        this.entries = entries;
    }

    @Override
    public Optional<InputStream> tryGetInputStream(String name) throws IOException {
        return Optional.ofNullable(entries.get(name))
            .map(ByteArrayInputStream::new);
    }

    @Override
    public void close() throws IOException {
    }
}
