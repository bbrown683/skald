package io.github.bbrown683.jasper.symbol;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String,Symbol> symbols = new HashMap<>();

    public void addSymbol(String name, Symbol symbol) {
        symbols.put(name, symbol);
    }

    public Symbol getSymbol(String name) {
        return symbols.get(name);
    }

    public boolean containsSymbol(String name) {
        return symbols.containsKey(name);
    }
}
