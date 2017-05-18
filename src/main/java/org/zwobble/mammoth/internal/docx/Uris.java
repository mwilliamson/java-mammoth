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

    public static String replaceFragment(String uri, String fragment) {
        int hashIndex = uri.indexOf("#");
        if (hashIndex != -1) {
            uri = uri.substring(0, hashIndex);
        }
        return uri + "#" + fragment;
    }
}
