package org.zwobble.mammoth;

import org.zwobble.mammoth.internal.results.Warning;

import java.util.List;

public interface Result<T> {
    T getValue();
    List<Warning> getWarnings();
}
