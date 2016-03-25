package org.zwobble.mammoth;

import java.util.Set;

/**
 * The result of converting a document.
 */
public interface Result<T> {
    /**
     * The generated value.
     */
    T getValue();

    /**
     * Any warnings generated during the conversion.
     */
    Set<String> getWarnings();
}
