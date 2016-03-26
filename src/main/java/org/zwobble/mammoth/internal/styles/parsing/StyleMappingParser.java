package org.zwobble.mammoth.internal.styles.parsing;

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.support.Var;
import org.zwobble.mammoth.internal.documents.NumberingLevel;
import org.zwobble.mammoth.internal.styles.*;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.zwobble.mammoth.internal.util.Maps.map;

public class StyleMappingParser extends BaseParser<StyleMapBuilder> {
    Rule StyleMapping(Var<StyleMapBuilder> styleMap) {
        Var<BiFunction<StyleMapBuilder, HtmlPath, StyleMapBuilder>> documentElementMatcher = new Var<>();
        Var<HtmlPath> path = new Var<>();
        return Sequence(
            DocumentElementMatcher(documentElementMatcher), Whitespace(), "=>", Whitespace(), HtmlPath(path), EOI,
            styleMap.set(documentElementMatcher.get().apply(styleMap.get(), path.get())));
    }

    Rule DocumentElementMatcher(Var<BiFunction<StyleMapBuilder, HtmlPath, StyleMapBuilder>> updateBuilder) {
        Var<ParagraphMatcher> paragraphMatcher = new Var<>();
        Var<RunMatcher> runMatcher = new Var<>();
        return FirstOf(
            Sequence(
                ParagraphMatcher(paragraphMatcher),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set((styleMap, htmlPath) -> styleMap.mapParagraph(paragraphMatcher.get(), htmlPath));
                        return true;
                    }
                }),
            Sequence(
                RunMatcher(runMatcher),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set((styleMap, htmlPath) -> styleMap.mapRun(runMatcher.get(), htmlPath));
                        return true;
                    }
                }),
            Sequence(
                "b",
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set(StyleMapBuilder::bold);
                        return true;
                    }
                }),
            Sequence(
                "i",
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set(StyleMapBuilder::italic);
                        return true;
                    }
                }),
            Sequence(
                "u",
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set(StyleMapBuilder::underline);
                        return true;
                    }
                }),
            Sequence(
                "strike",
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        updateBuilder.set(StyleMapBuilder::strikethrough);
                        return true;
                    }
                }));
    }

    public Rule ParagraphMatcher(Var<ParagraphMatcher> matcher) {
        Var<Optional<String>> styleId = new Var<>();
        Var<Optional<String>> styleName = new Var<>();
        Var<Optional<NumberingLevel>> numbering = new Var<>();
        return Sequence(
            "p", StyleId(styleId), StyleName(styleName), Numbering(numbering),
            matcher.set(new ParagraphMatcher(styleId.get(), styleName.get(), numbering.get())));
    }

    public Rule RunMatcher(Var<RunMatcher> matcher) {
        Var<Optional<String>> styleId = new Var<>();
        Var<Optional<String>> styleName = new Var<>();
        return Sequence(
            "r", StyleId(styleId), StyleName(styleName),
            matcher.set(new RunMatcher(styleId.get(), styleName.get())));
    }

    Rule StyleId(Var<Optional<String>> styleId) {
        return Sequence(styleId.set(Optional.empty()), Optional(".", Identifier(), styleId.set(Optional.of(match()))));
    }

    Rule StyleName(Var<Optional<String>> styleName) {
        Var<String> value = new Var<>();
        return Sequence(
            styleName.set(Optional.empty()),
            Optional("[style-name=", SingleQuotedString(value), "]", styleName.set(Optional.of(value.get()))));
    }

    Rule Numbering(Var<Optional<NumberingLevel>> numbering) {
        Var<Boolean> isOrdered = new Var<>();
        Var<BigInteger> level = new Var<>();
        return Sequence(
            numbering.set(Optional.empty()),
            Optional(Sequence(
                ":", NumberingType(isOrdered), "(", Number(level), ")",
                numbering.set(Optional.of(new NumberingLevel(level.get().subtract(BigInteger.ONE).toString(), isOrdered.get()))))));
    }

    Rule NumberingType(Var<Boolean> isOrdered) {
        return FirstOf(
            Sequence("ordered-list", isOrdered.set(true)),
            Sequence("unordered-list", isOrdered.set(false)));
    }

    Rule Number(Var<BigInteger> level) {
        return Sequence(
            OneOrMore(CharRange('0', '9')),
            level.set(new BigInteger(match())));
    }

    public Rule HtmlPath(Var<HtmlPath> htmlPath) {
        ListVar<HtmlPathElement> elements = new ListVar<>();
        return Sequence(Optional(HtmlPathElements(elements)), htmlPath.set(new HtmlPath(elements.get())));
    }

    Rule HtmlPathElements(ListVar<HtmlPathElement> elements) {
        Var<HtmlPathElement> element = new Var<>();
        return Sequence(
            HtmlPathElement(element),
            elements.append(element),
            ZeroOrMore(Sequence(Whitespace(), Ch('>'), Whitespace(), HtmlPathElement(element), elements.append(element))));
    }

    Rule HtmlPathElement(Var<HtmlPathElement> element) {
        ListVar<String> tagNames = new ListVar<>();
        ListVar<String> classNames = new ListVar<>();
        Var<Boolean> fresh = new Var<>();
        return Sequence(
            TagNames(tagNames),
            ClassNames(classNames),
            Fresh(fresh),
            element.set(new HtmlPathElement(
                tagNames.get(),
                classNames.isEmpty() ? map() : map("class", String.join(" ", classNames.get())),
                !fresh.get())));
    }

    Rule TagNames(ListVar<String> tagNames) {
        return Sequence(
            Identifier(),
            tagNames.append(match()),
            ZeroOrMore(Sequence("|", Identifier(), tagNames.append(match()))));
    }

    Rule ClassNames(ListVar<String> classNames) {
        Var<String> className = new Var<>();
        return ZeroOrMore(Sequence(ClassName(className), classNames.append(className)));
    }

    Rule ClassName(Var<String> className) {
        return Sequence(".", Identifier(), className.set(match()));
    }

    Rule Fresh(Var<Boolean> fresh) {
        return Sequence(fresh.set(false), Optional(":fresh", fresh.set(true)));
    }

    Rule SingleQuotedString(Var<String> value) {
        return Sequence("'", ZeroOrMore(NoneOf("\'")), value.set(match()), "'");
    }

    Rule Identifier() {
        return Sequence(
            FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')),
            ZeroOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), Ch('-'), Ch('_'))));
    }

    Rule Whitespace() {
        return ZeroOrMore(AnyOf(" \t"));
    }
}
