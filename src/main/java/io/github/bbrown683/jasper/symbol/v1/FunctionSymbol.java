package io.github.bbrown683.jasper.symbol.v1;

import lombok.Getter;
import org.apache.bcel.generic.Type;

@Getter
public class FunctionSymbol extends Symbol {
    private final Type returnType;
    private final boolean isPublic;
    private final boolean isStatic;
    public FunctionSymbol(String name, Type returnType, boolean isPublic, boolean isStatic) {
        super(name);
        this.returnType = returnType;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}
