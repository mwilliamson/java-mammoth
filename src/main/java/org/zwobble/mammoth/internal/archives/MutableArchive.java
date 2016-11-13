package org.zwobble.mammoth.internal.archives;

import org.zwobble.mammoth.internal.archives.Archive;

public interface MutableArchive extends Archive {

    void writeEntry(String path, String content);
}
