package io.github.bbrown683.jasper.symbol.v1;

import lombok.Getter;
import org.apache.bcel.generic.Type;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Stack;

public class GlobalSymbolTable {
    private final SymbolTable symbolTable;
    @Getter
    private final List<Stack<Scope>> globalScopes = new Stack<>();

    public GlobalSymbolTable() {
        symbolTable = new SymbolTable();
        loadBaseTypes();
    }

    private void loadBaseTypes() {
        List<String> baseTypes = List.of("Integer", "Long", "System");
        for (String baseType : baseTypes) {
            symbolTable.enterScope();
            loadBaseType("java.lang." + baseType);
            symbolTable.exitScope();
        }
    }

    private void loadBaseType(String baseType) {
        try {
            var clazz = Class.forName(baseType);
            var className = clazz.getSimpleName();
            System.out.println("Scanning Class: " + className);
            symbolTable.addSymbol(baseType, new ObjectTypeSymbol(className, clazz.getPackageName(), true));
            globalScopes.add(symbolTable.getCurrentScope());

            for (var method : clazz.getMethods()) {
                symbolTable.enterScope();
                String methodName = method.getName();
                System.out.print("Found Method: " + methodName);

                var returnType = method.getReturnType();
                var isMethodStatic = Modifier.isStatic(method.getModifiers());
                symbolTable.addSymbol(methodName, new FunctionSymbol(methodName, Type.getType(returnType), true, isMethodStatic));
                globalScopes.add(symbolTable.getCurrentScope());

                for (var parameter : method.getParameters()) {
                    String name = parameter.getName();
                    System.out.print(", Parameter: " + parameter.getName());
                    var type = parameter.getType();
                    var modifiers = parameter.getModifiers();
                    symbolTable.addSymbol(name, new VariableSymbol(name, Type.getType(type), Modifier.isPublic(modifiers), Modifier.isStatic(modifiers), !Modifier.isFinal(modifiers), type.isArray(), true, type.isPrimitive(), null));
                    globalScopes.add(symbolTable.getCurrentScope());
                }
                symbolTable.exitScope();
                System.out.println();
            }

            for (var field : clazz.getFields()) {
                String name = field.getName();
                System.out.println("Found Field: " + name);
                var type = field.getType();
                var modifiers = field.getModifiers();
                symbolTable.addSymbol(name, new VariableSymbol(name, Type.getType(type), Modifier.isPublic(modifiers), Modifier.isStatic(modifiers), !Modifier.isFinal(modifiers), type.isArray(), true, type.isPrimitive(), null));
                globalScopes.add(symbolTable.getCurrentScope());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
