package io.github.bbrown683.lang;

import io.github.bbrown683.lang.antlr4.LangParser;
import io.github.bbrown683.lang.antlr4.LangParserBaseVisitor;
import org.antlr.v4.runtime.misc.Pair;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LangVisitorASM extends LangParserBaseVisitor<Object> {
    private final ClassWriter classWriter = new ClassWriter(0);
    private final String className;
    private final boolean writeClassFile;

    private String packagePath = "";
    private final List<String> imports = new ArrayList<>();
    private int stackSize = 0;

    public LangVisitorASM(String className, boolean writeClassFile) {
        this.className = className;
        this.writeClassFile = writeClassFile;
    }

    private void writeClassFile(byte[] classBytes, String packagePath, String className) {
        File classPath = new File(packagePath);
        classPath.mkdirs();
        File classFile = new File(classPath, className + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(classFile)) {
            fos.write(classBytes);
        } catch (IOException e) {
            System.err.println("Failed to write class file due to error: " + e.getMessage());
        }
    }

    private String getDescriptor(Pair<String,Boolean> returnType, Pair<String,Boolean>... arguments) {
        StringBuilder descriptor = new StringBuilder("(");
        if(arguments != null) {
            for (Pair<String, Boolean> argument : arguments) {
                if (argument.b) {
                    descriptor.append("[");
                }
                descriptor.append(convertType(argument.a));
            }
        }
        descriptor.append(")");
        if (returnType.b) {
            descriptor.append("[");
        }
        descriptor.append(convertType(returnType.a));
        return descriptor.toString();
    }

    private String convertType(String type) {
        return switch (type) {
            case "byte" -> "B";
            case "short" -> "S";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            case "bool" -> "Z";
            case "char" -> "C";
            case "unit" -> "V";
            default -> "L" + type + ";";
        };
    }

    @Override
    public byte[] visitClassFile(LangParser.ClassFileContext ctx) {
        visitPackagePath(ctx.packagePath());
        classWriter.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, packagePath + "/" + className, null, "java/lang/Object", null);

        ctx.importPath().forEach(this::visitImportPath);

        ctx.memberVariable().forEach((memberVariableCtx) -> visitMemberVariable(memberVariableCtx, classWriter, null));
        ctx.function().forEach(this::visitFunction);
        ctx.object().forEach(this::visitObject);
        ctx.enum_().forEach(this::visitEnum);
        ctx.union().forEach(this::visitUnion);

        classWriter.visitEnd();
        byte[] classBytes = classWriter.toByteArray();
        if(writeClassFile) {
            writeClassFile(classBytes, packagePath, className);
        }
        return classBytes;
    }

    @Override
    public String visitPackagePath(LangParser.PackagePathContext ctx) {
        String path = visitPath(ctx.path()).replace(".", "/");
        packagePath = path;
        return path;
    }

    @Override
    public String visitImportPath(LangParser.ImportPathContext ctx) {
        String path = visitPath(ctx.path());
        imports.add(path);
        return path;
    }

    @Override
    public String visitPath(LangParser.PathContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitObject(LangParser.ObjectContext ctx) {
        String innerClassName = className + "$" + ctx.IDENTIFIER().getText();
        ClassWriter innerClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        innerClassWriter.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, innerClassName, null, null, null);

        ctx.memberVariable().forEach(this::visitMemberVariable);
        ctx.function().forEach((functionCtx) -> visitFunction(functionCtx, innerClassWriter));

        innerClassWriter.visitEnd();

        classWriter.visitInnerClass(ctx.IDENTIFIER().getText(), className, ctx.IDENTIFIER().getText(), Opcodes.ACC_PUBLIC);
        byte[] innerClassBytes = innerClassWriter.toByteArray();
        writeClassFile(innerClassBytes, packagePath, innerClassName);
        return null;
    }

    public Object visitFunction(LangParser.FunctionContext ctx, ClassWriter classWriter) {
        var isPublic = ctx.PUBLIC() != null ? Opcodes.ACC_PUBLIC : 0;
        var isStatic = ctx.STATIC() != null ? Opcodes.ACC_STATIC : 0;
        var opCodes = isPublic | isStatic;

        String name = ctx.IDENTIFIER().getText();
        boolean isConstructor = name.equals("new");

        String returnType = ctx.typeName() != null ? ctx.typeName().getText() : "unit";
        var methodVisitor = classWriter.visitMethod(opCodes, isConstructor ? "<init>" : name, "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        ctx.expression().forEach((expressionCtx) -> visitExpression(expressionCtx, methodVisitor));
        methodVisitor.visitEnd();
        return null;
    }

    @Override
    public Object visitFunction(LangParser.FunctionContext ctx) {
        return visitFunction(ctx, classWriter);
    }

    @Override
    public Object visitFunctionParameter(LangParser.FunctionParameterContext ctx) {
        return super.visitFunctionParameter(ctx);
    }

    public Object visitMemberVariable(LangParser.MemberVariableContext ctx, ClassWriter classWriter, MethodVisitor methodVisitor) {
        var isPublic = ctx.PUBLIC() != null ? Opcodes.ACC_PUBLIC : 0;
        return visitVariable(ctx.variable(), classWriter, methodVisitor, isPublic | Opcodes.ACC_STATIC);
    }

    @Override
    public Object visitMemberVariable(LangParser.MemberVariableContext ctx) {
        return visitMemberVariable(ctx, classWriter, null);
    }

    public Object visitVariable(LangParser.VariableContext ctx, ClassWriter classWriter, MethodVisitor methodVisitor, int opCodes) {
        var isMutable = ctx.MUTABLE() != null ? 0 : Opcodes.ACC_FINAL;
        String name = ctx.IDENTIFIER().getText();
        String type = visitTypeName(ctx.typeName());

        // Check if the type is a literal
        if(type != null && (!type.equals("unit") || !type.matches("^L.*;$"))) {
            Object literal = visitLiterals(ctx.literals());
            if(literal != null) {

            }
        }
        return null;
    }

    @Override
    public Object visitVariable(LangParser.VariableContext ctx) {
        return visitVariable(ctx, classWriter, null, 0);
    }

    public Object visitFunctionCall(LangParser.FunctionCallContext ctx, MethodVisitor methodVisitor) {
        /*
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("Hello, World");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 1);
         */
        return null;
    }

    @Override
    public Object visitFunctionCall(LangParser.FunctionCallContext ctx) {
        return visitFunctionCall(ctx, null);
    }

    @Override
    public Object visitFunctionCallArgument(LangParser.FunctionCallArgumentContext ctx) {
        return super.visitFunctionCallArgument(ctx);
    }

    public Object visitExpression(LangParser.ExpressionContext ctx, MethodVisitor methodVisitor) {
        if(ctx.functionCall() != null)
            visitFunctionCall(ctx.functionCall(), methodVisitor);
        if(ctx.ifStatement() != null)
            visitIfStatement(ctx.ifStatement());
        if(ctx.forLoop() != null)
            visitForLoop(ctx.forLoop());
        if(ctx.whileLoop() != null)
            visitWhileLoop(ctx.whileLoop());
        if(ctx.variable() != null)
            visitVariable(ctx.variable());
        return null;
    }

    @Override
    public Object visitExpression(LangParser.ExpressionContext ctx) {
        return visitExpression(ctx, null);
    }

    @Override
    public String visitTypeName(LangParser.TypeNameContext ctx) {
        return convertType(ctx.getText());
    }

    @Override
    public Object visitLiterals(LangParser.LiteralsContext ctx) {
        if(ctx != null) {
            if (ctx.STRING_LITERAL() != null) {
                return ctx.STRING_LITERAL().getText().replace("\"", "");
            } else if (ctx.CHAR_LITERAL() != null) {
                return ctx.CHAR_LITERAL().getText().replace("'", "");
            } else if (ctx.INTEGER_LITERAL() != null) {
                return Integer.parseInt(ctx.INTEGER_LITERAL().getText());
            } else if (ctx.FLOAT_LITERAL() != null) {
                return Float.parseFloat(ctx.FLOAT_LITERAL().getText());
            } else if (ctx.TRUE() != null) {
                return true;
            } else if (ctx.FALSE() != null) {
                return false;
            }
        }
        return null;
    }
}
