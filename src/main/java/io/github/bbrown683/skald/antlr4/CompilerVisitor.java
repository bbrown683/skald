package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.ExpressionParameter;
import io.github.bbrown683.skald.jvm.InstructionGenerator;
import io.github.bbrown683.skald.jvm.InstructionUtil;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileOutputStream;
import java.util.*;

public class CompilerVisitor extends JasperParserBaseVisitor<Object> {
    private final String className;
    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
    private InstructionUtil instructionUtil;

    public CompilerVisitor(String className) {
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

    @Override
    public byte[] visitClassFile(JasperParser.ClassFileContext ctx) {
        // Initialize with empty name until package is visited
        classGen = new ClassGen(className,
                "java.lang.Object",
                className + ".lang",
                Const.ACC_PUBLIC | Const.ACC_SUPER | Const.ACC_FINAL,
                null);
        classGen.setMajor(Const.MAJOR_23);
        classGen.setMinor(0);

        constantPoolGen = classGen.getConstantPool();
        instructionUtil = new InstructionUtil(classGen, constantPoolGen);

        // Create empty constructor.
        var constructorCtx = new JasperParser.FunctionContext(null, -1);
        constructorCtx.start = ctx.start;
        constructorCtx.stop = ctx.stop;
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.FUNCTION, "fn")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.IDENTIFIER, "new")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.LEFT_PAREN, "(")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.RIGHT_PAREN, ")")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.LEFT_BRACE, "{")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(JasperParser.RIGHT_BRACE, "}")));
        visitFunction(constructorCtx, ctx.variable());

        var functions = ctx.function();
        if(functions != null) functions.forEach(this::visitFunction);

        var javaClass = classGen.getJavaClass();
        try(FileOutputStream fileOutputStream = new FileOutputStream(className + ".class")) {
            javaClass.dump(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return javaClass.getBytes();
    }

    @Override
    public Object visitPackagePath(JasperParser.PackagePathContext ctx) {
        String path = ctx.path().getText();
        classGen.setClassName(path + "." + className);
        System.out.println("Package: " + path);
        return path;
    }

    @Override
    public Object visitImportPath(JasperParser.ImportPathContext ctx) {
        String path = ctx.path().getText();
        System.out.println("Import: " + path);
        return path;
    }

    @Override
    public Object visitPath(JasperParser.PathContext ctx) {
        return ctx.getText();
    }

    @Override
    public Pair<String,Type> visitFunctionParameter(JasperParser.FunctionParameterContext ctx) {
        var identifierName = ctx.IDENTIFIER().getText();
        var typeName = visitTypeName(ctx.typeName());

        boolean inferredType = false;
        if (typeName.isBlank()) {
            inferredType = true;
            System.out.println("Inferred type");
        }
        System.out.println("Parameter: " + identifierName + ", Type: " + typeName);

        var array = ctx.array();
        if(array != null) {
            return Pair.of(identifierName, new ArrayType(getType(typeName), array.size()));
        }
        return Pair.of(identifierName, getType(typeName));
    }

    public Object visitFunction(JasperParser.FunctionContext ctx, List<JasperParser.VariableContext> fieldVariables) {
        String functionName = ctx.IDENTIFIER().getText();
        String returnTypeName = ctx.typeName() != null ? ctx.typeName().getText() : "unit";
        System.out.println("===================================");
        System.out.println("Function: " + functionName);
        System.out.println("Return: " + returnTypeName);

        List<String> parameterNames = new ArrayList<>();
        List<Type> parameterTypes = new ArrayList<>();
        ctx.functionParameter().forEach((functionCtx) -> {
            var pair = visitFunctionParameter(functionCtx);
            parameterNames.add(pair.getLeft());
            parameterTypes.add(pair.getRight());
        });

        Type returnType = getType(returnTypeName);
        InstructionList instructionList = new InstructionList();

        boolean isConstructor = functionName.equals("new");
        int isPublic = ctx.PUBLIC() != null ? Const.ACC_PUBLIC : 0;
        int isStatic = ctx.STATIC() != null || ctx.parent instanceof JasperParser.ClassFileContext ? Const.ACC_STATIC : 0;

        var methodGen = new MethodGen(isPublic | isStatic,
                returnType,
                parameterTypes.toArray(new Type[0]),
                parameterNames.toArray(new String[0]),
                isConstructor ? "<init>" : functionName,
                classGen.getClassName(),
                instructionList,
                constantPoolGen);

        var parameters = new HashMap<ExpressionParameter, Object>();
        parameters.put(ExpressionParameter.METHOD, methodGen);
        parameters.put(ExpressionParameter.INSTRUCTION_LIST, instructionList);


        // Field variables are used when we are in a constructor and need to initialize fields.
        if(isConstructor && fieldVariables != null) {
            instructionList.append(instructionUtil.callSuper(null));
            fieldVariables.forEach((fieldCtx) -> visitVariable(fieldCtx, parameters));
        } else {
            ctx.expression().forEach((expressionCtx) -> visitExpression(expressionCtx, parameters));
        }
        var returnInstruction = instructionUtil.insertReturn(returnType);
        methodGen.addLineNumber(returnInstruction.getStart(), ctx.stop.getLine());
        instructionList.append(returnInstruction);

        // The way local variables work in the JVM is that they have typically two instructions:
        // 1. Push the value onto the stack
        // 2. Store the value in the local variable
        // Thus we need to set the start instruction to the next available instruction after these,
        // as at that point the variable is constructed. Anypoint before that and the variable will be corrupted.
        // TODO: We need to figure out how to handle the scope of the local variable as it is currently set to the end of the method,
        //  but situations such as if statements or loops will require the variable to be set to the end of the block.
        for(var localVariable : methodGen.getLocalVariables()) {
            var localEnd = localVariable.getEnd();
            var methodEnd = instructionList.getEnd();
            if(localEnd != methodEnd) {
                var localNext  = localEnd.getNext();
                localVariable.setStart(localNext != null ? localNext : methodEnd);
                localVariable.setEnd(methodEnd);
            }
        }

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        instructionList.dispose();
        System.out.println("===================================");
        return null;
    }

    @Override
    public Object visitFunction(JasperParser.FunctionContext ctx) {
        return visitFunction(ctx, null);
    }

    public Object visitVariable(JasperParser.VariableContext ctx, Map<ExpressionParameter, Object> parameters) {
        System.out.println("-----------------------------------");
        String variableName = ctx.IDENTIFIER().getText();

        int isPublic = ctx.PUBLIC() != null ? Const.ACC_PUBLIC : 0;
        int isStatic = ctx.STATIC() != null ? Const.ACC_STATIC : 0;
        int isMutable = ctx.MUTABLE() != null ? 0 : Const.ACC_FINAL;

        String typeName = visitTypeName(ctx.typeName());
        System.out.println("Variable: " + variableName +
                ", Type: " + typeName +
                ", Public: " + (isPublic == Const.ACC_PUBLIC) +
                ", Static: " + (isStatic == Const.ACC_STATIC) +
                ", Mutable: " + (isMutable != Const.ACC_FINAL));
        Type type = getType(typeName);

        var methodGen = (MethodGen)parameters.get(ExpressionParameter.METHOD);
        var methodInstructions = (InstructionList)parameters.get(ExpressionParameter.INSTRUCTION_LIST);

        var variableInstructions = new InstructionList();
        var instructionGenerator = new InstructionGenerator(classGen, constantPoolGen, variableInstructions, methodGen);

        // If we are in a constructor, we need to push the reference to the object onto the stack.
        boolean isConstructor = methodGen.getName().equals("<init>");
        if(isConstructor) instructionGenerator.callSuper();

        var literals = ctx.literals();
        var reference = ctx.reference();
        var expression = ctx.expression();
        if (literals != null) {
            Object literal = visitLiterals(literals);
            System.out.println("Literal: " + literal + ", Type: " + literal.getClass().getSimpleName());

            // If we are in a constructor, we need to create a field and store the value in it.
            // Otherwise, we need to create a local variable and store the value in it.
            if(isConstructor) {
                instructionGenerator.addVariableAsLiteralField(variableName, literal, type, isPublic | isStatic | isMutable, className);
            } else {
                instructionGenerator.addVariableLiteralAsLiteralLocalVariable(variableName, literal, type);
            }
        } else if (reference != null) {
            String referenceName = visitReference(reference);
            System.out.println("Reference: " + reference);

            instructionGenerator.addVariableAsReference(referenceName, variableName, type);
        } else if (expression != null) {
            visitExpression(expression, parameters);
        }

        methodGen.addLineNumber(variableInstructions.getStart(), ctx.stop.getLine());
        methodInstructions.append(variableInstructions);
        return null;
    }

    @Override
    public Object visitVariable(JasperParser.VariableContext ctx) {
        return visitVariable(ctx, Collections.emptyMap());
    }

    @Override
    public Object visitFunctionCallArgument(JasperParser.FunctionCallArgumentContext ctx) {
        var literals = ctx.literals();
        if(literals != null) {
            Object literal = visitLiterals(literals);

        }

        var reference = ctx.reference();
        if(reference != null) {
            String referenceName = visitReference(reference);
            //instructionGenerator.loadVariable();
        }

        var expression = ctx.expression();
        if(expression != null) visitExpression(expression);

        return null;
    }

    @Override
    public Object visitFunctionCall(JasperParser.FunctionCallContext ctx) {
        System.out.println("-----------------------------------");
        String reference = visitReference(ctx.reference());

        if(reference.contains(".")) {
            String referenceName = StringUtils.substringBeforeLast(reference, ".");
            String functionName = StringUtils.substringAfterLast(reference, ".");
            System.out.println("Reference: " + referenceName + ", Function: " + functionName);
        } else {
            System.out.println("Function: " + reference);
        }

        ctx.functionCallArgument().forEach(this::visitFunctionCallArgument);
        return null;
    }


    public Object visitExpression(JasperParser.ExpressionContext ctx, Map<ExpressionParameter, Object> parameters) {
        if (ctx.variable() != null) {
            return visitVariable(ctx.variable(), parameters);
        }
        if (ctx.functionCall() != null) {
            return visitFunctionCall(ctx.functionCall());
        }
        return null;
    }

    @Override
    public Object visitExpression(JasperParser.ExpressionContext ctx) {
        return visitExpression(ctx, Collections.emptyMap());
    }

    @Override
    public Object visitLiterals(JasperParser.LiteralsContext ctx) {
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

    @Override
    public String visitTypeName(JasperParser.TypeNameContext ctx) {
        if(ctx == null) {
            return "";
        }
        return ctx.getText();
    }

    @Override
    public String visitReference(JasperParser.ReferenceContext ctx) {
        return ctx.getText();
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
}
