package io.github.bbrown683.jasper.symbol;

import lombok.Builder;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TypeSymbol2 extends Symbol2 {
    private final boolean isPublic;
    private final boolean isStatic;

    public TypeSymbol2(String name, ParserRuleContext ctx, boolean isPublic, boolean isStatic) {
        super(name, ctx);
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}
