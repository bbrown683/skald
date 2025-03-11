package io.github.bbrown683.jasper.symbol.v1;

import lombok.Getter;
import org.apache.bcel.generic.Type;

@Getter
public class VariableSymbol extends Symbol {
    private final Type type;
    private final boolean isPublic;
    private final boolean isStatic;
    private final boolean isMutable;
    private final boolean isArray;
    private final boolean isParameter;
    private final boolean isLiteral;
    private final Object value;

    public VariableSymbol(String name, Type type, boolean isPublic, boolean isStatic, boolean isMutable, boolean isArray, boolean isParameter, boolean isLiteral, Object value) {
        super(name);
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
