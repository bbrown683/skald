package io.github.bbrown683.skald.reference;

import org.apache.bcel.generic.Type;

import java.util.List;

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

    public Type getReturnType() {
        return returnType;
    }

    public List<VariableReference> getParameters() {
        return parameters;
    }
}
