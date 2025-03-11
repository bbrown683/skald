package io.github.bbrown683.jasper.symbol.v1;

import java.util.Stack;

public class SymbolTable {
    private final Stack<Scope> scopes = new Stack<>();

    public SymbolTable() {
        scopes.push(new Scope());
    }

    public void enterScope() {
        scopes.push(new Scope());
    }

    public void exitScope() {
        if(scopes.size() > 1) {
            scopes.pop();
        }
    }

    public void addSymbol(String name, Symbol symbol) {
        scopes.peek().addSymbol(name, symbol);
    }

    public Symbol getSymbol(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            var currentScope = scopes.get(i);
            if (currentScope.containsSymbol(name)) {
                return currentScope.getSymbol(name);
            }
        }
        return null; // Symbol not found
    }

    public boolean containsSymbol(String name) {
        return getSymbol(name) != null;
    }

    public Stack<Scope> getCurrentScope() {
        return (Stack<Scope>)scopes.clone();
    }
}
