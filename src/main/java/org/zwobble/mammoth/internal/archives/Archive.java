package org.zwobble.mammoth.internal.archives;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface Archive extends Closeable {
    Optional<InputStream> tryGetInputStream(String name) throws IOException;
    boolean exists(String name);
}
