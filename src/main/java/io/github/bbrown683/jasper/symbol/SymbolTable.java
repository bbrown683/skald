package io.github.bbrown683.jasper.symbol;

import org.apache.bcel.generic.Type;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Stack<Scope> scopes = new Stack<>();

    public SymbolTable() {
        scopes.push(new Scope());
        loadBaseTypes();
    }

    public void enterScope() {
        scopes.push(new Scope());
    }

    public void exitScope() {
        if(scopes.size() > 1) {
            scopes.pop();
        }
    }

    public void addSymbol(String name, Symbol symbol) {
        scopes.peek().addSymbol(name, symbol);
    }

    public Symbol getSymbol(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            var currentScope = scopes.get(i);
            if (currentScope.containsSymbol(name)) {
                return currentScope.getSymbol(name);
            }
        }
        return null; // Symbol not found
    }

    public boolean containsSymbol(String name) {
        return getSymbol(name) != null;
    }

    public Stack<Scope> getCurrentScope() {
        return (Stack<Scope>)scopes.clone();
    }

    private void loadBaseTypes() {
        List<String> baseTypes = List.of("System");
        for (String baseType : baseTypes) {
            loadBaseType("java.lang." + baseType);
        }
    }

    private void loadBaseType(String baseType) {
        try {
            var clazz = Class.forName(baseType);
            var className = clazz.getSimpleName();
            System.out.println("Scanning Class: " + className);
            addSymbol(className, new TypeSymbol(className, clazz.getPackageName(), true, true, false, false));

            for (var method : clazz.getMethods()) {
                String methodName = method.getName();
                System.out.print("Found Method: " + methodName);

                var returnType = method.getReturnType();
                var isMethodStatic = Modifier.isStatic(method.getModifiers());
                addSymbol(methodName, new FunctionSymbol(methodName, Type.getType(returnType), true, isMethodStatic));

                for (var parameter : method.getParameters()) {
                    String name = parameter.getName();
                    System.out.print(", Parameter: " + parameter.getName());
                    var type = parameter.getType();
                    var modifiers = parameter.getModifiers();
                    addSymbol(name, new VariableSymbol(name, Type.getType(type), Modifier.isPublic(modifiers), Modifier.isStatic(modifiers), !Modifier.isFinal(modifiers), type.isArray(), true, type.isPrimitive(), null));
                }
                System.out.println();
            }

            for (var field : clazz.getFields()) {
                String name = field.getName();
                System.out.println("Found Field: " + name);
                var type = field.getType();
                var modifiers = field.getModifiers();
                addSymbol(name, new VariableSymbol(name, Type.getType(type), Modifier.isPublic(modifiers), Modifier.isStatic(modifiers), !Modifier.isFinal(modifiers), type.isArray(), true, type.isPrimitive(), null));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
