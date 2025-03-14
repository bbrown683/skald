package io.github.bbrown683.skald.symbol.local;

import org.antlr.v4.runtime.ParserRuleContext;

public class LocalClassFileSymbol extends LocalSymbol {
    public LocalClassFileSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
