package io.github.bbrown683.jasper.symbol;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;

@Getter
public class ClassFileSymbol2 extends Symbol2 {
    public ClassFileSymbol2(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
