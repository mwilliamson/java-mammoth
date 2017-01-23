package org.zwobble.mammoth.internal.docx;

public class Uris {
    private Uris() {
    }

    public static String uriToZipEntryName(String base, String uri) {
        if (uri.startsWith("/")) {
            return uri.substring(1);
        } else {
            return base + "/" + uri;
        }
    }
}
