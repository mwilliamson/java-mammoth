package org.zwobble.mammoth.internal.styles.parsing;


import com.google.common.collect.ImmutableList;
import org.parboiled.support.Var;

import java.util.List;

import static org.zwobble.mammoth.internal.util.MammothLists.list;

class ListVar<T> extends Var<ImmutableList.Builder<T>> {
    boolean append(Var<T> element) {
        return append(element.get());
    }

    boolean append(T element) {
        if (get() == null) {
            set(ImmutableList.builder());
        }
        return set(get().add(element));
    }

    public List<T> build() {
        if (get() == null) {
            return list();
        } else {
            return get().build();
        }
    }

    public boolean isEmpty() {
        return get() == null;
    }
}
