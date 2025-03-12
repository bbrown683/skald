package io.github.bbrown683.jasper.reference;

import lombok.Getter;
import org.apache.bcel.generic.Type;

@Getter
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
}