package io.github.bbrown683.skald.reference;

import org.apache.bcel.generic.Type;

public class VariableReference extends Reference {
    private final Type type;
    private final boolean isMutable;
    private final boolean isArray;

    public VariableReference(String name,
                             boolean isPublic,
                             boolean isStatic,
                             Type type,
                             boolean isMutable,
                             boolean isArray) {
        super(name, isPublic, isStatic);
        this.type = type;
        this.isMutable = isMutable;
        this.isArray = isArray;
    }

    public Type getType() {
        return type;
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isArray() {
        return isArray;
    }
}