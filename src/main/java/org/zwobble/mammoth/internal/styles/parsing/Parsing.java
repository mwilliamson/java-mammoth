package org.zwobble.mammoth.internal.styles.parsing;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.function.Function;

public class Parsing {
    public static <T, U extends BaseParser<T>, V> void parse(Class<U> parserType, Function<U, Rule> rule, String value) {
        U parser = Parboiled.createParser(parserType);
        ReportingParseRunner<T> runner = new ReportingParseRunner<>(rule.apply(parser));
        ParsingResult<T> result = runner.run(value);
        if (result.hasErrors()) {
            // TODO: wrap in result
            throw new RuntimeException("Parse error");
        }
    }
}
