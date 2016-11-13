package org.zwobble.mammoth.internal.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Maps.lookup;

public class NamespacePrefixes {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, NamespacePrefix> uriToPrefix = new HashMap<>();

        public Builder put(String prefix, String uri) {
            uriToPrefix.put(uri, new NamespacePrefix(Optional.of(prefix), uri));
            return this;
        }

        public Builder defaultPrefix(String uri) {
            uriToPrefix.put(uri, new NamespacePrefix(Optional.empty(), uri));
            return this;
        }
        
        public NamespacePrefixes build() {
            return new NamespacePrefixes(uriToPrefix);
        }
    }

    private final Map<String, NamespacePrefix> uriToPrefix;

    public NamespacePrefixes(Map<String, NamespacePrefix> uriToPrefix) {
        this.uriToPrefix = uriToPrefix;
    }

    public Optional<NamespacePrefix> lookupUri(String uri) {
        return lookup(uriToPrefix, uri);
    }
}
