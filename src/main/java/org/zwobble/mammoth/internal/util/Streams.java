package org.zwobble.mammoth.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
    private Streams() {}

    public static void copy(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[4096];
        while (true) {
            int bytesRead = source.read(buffer);
            if (bytesRead == -1) {
                return;
            }
            destination.write(buffer, 0, bytesRead);
        }
    }

    public static byte[] toByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(stream, output);
        return output.toByteArray();
    }
}
