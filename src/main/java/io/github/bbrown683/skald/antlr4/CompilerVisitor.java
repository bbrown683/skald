package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.jvm.InstructionGenerator;
import io.github.bbrown683.skald.jvm.InstructionUtil;
import io.github.bbrown683.skald.reference.ReferenceTable;
import io.github.bbrown683.skald.symbol.FunctionSymbol;
import io.github.bbrown683.skald.symbol.Symbol;
import io.github.bbrown683.skald.symbol.SymbolTable;
import io.github.bbrown683.skald.symbol.VariableSymbol;
import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileOutputStream;
import java.util.*;

public class CompilerVisitor extends SkaldParserBaseVisitor<Object> {
    private final String className;
    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
    private InstructionUtil instructionUtil;
    private final SymbolTable symbolTable;
    private final ReferenceTable referenceTable;
    private final Map<Symbol,LocalVariableGen> localVariables = new HashMap<>();

    public CompilerVisitor(String className, SymbolTable symbolTable, ReferenceTable referenceTable) {
        this.className = className;
        this.symbolTable = symbolTable;
        this.referenceTable = referenceTable;
    }

    @Override
    public Object visitClassFile(SkaldParser.ClassFileContext ctx) {
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
        /*
        var constructorCtx = new SkaldParser.FunctionContext(null, -1);
        constructorCtx.start = ctx.start;
        constructorCtx.stop = ctx.stop;
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.FUNCTION, "fn")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.IDENTIFIER, "new")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.LEFT_PAREN, "(")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.RIGHT_PAREN, ")")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.LEFT_BRACE, "{")));
        constructorCtx.addChild(new TerminalNodeImpl(new CommonToken(SkaldParser.RIGHT_BRACE, "}")));
        visitFunction(constructorCtx, ctx.variable());
        */

        var functions = ctx.function();
        if(functions != null) functions.forEach(this::visitFunction);

        var javaClass = classGen.getJavaClass();
        System.out.println(javaClass);
        try(FileOutputStream fileOutputStream = new FileOutputStream(className + ".class")) {
            javaClass.dump(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return javaClass.getBytes();
    }

    @Override
    public Object visitPackagePath(SkaldParser.PackagePathContext ctx) {
        String path = visitPath(ctx.path());
        classGen.setClassName(path + "." + className);
        System.out.println("Package: " + path);
        return path;
    }

    @Override
    public Object visitImportPath(SkaldParser.ImportPathContext ctx) {
        String path = visitPath(ctx.path());
        System.out.println("Import: " + path);
        return path;
    }

    @Override
    public String visitPath(SkaldParser.PathContext ctx) {
        return ctx.getText();
    }

    @Override
    public Pair<String,Type> visitFunctionParameter(SkaldParser.FunctionParameterContext ctx) {
        var symbol = (VariableSymbol)symbolTable.getSymbol(ctx);
        var identifierName = symbol.getName();
        var type = symbol.getType();

        System.out.println("Parameter: " + identifierName + ", Type: " + type);

        var array = ctx.array();
        if(array != null) {
            return Pair.of(identifierName, new ArrayType(type, array.size()));
        }
        return Pair.of(identifierName, type);
    }

    public Object visitFunction(SkaldParser.FunctionContext ctx, List<SkaldParser.VariableContext> fieldVariables) {
        var symbol = (FunctionSymbol)symbolTable.getSymbol(ctx);

        String functionName = symbol.getName();
        Type returnType = symbol.getReturnType();
        System.out.println("===================================");
        System.out.println("Function: " + functionName);
        System.out.println("Return: " + returnType);

        List<String> parameterNames = new ArrayList<>();
        List<Type> parameterTypes = new ArrayList<>();
        ctx.functionParameter().forEach((functionCtx) -> {
            var pair = visitFunctionParameter(functionCtx);
            parameterNames.add(pair.getLeft());
            parameterTypes.add(pair.getRight());
        });

        var instructionList = new InstructionList();

        var isConstructor = functionName.equals("new");
        var isPublic = ctx.PUBLIC() != null ? Const.ACC_PUBLIC : 0;
        var isStatic = ctx.STATIC() != null || ctx.parent instanceof SkaldParser.ClassFileContext ? Const.ACC_STATIC : 0;

        var methodGen = new MethodGen(isPublic | isStatic,
                returnType,
                parameterTypes.toArray(new Type[0]),
                parameterNames.toArray(new String[0]),
                isConstructor ? "<init>" : functionName,
                classGen.getClassName(),
                instructionList,
                constantPoolGen);

        var parameters = new HashMap<Class<?>, Object>();
        parameters.put(MethodGen.class, methodGen);
        parameters.put(InstructionList.class, instructionList);

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
    public Object visitFunction(SkaldParser.FunctionContext ctx) {
        return visitFunction(ctx, Collections.emptyList());
    }

    public Object visitVariable(SkaldParser.VariableContext ctx, Map<Class<?>, Object> parameters) {
        var symbol = (VariableSymbol)symbolTable.getSymbol(ctx);
        System.out.println("-----------------------------------");
        String variableName = symbol.getName();
        var isPublic = symbol.isPublic() ? Const.ACC_PUBLIC : 0;
        var isStatic = symbol.isStatic() ? Const.ACC_STATIC : 0;
        var isMutable = symbol.isMutable() ? 0 : Const.ACC_FINAL;
        var type = symbol.getType();
        var value = symbol.getValue();

        System.out.println("Variable: " + variableName +
                ", Type: " + type +
                ", Public: " + (isPublic == Const.ACC_PUBLIC) +
                ", Static: " + (isStatic == Const.ACC_STATIC) +
                ", Mutable: " + (isMutable != Const.ACC_FINAL));

        var methodGen = (MethodGen)parameters.get(MethodGen.class);
        var methodInstructions = (InstructionList)parameters.get(InstructionList.class);

        var variableInstructions = new InstructionList();
        var instructionGenerator = new InstructionGenerator(classGen, constantPoolGen, variableInstructions, methodGen);

        // If we are in a constructor, we need to push the reference to the object onto the stack.
        var isConstructor = methodGen.getName().equals("<init>");
        if(isConstructor) instructionGenerator.callSuper();

        var literals = ctx.literals();
        var reference = ctx.reference();
        var expression = ctx.expression();
        if (literals != null) {
            System.out.println("Literal: " + value + ", Type: " + value.getClass().getSimpleName());

            // If we are in a constructor, we need to create a field and store the value in it.
            // Otherwise, we need to create a local variable and store the value in it.
            if(isConstructor) {
                instructionGenerator.addVariableAsLiteralField(variableName, value, type, isPublic | isStatic | isMutable, className);
            } else {
                var localVariable = instructionGenerator.addVariableLiteralAsLiteralLocalVariable(variableName, value, type);
                localVariables.put(symbol, localVariable);
            }
        } else if (reference != null) {
            String referenceName = (String)symbol.getValue();
            System.out.println("Reference: " + referenceName);
            // Check symbol table first
            var visibleSymbols = symbol.getVisibleSymbols();
            if(visibleSymbols != null) {
                for (var visibleSymbol : visibleSymbols) {
                    if (!referenceName.equals(visibleSymbol.getName()) ||
                            !(visibleSymbol instanceof VariableSymbol variableSymbol)) continue;

                    if(variableSymbol.isStatic()) {
                        instructionGenerator.addVariableAsStaticReference(referenceName, variableName, type);
                    } else {
                        var localVariable = localVariables.get(variableSymbol);
                        if(localVariable != null) {
                            instructionGenerator.addVariableAsReference(variableName, localVariable);
                        }
                    }
                }
            } else {
                //referenceTable.getReference();
            }
        } else if (expression != null) {
            visitExpression(expression, parameters);
        }

        methodGen.addLineNumber(variableInstructions.getStart(), ctx.stop.getLine());
        methodInstructions.append(variableInstructions);
        return null;
    }

    @Override
    public Object visitVariable(SkaldParser.VariableContext ctx) {
        return visitVariable(ctx, Collections.emptyMap());
    }

    @Override
    public Object visitFunctionCallArgument(SkaldParser.FunctionCallArgumentContext ctx) {
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
    public Object visitFunctionCall(SkaldParser.FunctionCallContext ctx) {
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

    public Object visitExpression(SkaldParser.ExpressionContext ctx, Map<Class<?>, Object> parameters) {
        if (ctx.variable() != null) {
            return visitVariable(ctx.variable(), parameters);
        }
        if (ctx.functionCall() != null) {
            return visitFunctionCall(ctx.functionCall());
        }
        return null;
    }

    @Override
    public Object visitExpression(SkaldParser.ExpressionContext ctx) {
        return visitExpression(ctx, Collections.emptyMap());
    }

    @Override
    public String visitReference(SkaldParser.ReferenceContext ctx) {
        return ctx.getText();
    }
}
