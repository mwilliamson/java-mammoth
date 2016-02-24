package org.zwobble.mammoth;

import java.util.Set;

public interface Result<T> {
    T getValue();
    Set<String> getWarnings();
}
