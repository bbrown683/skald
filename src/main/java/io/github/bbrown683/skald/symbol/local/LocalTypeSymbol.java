package io.github.bbrown683.skald.symbol.local;

import org.antlr.v4.runtime.ParserRuleContext;

public class LocalTypeSymbol extends LocalSymbol {
    private final String packagePath;
    private final boolean isPublic;
    private final boolean isStatic;

    public LocalTypeSymbol(String name, ParserRuleContext ctx, String packagePath, boolean isPublic, boolean isStatic) {
        super(name, ctx);
        this.packagePath = packagePath;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
