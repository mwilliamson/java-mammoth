package org.zwobble.mammoth.internal.archives;

public interface MutableArchive extends Archive {

    void writeEntry(String path, String content);
}
