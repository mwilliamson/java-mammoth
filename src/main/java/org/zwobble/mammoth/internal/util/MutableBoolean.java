package org.zwobble.mammoth.internal.util;

public class MutableBoolean {
    private boolean value;

    public MutableBoolean(boolean initialValue) {
        this.value = initialValue;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean newValue) {
        this.value = newValue;
    }
}
