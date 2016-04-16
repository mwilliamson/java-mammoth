package org.zwobble.mammoth.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.zwobble.mammoth.internal.util.Streams.toByteArray;

public class Base64Encoding {
    public static String streamToBase64(SupplierWithException<InputStream, IOException> open) throws IOException {
        try (InputStream stream = open.get()) {
            return Base64Encoding.streamToBase64(stream);
        }
    }

    public static String streamToBase64(InputStream stream) throws IOException {
        return Base64.getEncoder().encodeToString(toByteArray(stream));
    }
}
