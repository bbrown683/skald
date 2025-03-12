package io.github.bbrown683.skald.symbol;

import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

@Getter
@ToString
public class FunctionSymbol extends Symbol {
    private final Type returnType;
    private final boolean isPublic;
    private final boolean isStatic;

    public FunctionSymbol(String name, ParserRuleContext ctx, Type returnType, boolean isPublic, boolean isStatic) {
        super(name, ctx);
        this.returnType = returnType;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}