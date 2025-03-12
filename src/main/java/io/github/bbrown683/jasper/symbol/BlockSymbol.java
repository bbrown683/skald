package io.github.bbrown683.jasper.symbol;

import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;

@Getter
@ToString
public class BlockSymbol extends Symbol {
    public BlockSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
