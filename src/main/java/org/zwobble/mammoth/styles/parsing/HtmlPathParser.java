package org.zwobble.mammoth.styles.parsing;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;
import org.zwobble.mammoth.styles.HtmlPath;
import org.zwobble.mammoth.styles.HtmlPathElement;

import static org.zwobble.mammoth.util.MammothMaps.map;

public class HtmlPathParser extends BaseParser<HtmlPath> {
    public Rule HtmlPath() {
        ListVar<HtmlPathElement> elements = new ListVar<>();
        return Sequence(Optional(HtmlPathElements(elements)), EOI, push(new HtmlPath(elements.build())));
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
                tagNames.build(),
                classNames.isEmpty() ? map() : map("class", String.join(" ", classNames.build())),
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

    Rule Identifier() {
        return Sequence(
            FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')),
            ZeroOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), Ch('-'), Ch('_'))));
    }

    Rule Whitespace() {
        return ZeroOrMore(AnyOf(" \t"));
    }
}
