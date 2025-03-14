package io.github.bbrown683.skald.symbol.local;

import org.antlr.v4.runtime.ParserRuleContext;

// This class marks the location in the tree for a particular symbol, without carrying any other metadata.
// Ignored in searches.
public class LocalMarkerSymbol extends LocalSymbol {
    public LocalMarkerSymbol(String name, ParserRuleContext ctx) {
        super(name, ctx);
    }
}
