package io.github.bbrown683.skald.reference;

import lombok.Getter;

import java.util.List;

@Getter
public class TypeReference extends Reference {
    private final List<VariableReference> fields;
    private final List<FunctionReference> functions;
    private final List<TypeReference> subTypes;
    private final String superType;

    public TypeReference(String name,
                         boolean isPublic,
                         boolean isStatic,
                         List<VariableReference> fields,
                         List<FunctionReference> functions,
                         List<TypeReference> subTypes,
                         String superType) {
        super(name, isPublic, isStatic);
        this.fields = fields;
        this.functions = functions;
        this.subTypes = subTypes;
        this.superType = superType;
    }
}