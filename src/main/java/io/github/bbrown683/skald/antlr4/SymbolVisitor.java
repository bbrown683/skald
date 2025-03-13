package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.reference.ReferenceTable;
import io.github.bbrown683.skald.symbol.*;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SymbolVisitor extends SkaldParserBaseVisitor<Object> {
    private SymbolTable symbolTable;
    private final ReferenceTable referenceTable = new ReferenceTable();
    private final String className;

    public SymbolVisitor(String className) {
        this.className = className;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ReferenceTable getReferenceTable() {
        return referenceTable;
    }

    @Override
    public Object visitClassFile(SkaldParser.ClassFileContext ctx) {
        symbolTable = new SymbolTable(new ClassFileSymbol(className, ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Object visitPackagePath(SkaldParser.PackagePathContext ctx) {
        referenceTable.addImport(visitPath(ctx.path()) + ".*"); // Add package path to imports
        return null;
    }

    @Override
    public Object visitImportPath(SkaldParser.ImportPathContext ctx) {
        referenceTable.addImport(visitPath(ctx.path()));
        return null;
    }

    @Override
    public String visitPath(SkaldParser.PathContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitFunction(SkaldParser.FunctionContext ctx) {
        var functionName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isPublic = ctx.PUBLIC() != null;
        boolean isStatic = ctx.STATIC() != null;

        symbolTable.addSymbol(new FunctionSymbol(functionName, ctx, getType(typeName), isPublic, isStatic));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitFunctionParameter(SkaldParser.FunctionParameterContext ctx) {
        var variableName = ctx.IDENTIFIER().getText();
        var typeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";

        boolean isMutable = ctx.MUTABLE() != null;
        boolean isArray = ctx.array() != null && !ctx.array().isEmpty();

        symbolTable.addSymbol(new VariableSymbol(variableName, ctx, getType(typeName), false, false, isMutable, isArray, true, null));
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
        symbolTable.addSymbol(new VariableSymbol(variableName, ctx, getType(typeName), isPublic, isStatic, isMutable, isArray, false, value));
        return null;
    }

    @Override
    public Object visitForLoop(SkaldParser.ForLoopContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("for", ctx));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitWhileLoop(SkaldParser.WhileLoopContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("while", ctx));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitIfStatement(SkaldParser.IfStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("if", ctx));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitElseIfStatement(SkaldParser.ElseIfStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("else-if", ctx));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
        return null;
    }


    @Override
    public Object visitElseStatement(SkaldParser.ElseStatementContext ctx) {
        symbolTable.addSymbol(new BlockSymbol("else", ctx));
        symbolTable.enterScope();
        visitChildren(ctx);
        symbolTable.exitScope();
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
