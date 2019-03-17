package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.NumberingLevel;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class Numbering {
    public static class AbstractNum {
        private final Map<String, NumberingLevel> levels;

        public AbstractNum(Map<String, NumberingLevel> levels) {
            this.levels = levels;
        }
    }

    public static class Num {
        private final Optional<String> abstractNumId;

        public Num(Optional<String> abstractNumId) {
            this.abstractNumId = abstractNumId;
        }
    }

    public static final Numbering EMPTY = new Numbering(map(), map());

    private final Map<String, AbstractNum> abstractNums;
    private final Map<String, Num> nums;

    public Numbering(
        Map<String, AbstractNum> abstractNums,
        Map<String, Num> nums
    ) {
        this.abstractNums = abstractNums;
        this.nums = nums;
    }

    public Optional<NumberingLevel> findLevel(String numId, String level) {
        return lookup(nums, numId)
            .flatMap(num -> num.abstractNumId)
            .flatMap(abstractNumId -> lookup(this.abstractNums, abstractNumId))
            .flatMap(abstractNum -> lookup(abstractNum.levels, level));
    }
}
