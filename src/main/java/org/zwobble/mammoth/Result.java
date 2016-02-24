package org.zwobble.mammoth;

import java.util.List;

public interface Result<T> {
    T getValue();
    List<String> getWarnings();
}
