using Mammoth.Couscous.java.util;
using Mammoth.Couscous.org.zwobble.mammoth.@internal.styles;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.styles.parsing {
    internal static class StyleMapParser {
        internal static StyleMap parseStyleMappings(List<string> mappings) {
            return DefaultStyleMap();
        }
        
        internal static StyleMap parse(string styleMap) {
            return DefaultStyleMap();
        }

        private static StyleMap DefaultStyleMap() {
            return StyleMap.builder()

                .mapParagraph(ParagraphMatcher.styleName("Normal"), HtmlPath.element("p"))

                .build();
        }
    }
}
