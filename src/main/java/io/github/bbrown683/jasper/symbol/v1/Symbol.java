package io.github.bbrown683.jasper.symbol.v1;

import lombok.*;
import org.antlr.v4.runtime.ParserRuleContext;

@Getter
public abstract class Symbol {
    protected String name;
    protected String line;
    protected Symbol parent;
    protected ParserRuleContext ctx; // The context in the parse tree where this symbol was declared

    public Symbol(String name) {
        this.name = name;
    }
}
