package io.github.bbrown683.skald.symbol.local;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

// Local symbols are symbols visible to the ClassFile they are declared in.
// They provide scope information.
public abstract class LocalSymbol {
    protected final String name;
    protected final ParserRuleContext ctx; // The context in the parse tree where this symbol was declared
    protected LocalSymbol parent;
    protected final List<LocalSymbol> children = new ArrayList<>();

    public LocalSymbol(String name, ParserRuleContext ctx) {
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

    public void addChild(LocalSymbol child) {
        children.add(child);
        child.parent = this;
    }

    // Returns a list of all visible symbols. Does not return block symbols.
    // Work up the symbol table hierarchy until the root symbol is reached.
    public List<LocalSymbol> getVisibleSymbols() {
        List<LocalSymbol> visibleSymbols = new ArrayList<>();
        var symbol = this;
        while (symbol != null) {
            var parent = symbol.parent;
            if(parent != null) { // Scan under parent for symbols
                var parentChildren = parent.children;
                // Check for symbols before the current symbol in the parent's children list
                var symbolIndex = parentChildren.indexOf(symbol);
                for(int i = 0; i < symbolIndex; i++) {
                    var sibling = parentChildren.get(i);
                    if (!(sibling instanceof LocalMarkerSymbol) && !visibleSymbols.contains(sibling))
                        visibleSymbols.add(sibling);
                }
            } else { // Add symbols from the root symbol
                for(var child : symbol.children) {
                    if (child != this && !(child instanceof LocalMarkerSymbol) && !visibleSymbols.contains(child))
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

    public LocalSymbol getParent() {
        return parent;
    }

    public List<LocalSymbol> getChildren() {
        return children;
    }

    // TODO: Should probably be a list, in the event of overloading
    public <T extends LocalSymbol> T findSymbol(String symbolName, Class<T> clazz) {
        var visibleSymbols = getVisibleSymbols();
        for (var visibleSymbol : visibleSymbols) {
            if(symbolName.equals(visibleSymbol.getName()) && visibleSymbol.getClass().equals(clazz)) {
                return clazz.cast(visibleSymbol);
            }
        }
        return null;
    }
}
