package io.github.bbrown683.skald.jvm;

import org.apache.bcel.generic.*;
import org.apache.commons.lang3.StringUtils;

public class InstructionGenerator {
    private final ClassGen classGen;
    private final ConstantPoolGen constantPoolGen;
    private final InstructionUtil instructionUtil;
    private final InstructionList instructionList;
    private final MethodGen methodGen;

    public InstructionGenerator(ClassGen classGen, ConstantPoolGen constantPoolGen, InstructionList instructionList) {
        this(classGen, constantPoolGen, instructionList, null);
    }

    public InstructionGenerator(ClassGen classGen, ConstantPoolGen constantPoolGen, InstructionList instructionList, MethodGen methodGen) {
        this.classGen = classGen;
        this.constantPoolGen = constantPoolGen;
        this.instructionList = instructionList;
        this.methodGen = methodGen;
        instructionUtil = new InstructionUtil(classGen, constantPoolGen);
    }

    public void callSuper() {
        instructionList.append(instructionUtil.insertSelfReference());
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

    public void addVariableAsStaticReference(String referenceName, String variableName, Type variableType) {
        String className = StringUtils.substringBeforeLast(referenceName, ".");
        String fieldName = StringUtils.substringAfterLast(referenceName, ".");

        instructionList.append(instructionUtil.loadStaticReference(className, fieldName, variableType));

        var localVariable = methodGen.addLocalVariable(variableName, variableType, null, null);
        instructionUtil.storeVariable(localVariable);

        // Set end so we can determine the scope of the local variable in the method.
        localVariable.setStart(instructionList.getStart());
        localVariable.setEnd(instructionList.getEnd());
    }
}
