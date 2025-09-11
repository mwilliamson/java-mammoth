package org.zwobble.mammoth.internal.docx;

import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.util.Iterables;
import org.zwobble.mammoth.internal.util.Maps;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.mammoth.internal.util.Maps.lookup;
import static org.zwobble.mammoth.internal.util.Maps.map;

public class Numbering {

    public static class AbstractNum {
        private final Map<String, AbstractNumLevel> levels;
        private final Optional<String> numStyleLink;

        public AbstractNum(Map<String, AbstractNumLevel> levels, Optional<String> numStyleLink) {
            this.levels = levels;
            this.numStyleLink = numStyleLink;
        }
    }

    public static class AbstractNumLevel {
        public static AbstractNumLevel ordered(String levelIndex) {
            return new AbstractNumLevel(levelIndex, true, Optional.empty());
        }

        public static AbstractNumLevel unordered(String levelIndex) {
            return new AbstractNumLevel(levelIndex, false, Optional.empty());
        }

        private final String levelIndex;
        private final boolean isOrdered;
        private final Optional<String> paragraphStyleId;

        public AbstractNumLevel(String levelIndex, boolean isOrdered, Optional<String> paragraphStyleId) {
            this.levelIndex = levelIndex;
            this.isOrdered = isOrdered;
            this.paragraphStyleId = paragraphStyleId;
        }

        public String levelIndex() {
            return levelIndex;
        }

        public NumberingLevel toNumberingLevel() {
            return new NumberingLevel(levelIndex, isOrdered);
        }
    }

    public static class Num {
        private final Optional<String> abstractNumId;

        public Num(Optional<String> abstractNumId) {
            this.abstractNumId = abstractNumId;
        }
    }

    public static final Numbering EMPTY = new Numbering(map(), map(), Styles.EMPTY);

    private final Map<String, AbstractNum> abstractNums;
    private final Map<String, AbstractNumLevel> levelsByParagraphStyleId;
    private final Map<String, Num> nums;
    private final Styles styles;

    public Numbering(
        Map<String, AbstractNum> abstractNums,
        Map<String, Num> nums,
        Styles styles
    ) {
        this.abstractNums = abstractNums;
        this.levelsByParagraphStyleId = Maps.toMapWithKey(
            Iterables.lazyFilter(
                Iterables.lazyFlatMap(
                    abstractNums.values(),
                    abstractNum -> abstractNum.levels.values()
                ),
                level -> level.paragraphStyleId.isPresent()
            ),
            level -> level.paragraphStyleId.get()
        );
        this.nums = nums;
        this.styles = styles;
    }

    public Optional<NumberingLevel> findLevel(String numId, String level) {
        return lookup(nums, numId)
            .flatMap(num -> num.abstractNumId)
            .flatMap(abstractNumId -> lookup(this.abstractNums, abstractNumId))
            .flatMap(abstractNum -> {
                if (abstractNum.numStyleLink.isPresent()) {
                    return abstractNum.numStyleLink
                        .flatMap(numStyleLink -> styles.findNumberingStyleById(numStyleLink))
                        .flatMap(style -> style.getNumId())
                        .flatMap(linkedNumId -> findLevel(linkedNumId, level));
                } else {
                    return lookup(abstractNum.levels, level).map(value -> value.toNumberingLevel());
                }
            });
    }

    public Optional<NumberingLevel> findLevelByParagraphStyleId(String styleId) {
        return lookup(levelsByParagraphStyleId, styleId).map(value -> value.toNumberingLevel());
    }
}
