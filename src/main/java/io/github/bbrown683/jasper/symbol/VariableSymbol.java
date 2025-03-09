package io.github.bbrown683.jasper.symbol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.bcel.generic.Type;

@Getter
@Setter
public class VariableSymbol extends Symbol {
    private Type type;
    private boolean isPublic;
    private boolean isStatic;
    private boolean isMutable;
    private boolean isArray;
    private boolean isParameter;
    private boolean isLiteral;
    private Object value;

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
