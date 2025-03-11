package io.github.bbrown683.jasper.reference;

import lombok.AllArgsConstructor;
import org.apache.bcel.generic.Type;

import java.util.List;

public record FunctionReference(String name,
    Type returnType,
    boolean isPublic,
    boolean isStatic,
    boolean isMutable,
    List<VariableReference> parameters) {}
