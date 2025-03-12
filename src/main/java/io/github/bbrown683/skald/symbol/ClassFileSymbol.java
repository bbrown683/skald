package io.github.bbrown683.skald.symbol;

import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;

@Getter
@ToString
public class ClassFileSymbol extends Symbol {
    public ClassFileSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
