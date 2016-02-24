package org.zwobble.mammoth.internal.styles;

public interface DocumentElementMatcher<T> {
    boolean matches(T element);
}
