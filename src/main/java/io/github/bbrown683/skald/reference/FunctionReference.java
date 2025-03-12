package io.github.bbrown683.skald.reference;

import lombok.Getter;
import org.apache.bcel.generic.Type;

import java.util.List;

@Getter
public class FunctionReference extends Reference {
    private final Type returnType;
    private final List<VariableReference> parameters;

    public FunctionReference(String name,
                             boolean isPublic,
                             boolean isStatic,
                             Type returnType,
                             List<VariableReference> parameters) {
        super(name, isPublic, isStatic);
        this.returnType = returnType;
        this.parameters = parameters;
    }
}
