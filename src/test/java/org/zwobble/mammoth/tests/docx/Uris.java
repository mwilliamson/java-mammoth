package org.zwobble.mammoth.tests.docx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.mammoth.internal.docx.Uris.uriToZipEntryName;

public class Uris {
    @Test
    public void whenPathDoesNotHaveLeadingSlashThenPathIsResolvedRelativeToBase() {
        assertThat(
            uriToZipEntryName("one/two", "three/four"),
            equalTo("one/two/three/four")
        );
    }

    @Test
    public void whenPathHasLeadingSlashThenBaseIsIgnored() {
        assertThat(
            uriToZipEntryName("one/two", "/three/four"),
            equalTo("three/four")
        );
    }
}
