package org.zwobble.mammoth.internal.xml;

import java.util.Optional;

public class NamespacePrefix {
    private final Optional<String> prefix;
    private final String uri;

    public NamespacePrefix(Optional<String> prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public Optional<String> getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }
}
