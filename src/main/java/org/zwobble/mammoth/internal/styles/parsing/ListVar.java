package org.zwobble.mammoth.internal.styles.parsing;


import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;

class ListVar<T> extends Var<List<T>> {
    boolean append(Var<T> element) {
        return append(element.get());
    }

    boolean append(T element) {
        get().add(element);
        return set(get());
    }

    @Override
    public List<T> get() {
        if (isNotSet()) {
            set(new ArrayList<T>());
        }
        return super.get();
    }

    public boolean isEmpty() {
        return get().isEmpty();
    }
}
