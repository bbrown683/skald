package io.github.bbrown683.jasper.symbol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.bcel.generic.Type;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FunctionSymbol extends Symbol {
    private Type returnType;
    private boolean isPublic;
    private boolean isStatic;
    private final List<VariableSymbol> parameters = new ArrayList<>();

    public FunctionSymbol(String name, Type returnType, boolean isPublic, boolean isStatic) {
        super(name);
        this.returnType = returnType;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }

    public void addParameter(VariableSymbol parameter) {
        parameters.add(parameter);
    }
}
