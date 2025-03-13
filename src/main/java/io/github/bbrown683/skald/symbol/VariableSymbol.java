package io.github.bbrown683.skald.symbol;

import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

@Getter
@ToString
public class VariableSymbol extends Symbol {
    private final Type type;
    private final boolean isPublic;
    private final boolean isStatic;
    private final boolean isMutable;
    private final boolean isArray;
    private final boolean isParameter;
    private final Object value;

    public VariableSymbol(String name, ParserRuleContext ctx, Type type, final boolean isPublic, final boolean isStatic, final boolean isMutable, final boolean isArray, final boolean isParameter, Object value) {
        super(name, ctx);
        this.type = type;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMutable = isMutable;
        this.isArray = isArray;
        this.isParameter = isParameter;
        this.value = value;
    }
}
