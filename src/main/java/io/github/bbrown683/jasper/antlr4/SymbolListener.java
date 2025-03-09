package io.github.bbrown683.jasper.antlr4;

import io.github.bbrown683.jasper.symbol.*;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SymbolListener extends JasperParserBaseListener {
    @Getter
    private final SymbolTable symbolTable = new SymbolTable();
    @Getter
    private final Map<ParserRuleContext, Stack<Scope>> contextScopes = new HashMap<>();
    private final String className;

    public SymbolListener(String className) {
        this.className = className;
    }

    private Type getType(String typeName) {
        if(typeName == null || typeName.isBlank() || typeName.equals("unit")) {
            return Type.VOID;
        }
        return switch (typeName) {
            case "byte", "ubyte" -> Type.BYTE;
            case "short", "ushort" -> Type.SHORT;
            case "int", "uint" -> Type.INT;
            case "long", "ulong" -> Type.LONG;
            case "float" -> Type.FLOAT;
            case "double" -> Type.DOUBLE;
            case "char" -> Type.CHAR;
            case "string" -> Type.STRING;
            case "boolean" -> Type.BOOLEAN;
            default -> new ObjectType(typeName);
        };
    }

    private Object parseIntegerFromString(String number) {
        List<Pair<Class,String>> parseFunctions = List.of(
                Pair.of(Byte.class, "parseByte"),
                Pair.of(Short.class, "parseShort"),
                Pair.of(Integer.class, "parseInt"), // Integer is a special case
                Pair.of(Long.class, "parseLong")
        );
        for (var parseFunction : parseFunctions) {
            Class clazz = parseFunction.getLeft();
            String methodName = parseFunction.getRight();
            try {
                return clazz.getMethod(methodName, String.class).invoke(null, number);
            } catch (Exception e) {} // Ignore exception
        }
        return null;
    }

    private Object parseFloatFromString(String number) {
        List<Class> classes = List.of(Float.class, Double.class);
        for (var clazz : classes) {
            try {
                return clazz.getMethod("parse" + clazz.getSimpleName(), String.class).invoke(null, number);
            } catch (Exception e) {} // Ignore exception
        }
        return null;
    }

    @Override
    public void enterClassFile(JasperParser.ClassFileContext ctx) {
        var classSymbol = new GlobalSymbol(className);
        symbolTable.addSymbol(classSymbol.getName(), classSymbol);
        contextScopes.put(ctx, symbolTable.getCurrentScope());
    }

    @Override
    public void enterFunction(JasperParser.FunctionContext ctx) {
        var functionName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;

        var functionSymbol = new FunctionSymbol(functionName, getType(typeName), isPublic, isStatic);
        symbolTable.addSymbol(functionSymbol.getName(), functionSymbol);
        contextScopes.put(ctx, symbolTable.getCurrentScope());
        symbolTable.enterScope();
    }

    @Override
    public void exitFunction(JasperParser.FunctionContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterFunctionParameter(JasperParser.FunctionParameterContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null;
        var parameter = new VariableSymbol(variableName, getType(typeName), false, false, isMutable, isArray, true, false, null);
        symbolTable.addSymbol(parameter.getName(), parameter);
        contextScopes.put(ctx, symbolTable.getCurrentScope());
    }

    @Override
    public void enterVariable(JasperParser.VariableContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;
        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null;
        boolean isLiteral = ctx.literals() != null;

        var variableSymbol = new VariableSymbol(variableName, getType(typeName), isPublic, isStatic, isMutable, isArray, false, isLiteral, null);
        symbolTable.addSymbol(variableSymbol.getName(), variableSymbol);
        contextScopes.put(ctx, symbolTable.getCurrentScope());
    }

    @Override
    public void enterForLoop(JasperParser.ForLoopContext ctx) {
        symbolTable.enterScope();
    }

    @Override
    public void exitForLoop(JasperParser.ForLoopContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterWhileLoop(JasperParser.WhileLoopContext ctx) {
        symbolTable.enterScope();
    }

    @Override
    public void exitWhileLoop(JasperParser.WhileLoopContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterIfStatement(JasperParser.IfStatementContext ctx) {
        symbolTable.enterScope();
    }

    @Override
    public void exitIfStatement(JasperParser.IfStatementContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterElseIfStatement(JasperParser.ElseIfStatementContext ctx) {
        symbolTable.enterScope();
    }

    @Override
    public void exitElseIfStatement(JasperParser.ElseIfStatementContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterElseStatement(JasperParser.ElseStatementContext ctx) {
        symbolTable.enterScope();
    }

    @Override
    public void exitElseStatement(JasperParser.ElseStatementContext ctx) {
        symbolTable.exitScope();
    }
}
