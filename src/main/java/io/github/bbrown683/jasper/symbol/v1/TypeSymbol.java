package io.github.bbrown683.jasper.symbol.v1;

import lombok.Getter;

@Getter
public sealed abstract class TypeSymbol extends Symbol permits ObjectTypeSymbol {
    private final String packagePath;
    private final boolean isPublic;

    public TypeSymbol(String name, String packagePath, boolean isPublic) {
        super(name);
        this.packagePath = packagePath;
        this.isPublic = isPublic;
    }
}