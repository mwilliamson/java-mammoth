package org.zwobble.mammoth.tests.conversion;

import org.junit.jupiter.api.Test;
import org.zwobble.mammoth.internal.conversion.InternalImageConverter;
import org.zwobble.mammoth.internal.documents.Image;
import org.zwobble.mammoth.internal.html.Html;
import org.zwobble.mammoth.internal.html.HtmlNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.mammoth.internal.util.Lists.list;
import static org.zwobble.mammoth.internal.util.Maps.map;
import static org.zwobble.mammoth.tests.DeepReflectionMatcher.deepEquals;

public class InternalImageConverterTests {
    @Test
    public void whenElementDoesNotHaveAltTextThenAltAttributeIsNotSet() throws IOException {
        Image internalImage = new Image(
            Optional.empty(),
            Optional.of("image/jpeg"),
            () -> new ByteArrayInputStream(new byte[]{97, 98, 99})
        );

        InternalImageConverter imageConverter = InternalImageConverter.imgElement(image -> {
            return map("src", "<src>");
        });
        List<HtmlNode> result = imageConverter.convert(internalImage);

        assertThat(
            result,
            deepEquals(list(Html.element("img", map("src", "<src>"))))
        );
    }

    @Test
    public void whenElementHasAltTextThenAltAttributeIsSet() throws IOException {
        Image internalImage = new Image(
            Optional.empty(),
            Optional.of("image/jpeg"),
            () -> new ByteArrayInputStream(new byte[]{97, 98, 99})
        );

        InternalImageConverter imageConverter = InternalImageConverter.imgElement(image -> {
            return map("alt", "<alt>", "src", "<src>");
        });
        List<HtmlNode> result = imageConverter.convert(internalImage);

        assertThat(
            result,
            deepEquals(list(Html.element("img", map("alt", "<alt>", "src", "<src>"))))
        );
    }
}
