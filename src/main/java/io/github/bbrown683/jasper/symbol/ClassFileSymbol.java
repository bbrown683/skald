package io.github.bbrown683.jasper.symbol;

import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;

@ToString
public class ClassFileSymbol extends Symbol {
    public ClassFileSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
