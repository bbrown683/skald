package io.github.bbrown683.skald.symbol.external;

import org.apache.bcel.generic.Type;

import java.util.List;

public class ExternalFunctionSymbol extends ExternalSymbol {
    private final Type returnType;
    private final List<ExternalVariableSymbol> parameters;

    public ExternalFunctionSymbol(String name,
                                  boolean isPublic,
                                  boolean isStatic,
                                  Type returnType,
                                  List<ExternalVariableSymbol> parameters) {
        super(name, isPublic, isStatic);
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<ExternalVariableSymbol> getParameters() {
        return parameters;
    }
}
