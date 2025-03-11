package io.github.bbrown683.jasper.symbol;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable2 {
    private final Map<ParserRuleContext,Symbol2> symbolMap = new HashMap<>();
    private Symbol2 currentSymbol;
    private Symbol2 previousSymbol;

    public SymbolTable2(Symbol2 rootSymbol) {
        currentSymbol = rootSymbol;
        symbolMap.put(currentSymbol.getCtx(), currentSymbol);
    }

    // Goes down a level in the symbol table
    public void enterScope() {
//        currentSymbol = symbol;
        currentSymbol = previousSymbol;
    }

    // Goes up a level in the symbol table
    public void exitScope() {
        currentSymbol = currentSymbol.getParent();
    }

    // Adds a symbol at the current level in the symbol table
    public void addSymbol(Symbol2 symbol) {
//        symbol.parent = currentSymbol;
//        currentSymbol.getChildren().add(symbol);
        currentSymbol.addChild(symbol);
        symbolMap.put(symbol.getCtx(), symbol);
        previousSymbol = symbol;
    }

    public Symbol2 getSymbol(ParserRuleContext ctx) {
        return symbolMap.get(ctx);
    }

    public boolean containsSymbol(ParserRuleContext ctx) {
        return symbolMap.containsKey(ctx);
    }
}
