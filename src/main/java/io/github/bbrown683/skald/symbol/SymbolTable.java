package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<ParserRuleContext,Symbol> symbolMap = new HashMap<>();
    private Symbol currentSymbol;
    private Symbol previousSymbol;

    public SymbolTable(Symbol rootSymbol) {
        currentSymbol = rootSymbol;
        symbolMap.put(currentSymbol.getCtx(), currentSymbol);
    }

    // Goes down a level in the symbol table
    public void enterScope() {
        currentSymbol = previousSymbol;
    }

    // Goes up a level in the symbol table
    public void exitScope() {
        currentSymbol = currentSymbol.getParent();
    }

    // Adds a symbol at the current level in the symbol table
    public void addSymbol(Symbol symbol) {
        currentSymbol.addChild(symbol);
        symbolMap.put(symbol.getCtx(), symbol);
        previousSymbol = symbol;
    }

    public Symbol getSymbol(ParserRuleContext ctx) {
        return symbolMap.get(ctx);
    }

    public boolean containsSymbol(ParserRuleContext ctx) {
        return symbolMap.containsKey(ctx);
    }
}
