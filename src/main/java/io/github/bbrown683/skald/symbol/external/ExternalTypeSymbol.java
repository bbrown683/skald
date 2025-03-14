package io.github.bbrown683.skald.symbol.external;

import java.util.List;

public class ExternalTypeSymbol extends ExternalSymbol {
    private final List<ExternalVariableSymbol> fields;
    private final List<ExternalFunctionSymbol> functions;
    private final List<ExternalTypeSymbol> subTypes;
    private final String superType;

    public ExternalTypeSymbol(String name,
                              boolean isPublic,
                              boolean isStatic,
                              List<ExternalVariableSymbol> fields,
                              List<ExternalFunctionSymbol> functions,
                              List<ExternalTypeSymbol> subTypes,
                              String superType) {
        super(name, isPublic, isStatic);
        this.fields = fields;
        this.functions = functions;
        this.subTypes = subTypes;
        this.superType = superType;
    }

    public List<ExternalVariableSymbol> getFields() {
        return fields;
    }

    public List<ExternalFunctionSymbol> getFunctions() {
        return functions;
    }

    public List<ExternalTypeSymbol> getSubTypes() {
        return subTypes;
    }

    public String getSuperType() {
        return superType;
    }
}