package org.zwobble.mammoth.tests.archives;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.archives.ZipPaths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class ZipPathsTests {
    @Test
    public void splitPathSplitZipPathsOnLastForwardSlash() {
        assertThat(ZipPaths.splitPath("a/b"), isSplitPath("a", "b"));
        assertThat(ZipPaths.splitPath("a/b/c"), isSplitPath("a/b", "c"));
        assertThat(ZipPaths.splitPath("/a/b/c"), isSplitPath("/a/b", "c"));
    }

    @Test
    public void whenPathHasNoForwardSlashesThenSplitPathReturnsEmptyDirname() {
        assertThat(ZipPaths.splitPath("name"), isSplitPath("", "name"));
    }

    private Matcher<ZipPaths.SplitPath> isSplitPath(String dirname, String basename) {
        return deepEquals(new ZipPaths.SplitPath(dirname, basename));
    }

    @Test
    public void joinPathJoinsArgumentsWithForwardSlashes() {
        assertThat(ZipPaths.joinPath("a", "b"), equalTo("a/b"));
        assertThat(ZipPaths.joinPath("a/b", "c"), equalTo("a/b/c"));
        assertThat(ZipPaths.joinPath("/a/b", "c"), equalTo("/a/b/c"));
    }

    @Test
    public void emptyPartsAreIgnoredWhenJoiningPaths() {
        assertThat(ZipPaths.joinPath("a", ""), equalTo("a"));
        assertThat(ZipPaths.joinPath("", "b"), equalTo("b"));
        assertThat(ZipPaths.joinPath("a", "", "b"), equalTo("a/b"));
    }

    @Test
    public void whenJoiningPathsThenAbsolutePathsIgnoreEarlierPaths() {
        assertThat(ZipPaths.joinPath("a", "/b"), equalTo("/b"));
        assertThat(ZipPaths.joinPath("a", "/b", "c"), equalTo("/b/c"));
        assertThat(ZipPaths.joinPath("/a", "/b"), equalTo("/b"));
        assertThat(ZipPaths.joinPath("/a"), equalTo("/a"));
    }
}
