package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

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

    public Type getReturnType() {
        return returnType;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isStatic() {
        return isStatic;
    }
}