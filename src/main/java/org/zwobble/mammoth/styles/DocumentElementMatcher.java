package org.zwobble.mammoth.styles;

public interface DocumentElementMatcher<T> {
    boolean matches(T element);
}
