package io.github.bbrown683.skald.antlr4;

import io.github.bbrown683.skald.jvm.InstructionGenerator;
import io.github.bbrown683.skald.jvm.InstructionUtil;
import io.github.bbrown683.skald.symbol.external.ExternalFunctionSymbol;
import io.github.bbrown683.skald.symbol.external.ExternalSymbol;
import io.github.bbrown683.skald.symbol.external.ExternalSymbolTable;
import io.github.bbrown683.skald.symbol.external.ExternalVariableSymbol;
import io.github.bbrown683.skald.symbol.local.*;
import org.antlr.v4.runtime.ParserRuleContext;
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
    private final LocalSymbolTable localSymbolTable;
    private final ExternalSymbolTable externalSymbolTable;
    private final Map<LocalSymbol,LocalVariableGen> localVariables = new HashMap<>();

    public CompilerVisitor(String className, LocalSymbolTable localSymbolTable, ExternalSymbolTable externalSymbolTable) {
        this.className = className;
        this.localSymbolTable = localSymbolTable;
        this.externalSymbolTable = externalSymbolTable;
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
        generateConstructor(ctx);

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
        var symbol = (LocalVariableSymbol) localSymbolTable.getSymbol(ctx);
        var identifierName = symbol.getName();
        var type = symbol.getType();

        System.out.println("Parameter: " + identifierName + ", Type: " + type);

        var array = ctx.array();
        if(array != null) {
            return Pair.of(identifierName, new ArrayType(type, array.size()));
        }
        return Pair.of(identifierName, type);
    }

    private Object generateConstructor(ParserRuleContext ctx) {
        System.out.println("===================================");
        System.out.println("Generating Constructor");

        List<String> parameterNames = new ArrayList<>();
        List<Type> parameterTypes = new ArrayList<>();
        if (ctx instanceof SkaldParser.ConstructorContext constructorCtx) {
            var parameters = constructorCtx.functionParameter();
            parameters.forEach((functionCtx) -> {
                var pair = visitFunctionParameter(functionCtx);
                parameterNames.add(pair.getLeft());
                parameterTypes.add(pair.getRight());
            });
        }

        var instructionGenerator = new InstructionGenerator(classGen, constantPoolGen);
        var methodGen = instructionGenerator.getMethodGen(Const.ACC_PUBLIC, "<init>", Type.VOID, parameterTypes, parameterNames);

        var parameters = Map.of(
                MethodGen.class, methodGen,
                InstructionGenerator.class, instructionGenerator
        );

        // Field variables are used when we are in a constructor and need to initialize fields.
        instructionGenerator.callSuper(null);

        if (ctx instanceof SkaldParser.ClassFileContext classFileContext) {
            var variables = classFileContext.variable();
            if (variables != null) {
                variables.forEach((fieldCtx) -> visitVariable(fieldCtx, parameters));
            }
        } else if (ctx instanceof SkaldParser.ConstructorContext constructorCtx) {
            var expressions = constructorCtx.expression();
            if (expressions != null) {
                expressions.forEach((expressionCtx) -> visitExpression(expressionCtx, parameters));
            }
        }

        instructionGenerator.insertReturn(Type.VOID);
        var constructorInstructions = instructionGenerator.getInstructionList();
        var returnInstruction = constructorInstructions.getEnd();
        methodGen.addLineNumber(returnInstruction, ctx.stop.getLine());

        LocalSymbol symbol = localSymbolTable.getSymbol(ctx); // Probably refer to this for updating scopes later.
        instructionGenerator.updateVariableScope();
        instructionGenerator.completeFunction();
        System.out.println("===================================");
        return null;
    }

    @Override
    public Object visitConstructor(SkaldParser.ConstructorContext ctx) {
        return generateConstructor(ctx);
    }

    @Override
    public Object visitFunction(SkaldParser.FunctionContext ctx) {
        var symbol = (LocalFunctionSymbol) localSymbolTable.getSymbol(ctx);

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

        var instructionGenerator = new InstructionGenerator(classGen, constantPoolGen);

        var isPublic = ctx.PUBLIC() != null ? Const.ACC_PUBLIC : 0;
        var isStatic = ctx.STATIC() != null || ctx.parent instanceof SkaldParser.ClassFileContext ? Const.ACC_STATIC : 0;
        var methodGen = instructionGenerator.getMethodGen(isPublic | isStatic, functionName, returnType, parameterTypes, parameterNames);

        var parameters = Map.of(
                MethodGen.class, methodGen,
                InstructionGenerator.class, instructionGenerator
        );

        ctx.expression().forEach((expressionCtx) -> visitExpression(expressionCtx, parameters));

        instructionGenerator.insertReturn(Type.VOID);

        var methodInstructions = instructionGenerator.getInstructionList();
        var returnInstruction = methodInstructions.getEnd();
        methodGen.addLineNumber(returnInstruction, ctx.stop.getLine());

        instructionGenerator.updateVariableScope();
        instructionGenerator.completeFunction();
        System.out.println("===================================");
        return null;
    }

    public Object visitVariable(SkaldParser.VariableContext ctx, Map<Class<?>, Object> parameters) {
        var symbol = (LocalVariableSymbol) localSymbolTable.getSymbol(ctx);
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
        var instructionGenerator = new InstructionGenerator(classGen, constantPoolGen, methodGen);

        var literals = ctx.literals();
        var reference = ctx.reference();
        var expression = ctx.expression();
        if (literals != null) {
            System.out.println("Literal: " + value + ", Type: " + value.getClass().getSimpleName());

            // If we are in a constructor, we need to create a field and store the value in it.
            // Otherwise, we need to create a local variable and store the value in it.
            if(methodGen.getName().equals("<init>")) {
                instructionGenerator.addVariableAsLiteralField(variableName, value, type, isPublic | isStatic | isMutable, className);
            } else {
                var localVariable = instructionGenerator.addVariableLiteralAsLiteralLocalVariable(variableName, value, type);
                localVariables.put(symbol, localVariable);
            }
        } else if (reference != null) {
            String symbolName = (String)symbol.getValue();
            System.out.println("Reference: " + symbolName);
            // Check symbol table first
            var localSymbol = symbol.findSymbol(symbolName, LocalVariableSymbol.class);
            if(localSymbol != null) {
                if (localSymbol.isStatic()) {
                    instructionGenerator.addVariableAsStaticReference(symbolName, variableName, className, type);
                } else {
                    instructionGenerator.addVariableAsReference(variableName, localVariables.get(localSymbol));
                }
            } else {
                ExternalVariableSymbol externalSymbol;
                String symbolParentName = "";
                String actualSymbolName = "";
                if (symbolName.contains(".")) { // Static symbol
                    symbolParentName = StringUtils.substringBeforeLast(symbolName, ".");
                    actualSymbolName = StringUtils.substringAfterLast(symbolName, ".");
                    var externalSymbols = externalSymbolTable.getSymbol(actualSymbolName, symbolParentName, ExternalVariableSymbol.class);
                    externalSymbol = externalSymbols.getFirst();
                } else {
                    var externalSymbols = externalSymbolTable.getSymbol(symbolName, null, ExternalVariableSymbol.class);
                    externalSymbol = externalSymbols.getFirst();
                }

                if (externalSymbol != null) {
                    if (externalSymbol.isStatic()) {
                        instructionGenerator.addVariableAsStaticReference(variableName, symbolParentName, actualSymbolName, externalSymbol.getType());
                    } else {
                        instructionGenerator.addVariableAsReference(variableName, localVariables.get(localSymbol));
                    }
                }
            }
        } else if (expression != null) {
            visitExpression(expression, parameters);
        }

        var variableInstructions = instructionGenerator.getInstructionList();
        methodGen.addLineNumber(variableInstructions.getStart(), ctx.stop.getLine());

        var methodInstructions = (InstructionGenerator)parameters.get(InstructionGenerator.class);
        methodInstructions.getInstructionList().append(variableInstructions);
        return null;
    }

    @Override
    public Object visitVariable(SkaldParser.VariableContext ctx) {
        return visitVariable(ctx, Collections.emptyMap());
    }

    public Object visitFunctionCallArgument(SkaldParser.FunctionCallArgumentContext ctx, Map<Class<?>, Object> parameters) {
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
    public Object visitFunctionCallArgument(SkaldParser.FunctionCallArgumentContext ctx) {
        return visitFunctionCallArgument(ctx, Collections.emptyMap());
    }

    public Object visitFunctionCall(SkaldParser.FunctionCallContext ctx, Map<Class<?>, Object> parameters) {
        var localSymbol = (LocalMarkerSymbol)localSymbolTable.getSymbol(ctx);
        System.out.println("-----------------------------------");
        String reference = visitReference(ctx.reference());

        var arguments = ctx.functionCallArgument();
        if(arguments != null) arguments.forEach((argumentCtx) -> visitFunctionCallArgument(argumentCtx, parameters));

        // TODO: also consider local functions with and without variable access.
        if(reference.contains(".")) {
            String referenceName = StringUtils.substringBeforeLast(reference, ".");
            String functionName = StringUtils.substringAfterLast(reference, ".");
            var referenceSymbol = localSymbol.findSymbol(referenceName, LocalVariableSymbol.class);
            var type = referenceSymbol.getType();

            System.out.println("Reference: " + referenceName + ", Function: " + functionName);
            var externalSymbols = externalSymbolTable.getSymbol(functionName, type.getClassName(), ExternalFunctionSymbol.class);

            // Find matching function, due to overloading
            int argumentCount;
            if (arguments != null) argumentCount = arguments.size();
            else argumentCount = 0;

            var externalSymbol = externalSymbols
                    .stream()
                    .filter(s -> s.getParameters().size() == argumentCount)
                    .findFirst()
                    .orElse(null);

            var instructionGenerator = (InstructionGenerator)parameters.get(InstructionGenerator.class);

            List<Type> argumentTypes = externalSymbol.getParameters().stream().map(ExternalVariableSymbol::getType).toList();
            instructionGenerator.callFunction(type.getClassName(), functionName, externalSymbol.getReturnType(), argumentTypes.toArray(new Type[0]), externalSymbol.isStatic());
        } else {
            System.out.println("Local Function: " + reference);
            //externalSymbol = externalSymbolTable.getSymbol(reference, null, ExternalFunctionSymbol.class);
        }

        return null;
    }

    @Override
    public Object visitFunctionCall(SkaldParser.FunctionCallContext ctx) {
        return visitFunctionCall(ctx, Collections.emptyMap());
    }

    public Object visitExpression(SkaldParser.ExpressionContext ctx, Map<Class<?>,Object> parameters) {
        var variable = ctx.variable();
        if (variable != null) {
            return visitVariable(variable, parameters);
        }
        var functionCall = ctx.functionCall();
        if (functionCall != null) {
            return visitFunctionCall(functionCall, parameters);
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
