package org.zwobble.mammoth.internal.util;

public class Paths {
    private Paths() {}

    public static String getExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return dotIndex == -1 ? "" : path.substring(dotIndex + 1);
    }
}
