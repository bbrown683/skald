package io.github.bbrown683.skald.reference;

import java.util.List;

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

    public List<VariableReference> getFields() {
        return fields;
    }

    public List<FunctionReference> getFunctions() {
        return functions;
    }

    public List<TypeReference> getSubTypes() {
        return subTypes;
    }

    public String getSuperType() {
        return superType;
    }
}