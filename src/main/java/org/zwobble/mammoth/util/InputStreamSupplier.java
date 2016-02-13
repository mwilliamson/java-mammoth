package org.zwobble.mammoth.util;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {
    InputStream open() throws IOException;
}
