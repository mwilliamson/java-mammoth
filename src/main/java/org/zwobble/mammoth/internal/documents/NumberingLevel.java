package org.zwobble.mammoth.internal.documents;

import java.util.Optional;

public class NumberingLevel {
    public static NumberingLevel ordered(String levelIndex) {
        return new NumberingLevel(levelIndex, true);
    }

    public static NumberingLevel unordered(String levelIndex) {
        return new NumberingLevel(levelIndex, false);
    }

    private final String levelIndex;
    private final boolean isOrdered;
    /**
     * The id of the numbering
     */
    private final Optional<String> numberingID;

    public NumberingLevel(Optional<String> numberingID, String levelIndex, boolean isOrdered) {
      this.levelIndex = levelIndex;
      this.isOrdered = isOrdered;
      this.numberingID = numberingID;
    }
    
    public NumberingLevel(String levelIndex, boolean isOrdered) {
        this(Optional.empty(), levelIndex, isOrdered);
    }

    public String getLevelIndex() {
        return levelIndex;
    }

    public boolean isOrdered() {
        return isOrdered;
    }
    
    public Optional<String> getNumberingID() {
      return numberingID;
    }
}
