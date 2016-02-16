package org.zwobble.mammoth.docx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZippedDocxFile implements DocxFile {
    private final ZipFile zipFile;

    public ZippedDocxFile(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public Optional<InputStream> tryGetInputStream(String name) throws IOException {
        ZipEntry entry = zipFile.getEntry(name);
        if (entry == null) {
            return Optional.empty();
        } else {
            return Optional.of(zipFile.getInputStream(entry));
        }
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }
}
