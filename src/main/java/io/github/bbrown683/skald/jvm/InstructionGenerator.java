package io.github.bbrown683.skald.jvm;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstructionGenerator {
    private final ClassGen classGen;
    private final ConstantPoolGen constantPoolGen;
    private final InstructionUtil instructionUtil;
    private final InstructionList instructionList = new InstructionList();
    private MethodGen methodGen;

    public InstructionGenerator(ClassGen classGen, ConstantPoolGen constantPoolGen) {
        this(classGen, constantPoolGen, null);
    }

    public InstructionGenerator(ClassGen classGen, ConstantPoolGen constantPoolGen, MethodGen methodGen) {
        this.classGen = classGen;
        this.constantPoolGen = constantPoolGen;
        this.methodGen = methodGen;
        instructionUtil = new InstructionUtil(classGen, constantPoolGen);
    }

    public InstructionList getInstructionList() {
        return instructionList;
    }

    public void callSuper(String superClass) {
        instructionList.append(instructionUtil.callSuper(superClass));
    }

    private void pushLiteral(Object literal) {
        instructionList.append(instructionUtil.pushLiteral(literal));
    }

    public void addVariableAsLiteralField(String variableName, Object literal, Type type, int accessFlags, String className) {
        pushLiteral(literal);

        FieldGen fieldGen = new FieldGen(accessFlags, type, variableName, constantPoolGen);
        classGen.addField(fieldGen.getField());

        instructionList.append(instructionUtil.insertField(className, variableName, type));
    }

    public LocalVariableGen addVariableLiteralAsLiteralLocalVariable(String variableName, Object literal, Type variableType) {
        pushLiteral(literal);

        var localVariable = methodGen.addLocalVariable(variableName, variableType, null, null);

        instructionList.append(instructionUtil.storeVariable(localVariable));
        // Set end so we can determine the scope of the local variable in the method.
        localVariable.setStart(instructionList.getStart());
        localVariable.setEnd(instructionList.getEnd());
        return localVariable;
    }

    public void addVariableAsReference(String variableName, LocalVariableGen referenceVariable) {
        instructionList.append(instructionUtil.loadReference(referenceVariable));

        var localVariable = methodGen.addLocalVariable(variableName, referenceVariable.getType(), null, null);
        instructionList.append(instructionUtil.storeVariable(localVariable));

        // Set end so we can determine the scope of the local variable in the method.
        localVariable.setStart(instructionList.getStart());
        localVariable.setEnd(instructionList.getEnd());
    }

    public void addVariableAsStaticReference(String variableName, String className, String referenceName, Type referenceType) {
        instructionList.append(instructionUtil.loadStaticReference(className, referenceName, referenceType));

        var localVariable = methodGen.addLocalVariable(variableName, referenceType, null, null);
        instructionUtil.storeVariable(localVariable);

        // Set end so we can determine the scope of the local variable in the method.
        localVariable.setStart(instructionList.getStart());
        localVariable.setEnd(instructionList.getEnd());
    }

    public void insertReturn(Type returnType) {
        instructionList.append(instructionUtil.insertReturn(returnType));
    }

    public void callFunction(String className, String functionName, Type returnType, Type[] argumentTypes, boolean isStatic) {
        instructionList.append(instructionUtil.callFunction(className, functionName, returnType, argumentTypes, isStatic));
    }

    public MethodGen getMethodGen(int accessFlags, String name, Type returnType, List<Type> parameterTypes, List<String> parameterNames) {
        methodGen = new MethodGen(accessFlags,
                returnType,
                parameterTypes.toArray(new Type[0]),
                parameterNames.toArray(new String[0]),
                name,
                classGen.getClassName(),
                instructionList,
                constantPoolGen);
        return methodGen;
    }

    // The way local variables work in the JVM is that they have typically two instructions:
    // 1. Push the value onto the stack
    // 2. Store the value in the local variable
    // Thus we need to set the start instruction to the next available instruction after these,
    // as at that point the variable is constructed. Anypoint before that and the variable will be corrupted.
    // TODO: We need to figure out how to handle the scope of the local variable as it is currently set to the end of the method,
    //  but situations such as if statements or loops will require the variable to be set to the end of the block.
    public void updateVariableScope() {
        for(var localVariable : methodGen.getLocalVariables()) {
            var localEnd = localVariable.getEnd();
            var methodEnd = instructionList.getEnd();
            if(localEnd != methodEnd) {
                var localNext  = localEnd.getNext();
                localVariable.setStart(localNext != null ? localNext : methodEnd);
                localVariable.setEnd(methodEnd);
            }
        }
    }

    public void completeFunction() {
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        instructionList.dispose();
    }
}
