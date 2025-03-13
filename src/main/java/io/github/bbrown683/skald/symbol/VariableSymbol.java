package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

public class VariableSymbol extends Symbol {
    private final Type type;
    private final boolean isPublic;
    private final boolean isStatic;
    private final boolean isMutable;
    private final boolean isArray;
    private final boolean isParameter;
    private final Object value;

    public VariableSymbol(String name, ParserRuleContext ctx, Type type, boolean isPublic, boolean isStatic, boolean isMutable, boolean isArray, boolean isParameter, Object value) {
        super(name, ctx);
        this.type = type;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMutable = isMutable;
        this.isArray = isArray;
        this.isParameter = isParameter;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isParameter() {
        return isParameter;
    }

    public Object getValue() {
        return value;
    }
}
