package io.github.bbrown683.jasper.symbol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeSymbol extends Symbol {
    private String packagePath;
    private boolean isPublic;
    private boolean isObject;
    private boolean isEnum;
    private boolean isUnion;

    public TypeSymbol(String name, String packagePath, boolean isPublic, boolean isObject, boolean isEnum, boolean isUnion) {
        super(name);
        this.packagePath = packagePath;
    }
}