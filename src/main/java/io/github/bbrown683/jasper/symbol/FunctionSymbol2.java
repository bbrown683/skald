package io.github.bbrown683.jasper.symbol;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

@Getter
public class FunctionSymbol2 extends Symbol2 {
    private final Type returnType;
    private final boolean isPublic;
    private final boolean isStatic;

    public FunctionSymbol2(String name, ParserRuleContext ctx, Type returnType, boolean isPublic, boolean isStatic) {
        super(name, ctx);
        this.returnType = returnType;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}