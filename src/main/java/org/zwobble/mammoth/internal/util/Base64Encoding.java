package org.zwobble.mammoth.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.zwobble.mammoth.internal.util.Streams.toByteArray;

public class Base64Encoding {
    public static String streamToBase64(InputStream stream) throws IOException {
        return Base64.getEncoder().encodeToString(toByteArray(stream));
    }
}
