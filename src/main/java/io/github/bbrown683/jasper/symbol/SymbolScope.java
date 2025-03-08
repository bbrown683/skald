package io.github.bbrown683.jasper.symbol;

public class SymbolScope {
    private SymbolScope parentScope;

    public SymbolScope(SymbolScope parentScope) {
        this.parentScope = parentScope;
    }

    public SymbolScope getParentScope() {
        return parentScope;
    }
}
