package io.github.bbrown683.jasper.jvm;

import org.apache.bcel.generic.*;

// Performs all necessary bytecode generation for the language.
public class InstructionUtil {
    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
    private final InstructionFactory instructionFactory;

    public InstructionUtil(ClassGen classGen, ConstantPoolGen constantPoolGen) {
        this.classGen = classGen;
        this.constantPoolGen = constantPoolGen;
        instructionFactory = new InstructionFactory(classGen, constantPoolGen);
    }

    private void returnBoolean(InstructionList instructionList) {
        returnInt(instructionList);
    }

    private void returnChar(InstructionList instructionList) {
        returnInt(instructionList);
    }

    private void returnString(InstructionList instructionList) {
        returnReference(instructionList);
    }

    private void returnByte(InstructionList instructionList) {
        returnInt(instructionList);
    }

    private void returnShort(InstructionList instructionList) {
        returnInt(instructionList);
    }

    private void returnInt(InstructionList instructionList) {
        instructionList.append(new IRETURN());
        System.out.println("Returning Int");
    }

    private void returnLong(InstructionList instructionList) {
        instructionList.append(new LRETURN());
        System.out.println("Returning Long");
    }

    private void returnFloat(InstructionList instructionList) {
        instructionList.append(new FRETURN());
        System.out.println("Returning Float");
    }

    private void returnDouble(InstructionList instructionList) {
        instructionList.append(new DRETURN());
        System.out.println("Returning Double");
    }

    private void returnReference(InstructionList instructionList) {
        instructionList.append(new ARETURN());
        System.out.println("Returning Reference");
    }

    public void returnVoid(InstructionList instructionList) {
        instructionList.append(new RETURN());
        System.out.println("Returning Void");
    }

    public InstructionList insertReturn(Type returnType) {
        InstructionList instructionList = new InstructionList();
        if(returnType.equals(Type.VOID)) {
            returnVoid(instructionList);
        } else if(returnType.equals(Type.BOOLEAN)) {
            returnBoolean(instructionList);
        } else if(returnType.equals(Type.CHAR)) {
            returnChar(instructionList);
        } else if(returnType.equals(Type.STRING)) {
            returnString(instructionList);
        } else if(returnType.equals(Type.BYTE)) {
            returnByte(instructionList);
        } else if(returnType.equals(Type.SHORT)) {
            returnShort(instructionList);
        } else if(returnType.equals(Type.INT)) {
            returnInt(instructionList);
        } else if(returnType.equals(Type.LONG)) {
            returnLong(instructionList);
        } else if(returnType.equals(Type.FLOAT)) {
            returnFloat(instructionList);
        } else if(returnType.equals(Type.DOUBLE)) {
            returnDouble(instructionList);
        } else {
            returnReference(instructionList);
        }
        return instructionList;
    }

    private void pushBoolean(InstructionList instructionList, boolean value) {
        pushInt(instructionList, value ? 1 : 0);
    }

    private void pushChar(InstructionList instructionList, char value) {
        pushByte(instructionList, (byte)value);
    }

    private void pushString(InstructionList instructionList, String value) {
        int index = constantPoolGen.addString(value);
        instructionList.append(new LDC(index));
        System.out.println("Pushing String: " + value);
    }

    private void pushByte(InstructionList instructionList, byte value) {
        if (value >= -1 && value <= 5) {
            instructionList.append(new ICONST(value));
        } else {
            instructionList.append(new BIPUSH(value));
        }
        System.out.println("Pushing Byte: " + value);
    }

    private void pushShort(InstructionList instructionList, short value) {
        if (value >= -1 && value <= 5) {
            instructionList.append(new ICONST(value));
        } else {
            instructionList.append(new SIPUSH(value));
        }
        System.out.println("Pushing Short: " + value);
    }

    private void pushInt(InstructionList instructionList, int value) {
        if(value >= -1 && value <= 5) {
            instructionList.append(new ICONST(value));
        } else {
            int index = constantPoolGen.addInteger(value);
            instructionList.append(new LDC(index));
        }
        System.out.println("Pushing Int: " + value);
    }

    private void pushLong(InstructionList instructionList, long value) {
        if(value == 0 || value == 1) {
            instructionList.append(new LCONST(value));
        } else {
            int index = constantPoolGen.addLong(value);
            instructionList.append(new LDC(index));
        }
        System.out.println("Pushing Long: " + value);
    }

    private void pushFloat(InstructionList instructionList, float value) {
        if(value == 0.0 || value == 1.0 || value == 2.0) {
            instructionList.append(new FCONST(value));
        } else {
            int index = constantPoolGen.addFloat(value);
            instructionList.append(new LDC(index));
        }
        System.out.println("Pushing Float: " + value);
    }

    private void pushDouble(InstructionList instructionList, double value) {
        if(value == 0.0 || value == 1.0) {
            instructionList.append(new DCONST(value));
        } else {
            int index = instructionFactory.getConstantPool().addDouble(value);
            instructionList.append(new LDC2_W(index));
        }
        System.out.println("Pushing Double: " + value);
    }

    private void pushNull(InstructionList instructionList) {
        instructionList.append(InstructionConst.ACONST_NULL);
        System.out.println("Pushing Null");
    }

    public InstructionList pushLiteral(Object value) {
        InstructionList instructionList = new InstructionList();
        switch (value) {
            case null -> pushNull(instructionList);
            case Boolean b -> pushBoolean(instructionList, b);
            case Character c -> pushChar(instructionList, c);
            case String s -> pushString(instructionList, s);
            case Byte b -> pushByte(instructionList, b);
            case Short s -> pushShort(instructionList, s);
            case Integer i -> pushInt(instructionList, i);
            case Long l -> pushLong(instructionList, l);
            case Float f -> pushFloat(instructionList, f);
            case Double d -> pushDouble(instructionList, d);
            default -> throw new RuntimeException("Unsupported literal type: " + value.getClass().getName());
        }
        return instructionList;
    }

    private void loadBoolean(InstructionList instructionList, int index) {
        loadInt(instructionList, index);
    }

    private void loadChar(InstructionList instructionList, int index) {
        loadByte(instructionList, index);
    }

    private void loadString(InstructionList instructionList, int index) {
        loadReference(instructionList, index);
    }

    private void loadByte(InstructionList instructionList, int index) {
        loadInt(instructionList, index);
    }

    private void loadShort(InstructionList instructionList, int index) {
        loadInt(instructionList, index);
    }

    private void loadInt(InstructionList instructionList, int index) {
        instructionList.append(new ILOAD(index));
    }

    private void loadLong(InstructionList instructionList, int index) {
        instructionList.append(new LLOAD(index));
    }

    private void loadFloat(InstructionList instructionList, int index) {
        instructionList.append(new FLOAD(index));
    }

    private void loadDouble(InstructionList instructionList, int index) {
        instructionList.append(new DLOAD(index));
    }

    private void loadReference(InstructionList instructionList, int index) {
        instructionList.append(new ALOAD(index));
    }

    public InstructionList loadVariable(LocalVariableGen localVariable) {
        InstructionList instructionList = new InstructionList();

        int index = localVariable.getIndex();
        Type type = localVariable.getType();
        if(type.equals(Type.BOOLEAN)) {
            loadBoolean(instructionList, index);
        } else if(type.equals(Type.CHAR)) {
            loadChar(instructionList, index);
        } else if(type.equals(Type.STRING)) {
            loadString(instructionList, index);
        } else if(type.equals(Type.BYTE)) {
            loadByte(instructionList, index);
        } else if(type.equals(Type.SHORT)) {
            loadShort(instructionList, index);
        } else if(type.equals(Type.INT)) {
            loadInt(instructionList, index);
        } else if(type.equals(Type.LONG)) {
            loadLong(instructionList, index);
        } else if(type.equals(Type.FLOAT)) {
            loadFloat(instructionList, index);
        } else if(type.equals(Type.DOUBLE)) {
            loadDouble(instructionList, index);
        } else {
            loadReference(instructionList, index);
        }
        return instructionList;
    }

    public InstructionList loadStaticReference(String className, String fieldName, Type fieldType) {
        int index = constantPoolGen.addFieldref(className, fieldName, fieldType.getSignature());
        InstructionList instructionList = new InstructionList();
        instructionList.append(new GETSTATIC(index));
        return instructionList;
    }

    private void storeBoolean(InstructionList instructionList, int index) {
        storeInt(instructionList, index);
    }

    private void storeChar(InstructionList instructionList, int index) {
        storeInt(instructionList, index);
    }

    private void storeString(InstructionList instructionList, int index) {
        storeReference(instructionList, index);
    }

    private void storeByte(InstructionList instructionList, int index) {
        storeInt(instructionList, index);
    }

    private void storeShort(InstructionList instructionList, int index) {
        storeInt(instructionList, index);
    }

    private void storeInt(InstructionList instructionList, int index) {
        instructionList.append(new ISTORE(index));
        System.out.println("Storing Int");
    }

    private void storeLong(InstructionList instructionList, int index) {
        instructionList.append(new LSTORE(index));
        System.out.println("Storing Long");
    }

    private void storeFloat(InstructionList instructionList, int index) {
        instructionList.append(new FSTORE(index));
        System.out.println("Storing Float");
    }

    private void storeDouble(InstructionList instructionList, int index) {
        instructionList.append(new DSTORE(index));
        System.out.println("Storing Double");
    }

    private void storeReference(InstructionList instructionList, int index) {
        instructionList.append(new ASTORE(index));
        System.out.println("Storing Reference");
    }

    public InstructionList storeVariable(LocalVariableGen localVariable) {
        InstructionList instructionList = new InstructionList();

        int index = localVariable.getIndex();
        Type type = localVariable.getType();
        if(type.equals(Type.BOOLEAN)) {
            storeBoolean(instructionList, index);
        } else if(type.equals(Type.CHAR)) {
            storeChar(instructionList, index);
        } else if(type.equals(Type.STRING)) {
            storeString(instructionList, index);
        } else if(type.equals(Type.BYTE)) {
            storeByte(instructionList, index);
        } else if(type.equals(Type.SHORT)) {
            storeShort(instructionList, index);
        } else if(type.equals(Type.INT)) {
            storeInt(instructionList, index);
        } else if(type.equals(Type.LONG)) {
            storeLong(instructionList, index);
        } else if(type.equals(Type.FLOAT)) {
            storeFloat(instructionList, index);
        } else if(type.equals(Type.DOUBLE)) {
            storeDouble(instructionList, index);
        } else {
            storeReference(instructionList, index);
        }
        return instructionList;
    }

    public InstructionList insertNop() {
        InstructionList instructionList = new InstructionList();
        instructionList.append(InstructionConst.NOP);
        return instructionList;
    }

    public InstructionList insertField(String className, String fieldName, Type fieldType) {
        int index = constantPoolGen.addFieldref(className, fieldName, fieldType.getSignature());
        InstructionList instructionList = new InstructionList();
        instructionList.append(new PUTFIELD(index));
        return instructionList;
    }

    public InstructionList insertSelfReference() {
        InstructionList instructionList = new InstructionList();
        instructionList.append(InstructionConst.THIS);
        return instructionList;
    }

    public InstructionList callSuper(String superClass) {
        if(superClass == null) superClass = "java/lang/Object";

        InstructionList instructionList = new InstructionList();
        instructionList.append(insertSelfReference());
        instructionList.append(new INVOKESPECIAL(constantPoolGen.addMethodref(superClass, "<init>", "()V")));
        return instructionList;
    }

    public InstructionList callMethod(String className, String methodName, Type returnType, Type[] argumentTypes) {
        int index = constantPoolGen.addMethodref(className, methodName, Type.getMethodSignature(returnType, argumentTypes));
        InstructionList instructionList = new InstructionList();
        instructionList.append(new INVOKESTATIC(index));
        return instructionList;
    }
}
