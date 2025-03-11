package io.github.bbrown683.jasper.symbol;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Symbol2 {
    protected String name;
    protected ParserRuleContext ctx; // The context in the parse tree where this symbol was declared
    protected Symbol2 parent;
    protected List<Symbol2> children = new ArrayList<>();

    public Symbol2(String name, ParserRuleContext ctx) {
        this.name = name;
        this.ctx = ctx;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addChild(Symbol2 child) {
        children.add(child);
        child.setParent(this);
    }
}
