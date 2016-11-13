package org.zwobble.mammoth.internal.util;

import java.io.*;
import java.util.stream.Collectors;

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

    public static String toString(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
            .lines()
            .collect(Collectors.joining("\n"));
    }
}
