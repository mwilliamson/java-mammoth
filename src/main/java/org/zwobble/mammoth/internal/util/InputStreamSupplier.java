package org.zwobble.mammoth.internal.util;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {
    InputStream open() throws IOException;
}
