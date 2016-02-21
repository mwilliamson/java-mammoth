package org.zwobble.mammoth.styles;

import com.google.common.collect.ImmutableList;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.List;

import static org.zwobble.mammoth.util.MammothLists.list;
import static org.zwobble.mammoth.util.MammothMaps.map;

public class StyleMapParser extends BaseParser<Object> {
    public static HtmlPath parseHtmlPath(String value) {
        StyleMapParser parser = Parboiled.createParser(StyleMapParser.class);
        ReportingParseRunner runner = new ReportingParseRunner(parser.HtmlPath());
        ParsingResult<?> result = runner.run(value);
        if (result.hasErrors()) {
            // TODO: wrap in result
            throw new RuntimeException("Parse error");
        } else {
            return (HtmlPath) result.valueStack.peek();
        }
    }

    Rule HtmlPath() {
        ListVar<HtmlPathElement> elements = new ListVar<>();
        return Sequence(HtmlPathElements(elements), EOI, push(new HtmlPath(elements.build())));
    }

    Rule HtmlPathElements(ListVar<HtmlPathElement> elements) {
        Var<HtmlPathElement> element = new Var<>();
        return FirstOf(
            Sequence(
                HtmlPathElement(element),
                elements.append(element),
                ZeroOrMore(Sequence(Whitespace(), Ch('>'), Whitespace(), HtmlPathElement(element), elements.append(element)))),
            toRule(push(ImmutableList.builder())));
    }

    Rule HtmlPathElement(Var<HtmlPathElement> element) {
        Var<String> tagName = new Var<>();
        ListVar<String> classNames = new ListVar<>();
        Var<Boolean> fresh = new Var<>();
        return Sequence(
            TagNames(tagName),
            ClassNames(classNames),
            Fresh(fresh),
            element.set(new HtmlPathElement(
                list(tagName.get()),
                classNames.isEmpty() ? map() : map("class", String.join(" ", classNames.build())),
                !fresh.get())));
    }

    Rule TagNames(Var<String> tagName) {
        return Sequence(Identifier(), tagName.set(match()));
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

    class ListVar<T> extends Var<ImmutableList.Builder<T>> {
        boolean append(Var<T> element) {
            if (get() == null) {
                set(ImmutableList.builder());
            }
            return set(get().add(element.get()));
        }

        public List<T> build() {
            if (get() == null) {
                return list();
            } else {
                return get().build();
            }
        }

        public boolean isEmpty() {
            return get() == null;
        }
    }
}
