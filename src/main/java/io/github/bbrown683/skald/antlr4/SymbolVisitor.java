package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.symbol.external.ExternalSymbolTable;
import io.github.bbrown683.skald.symbol.local.*;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SymbolVisitor extends SkaldParserBaseVisitor<Object> {
    private LocalSymbolTable localSymbolTable;
    private final ExternalSymbolTable externalSymbolTable = new ExternalSymbolTable();
    private final String className;

    public SymbolVisitor(String className) {
        this.className = className;
    }

    public LocalSymbolTable getLocalSymbolTable() {
        return localSymbolTable;
    }

    public ExternalSymbolTable getExternalSymbolTable() {
        return externalSymbolTable;
    }

    @Override
    public Object visitClassFile(SkaldParser.ClassFileContext ctx) {
        localSymbolTable = new LocalSymbolTable(new LocalClassFileSymbol(className, ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Object visitPackagePath(SkaldParser.PackagePathContext ctx) {
        externalSymbolTable.addImport(visitPath(ctx.path()) + ".*"); // Add package path to imports
        return null;
    }

    @Override
    public Object visitImportPath(SkaldParser.ImportPathContext ctx) {
        externalSymbolTable.addImport(visitPath(ctx.path()));
        return null;
    }

    @Override
    public String visitPath(SkaldParser.PathContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitConstructor(SkaldParser.ConstructorContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("constructor", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitFunction(SkaldParser.FunctionContext ctx) {
        var functionName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;

        localSymbolTable.addSymbol(new LocalFunctionSymbol(functionName, ctx, getType(typeName), isPublic, isStatic));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitFunctionParameter(SkaldParser.FunctionParameterContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null && !ctx.array().isEmpty();

        localSymbolTable.addSymbol(new LocalVariableSymbol(variableName, ctx, getType(typeName), false, false, isMutable, isArray, true, null));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Object visitVariable(SkaldParser.VariableContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;
        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null && !ctx.array().isEmpty();

        Object value = null;
        var reference = ctx.reference();
        var literals = ctx.literals();
        if (reference != null) {
            value = visitReference(reference);
        } else if(literals != null) {
            value = visitLiterals(ctx.literals());
        }
        localSymbolTable.addSymbol(new LocalVariableSymbol(variableName, ctx, getType(typeName), isPublic, isStatic, isMutable, isArray, false, value));
        return null;
    }

    @Override
    public Object visitFunctionCall(SkaldParser.FunctionCallContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("function-call", ctx));
        return null;
    }

    @Override
    public Object visitForLoop(SkaldParser.ForLoopContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("for", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitWhileLoop(SkaldParser.WhileLoopContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("while", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitIfStatement(SkaldParser.IfStatementContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("if", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitElseIfStatement(SkaldParser.ElseIfStatementContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("else-if", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }


    @Override
    public Object visitElseStatement(SkaldParser.ElseStatementContext ctx) {
        localSymbolTable.addSymbol(new LocalMarkerSymbol("else", ctx));
        localSymbolTable.enterScope();
        visitChildren(ctx);
        localSymbolTable.exitScope();
        return null;
    }

    @Override
    public String visitReference(SkaldParser.ReferenceContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitLiterals(SkaldParser.LiteralsContext ctx) {
        var integerLiteral = ctx.INTEGER_LITERAL();
        if (integerLiteral != null) {
            String number = integerLiteral.getText();
            return parseIntegerFromString(number);
        }

        var floatLiteral = ctx.FLOAT_LITERAL();
        if (floatLiteral != null) {
            String number = floatLiteral.getText();
            return parseFloatFromString(number);
        }

        var stringLiteral = ctx.STRING_LITERAL();
        if (stringLiteral != null) {
            String literal = stringLiteral.getText();
            return literal.replaceAll("\"", "");
        }

        var charLiteral = ctx.CHAR_LITERAL();
        if (charLiteral != null) {
            String literal = charLiteral.getText();
            return literal.replaceAll("'", "").charAt(0);
        }

        boolean isTrue = ctx.TRUE() != null;
        boolean isFalse = ctx.FALSE() != null;
        if (isTrue) return Boolean.TRUE;
        else if (isFalse) return Boolean.FALSE;

        return null;
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
}
