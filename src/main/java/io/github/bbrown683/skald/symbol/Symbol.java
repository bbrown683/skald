package io.github.bbrown683.skald.symbol;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Symbol {
    protected final String name;
    protected final ParserRuleContext ctx; // The context in the parse tree where this symbol was declared
    protected Symbol parent;
    protected final List<Symbol> children = new ArrayList<>();

    public Symbol(String name, ParserRuleContext ctx) {
        this.name = name;
        this.ctx = ctx;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    private int getDepth() {
        if (parent == null) {
            return 0;
        } else {
            return parent.getDepth() + 1;
        }
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addChild(Symbol child) {
        children.add(child);
        child.parent = this;
    }

    // Returns a list of all visible symbols. Does not return block symbols.
    // Work up the symbol table hierarchy until the root symbol is reached.
    public List<Symbol> getVisibleSymbols() {
        List<Symbol> visibleSymbols = new ArrayList<>();
        var symbol = this;
        while (symbol != null) {
            var parent = symbol.parent;
            if(parent != null) { // Scan under parent for symbols
                var parentChildren = parent.children;
                // Check for symbols before the current symbol in the parent's children list
                var symbolIndex = parentChildren.indexOf(symbol);
                for(int i = 0; i < symbolIndex; i++) {
                    var sibling = parentChildren.get(i);
                    if (!(sibling instanceof BlockSymbol) && !visibleSymbols.contains(sibling))
                        visibleSymbols.add(sibling);
                }
            } else { // Add symbols from the root symbol
                for(var child : symbol.children) {
                    if (child != this && !(child instanceof BlockSymbol) && !visibleSymbols.contains(child))
                        visibleSymbols.add(child);
                }
            }
            symbol = parent;
        }
        return visibleSymbols;
    }

    public String getName() {
        return name;
    }

    public ParserRuleContext getCtx() {
        return ctx;
    }

    public Symbol getParent() {
        return parent;
    }

    public List<Symbol> getChildren() {
        return children;
    }
}
