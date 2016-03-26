package org.zwobble.mammoth.internal.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.MammothMaps.lookup;

public class NamespacePrefixes {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> uriToPrefix = new HashMap<>();

        public Builder put(String prefix, String uri) {
            uriToPrefix.put(uri, prefix);
            return this;
        }
        
        public NamespacePrefixes build() {
            return new NamespacePrefixes(uriToPrefix);
        }
    }

    private final Map<String, String> uriToPrefix;

    public NamespacePrefixes(Map<String, String> uriToPrefix) {
        this.uriToPrefix = uriToPrefix;
    }

    public Optional<String> prefixForUri(String uri) {
        return lookup(uriToPrefix, uri);
    }
}
