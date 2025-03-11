package io.github.bbrown683.jasper.reference;

import java.util.List;

public record TypeReference(
    String name,
    String packagePath,
    boolean isPublic,
    boolean isStatic,
    boolean isMutable,
    List<VariableReference> fields,
    List<FunctionReference> functions,
    List<TypeReference> subtypes
) {}