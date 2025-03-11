package io.github.bbrown683.jasper.reference;

import org.apache.bcel.generic.Type;

public record VariableReference(
    String name,
    Type type,
    boolean isPublic,
    boolean isStatic,
    boolean isMutable) {}
