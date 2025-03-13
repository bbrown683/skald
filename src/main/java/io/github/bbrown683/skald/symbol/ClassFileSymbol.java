package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;

public class ClassFileSymbol extends Symbol {
    public ClassFileSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
