package io.github.bbrown683.skald.symbol;

import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;

@Getter
@ToString
public class TypeSymbol extends Symbol {
    private final String packagePath;
    private final boolean isPublic;
    private final boolean isStatic;

    public TypeSymbol(String name, ParserRuleContext ctx, String packagePath, boolean isPublic, boolean isStatic) {
        super(name, ctx);
        this.packagePath = packagePath;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}
