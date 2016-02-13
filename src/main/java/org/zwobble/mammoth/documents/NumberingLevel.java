package org.zwobble.mammoth.documents;

public class NumberingLevel {
    public static NumberingLevel ordered(String levelIndex) {
        return new NumberingLevel(levelIndex, true);
    }

    private final String levelIndex;
    private final boolean isOrdered;

    public NumberingLevel(String levelIndex, boolean isOrdered) {
        this.levelIndex = levelIndex;
        this.isOrdered = isOrdered;
    }

    public String getLevelIndex() {
        return levelIndex;
    }

    public boolean isOrdered() {
        return isOrdered;
    }
}
