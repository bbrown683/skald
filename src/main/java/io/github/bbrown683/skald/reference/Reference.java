package io.github.bbrown683.skald.reference;

import lombok.Getter;

@Getter
public abstract class Reference {
    private final String name;
    private final boolean isPublic;
    private final boolean isStatic;

    public Reference(String name, boolean isPublic, boolean isStatic) {
        this.name = name;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }
}
