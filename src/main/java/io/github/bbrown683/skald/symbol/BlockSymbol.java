package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;

public class BlockSymbol extends Symbol {
    public BlockSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
