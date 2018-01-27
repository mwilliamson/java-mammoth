package org.zwobble.mammoth.internal.archives;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.mammoth.internal.util.Lists.eagerFilter;

public class ZipPaths {
    private ZipPaths() {
    }

    public static class SplitPath {
        private final String dirname;
        private final String basename;

        public SplitPath(String dirname, String basename) {
            this.dirname = dirname;
            this.basename = basename;
        }

        public String getDirname() {
            return dirname;
        }

        public String getBasename() {
            return basename;
        }
    }

    public static SplitPath splitPath(String path) {
        int index = path.lastIndexOf("/");
        if (index == -1) {
            return new SplitPath("", path);
        } else {
            String dirname = path.substring(0, index);
            String basename = path.substring(index + 1);
            return new SplitPath(dirname, basename);
        }
    }

    public static String joinPath(String... paths) {
        List<String> nonEmptyPaths = eagerFilter(asList(paths), path -> !path.isEmpty());

        List<String> relevantPaths = new ArrayList<>();
        for (String path : nonEmptyPaths) {
            if (path.startsWith("/")) {
                relevantPaths.clear();
            }
            relevantPaths.add(path);
        }
        return String.join("/", relevantPaths);
    }
}
