package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.symbol.*;
import lombok.Getter;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SymbolListener extends SkaldParserBaseListener {
    @Getter
    private SymbolTable symbolTable;
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
    public void enterClassFile(SkaldParser.ClassFileContext ctx) {
        symbolTable = new SymbolTable(new ClassFileSymbol(className, ctx));
    }

    @Override
    public void enterFunction(SkaldParser.FunctionContext ctx) {
        var functionName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;

        symbolTable.addSymbol(new FunctionSymbol(functionName, ctx, getType(typeName), isPublic, isStatic));
        symbolTable.enterScope();
    }

    @Override
    public void exitFunction(SkaldParser.FunctionContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterFunctionParameter(SkaldParser.FunctionParameterContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null && !ctx.array().isEmpty();

        symbolTable.addSymbol(new VariableSymbol(variableName, ctx, getType(typeName), false, false, isMutable, isArray, true, false, null));
    }

    @Override
    public void enterVariable(SkaldParser.VariableContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;
        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null && !ctx.array().isEmpty();
        boolean isLiteral = ctx.literals() != null;

        symbolTable.addSymbol(new VariableSymbol(variableName, ctx, getType(typeName), isPublic, isStatic, isMutable, isArray, false, isLiteral, null));
    }

    @Override
    public void enterForLoop(SkaldParser.ForLoopContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("for", ctx));
        symbolTable.enterScope();
    }

    @Override
    public void exitForLoop(SkaldParser.ForLoopContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterWhileLoop(SkaldParser.WhileLoopContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("while", ctx));
        symbolTable.enterScope();
    }

    @Override
    public void exitWhileLoop(SkaldParser.WhileLoopContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterIfStatement(SkaldParser.IfStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("if", ctx));
        symbolTable.enterScope();
    }

    @Override
    public void exitIfStatement(SkaldParser.IfStatementContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterElseIfStatement(SkaldParser.ElseIfStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("else-if", ctx));
        symbolTable.enterScope();
    }

    @Override
    public void exitElseIfStatement(SkaldParser.ElseIfStatementContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterElseStatement(SkaldParser.ElseStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("else", ctx));
        symbolTable.enterScope();
    }

    @Override
    public void exitElseStatement(SkaldParser.ElseStatementContext ctx) {
        symbolTable.exitScope();
    }
}
