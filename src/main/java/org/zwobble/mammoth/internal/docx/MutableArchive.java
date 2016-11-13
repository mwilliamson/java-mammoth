package org.zwobble.mammoth.internal.docx;

public interface MutableArchive extends Archive {

    void writeEntry(String path, String content);
}
