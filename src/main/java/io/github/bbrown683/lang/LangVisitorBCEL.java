package io.github.bbrown683.lang;

import io.github.bbrown683.lang.antlr4.LangParser;
import io.github.bbrown683.lang.antlr4.LangParserBaseVisitor;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.FileOutputStream;

public class LangVisitorBCEL extends LangParserBaseVisitor<Object> {
    private ClassGen classGen;

    public LangVisitorBCEL(String className) {
        classGen = new ClassGen(className, "java.lang.Object",  null, Const.ACC_PUBLIC | Const.ACC_SUPER, null);
    }

    @Override
    public Object visitClassFile(LangParser.ClassFileContext ctx) {
        ctx.function().forEach(this::visit);
        return classGen.getJavaClass().getBytes();
    }

    @Override
    public Object visitFunction(LangParser.FunctionContext ctx) {
        GETSTATIC getstatic = new GETSTATIC(classGen.getConstantPool().addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;"));
        LDC ldc = new LDC(classGen.getConstantPool().addString("Hello, World!"));

        InstructionList instructionList = new InstructionList();
        instructionList.append(getstatic);
        instructionList.append(ldc);
        instructionList.append(InstructionConst.RETURN);

        int isStatic = ctx.STATIC() != null ? Const.ACC_STATIC : 0;
        int isPublic = ctx.PUBLIC() != null ? Const.ACC_PUBLIC : 0;
        var method = new MethodGen(isPublic | isStatic,
                Type.VOID,
                new Type[] { new ArrayType(Type.STRING, 1) },
                new String[] { "args" },
                "main",
                classGen.getClassName(),
                instructionList,
                classGen.getConstantPool());
        method.setMaxLocals();
        method.setMaxStack();
        classGen.addMethod(method.getMethod());
        instructionList.dispose();
        return null;
    }

    @Override
    public Object visitVariable(LangParser.VariableContext ctx) {
        return null;
    }
}
