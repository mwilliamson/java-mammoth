package org.zwobble.mammoth.internal.util;

public interface SupplierWithException<T, E extends Throwable> {
    T get() throws E;
}
