package io.github.bbrown683.jasper.symbol;

import lombok.Builder;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.Type;

@Getter
public class VariableSymbol2 extends Symbol2 {
    private final Type type;
    private final boolean isPublic;
    private final boolean isStatic;
    private final boolean isMutable;
    private final boolean isArray;
    private final boolean isParameter;
    private final boolean isLiteral;
    private final Object value;

    public VariableSymbol2(String name, ParserRuleContext ctx, Type type, final boolean isPublic, final boolean isStatic, final boolean isMutable, final boolean isArray, final boolean isParameter, final boolean isLiteral, Object value) {
        super(name, ctx);
        this.type = type;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMutable = isMutable;
        this.isArray = isArray;
        this.isParameter = isParameter;
        this.isLiteral = isLiteral;
        this.value = value;
    }
}
