package io.github.bbrown683.jasper.reference;

import io.github.bbrown683.jasper.symbol.FunctionSymbol;
import io.github.bbrown683.jasper.symbol.TypeSymbol;
import io.github.bbrown683.jasper.symbol.VariableSymbol;
import org.apache.bcel.generic.Type;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenceTable {
    private Map<String,TypeReference> referenceMap = new HashMap<>();

    public TypeReference getReference(String name) {
        
    }

    private void loadBuiltInTypes() {
        List<String> builtInTypes = List.of("Integer");
        for (String type : builtInTypes) {
            loadBuiltInType(type);
        }
    }

    private void loadBuiltInType(String type) {
        try {
            Class<?> clazz = Class.forName("java.lang." + type);
            var typeSymbol = new TypeSymbol(clazz.getSimpleName(), null, clazz.getPackageName(), true, true);
            for(var fields : clazz.getFields()) {
                var fieldName = fields.getName();
                var fieldType = fields.getType();
                var isPublic = Modifier.isPublic(fields.getModifiers());
                var isStatic = Modifier.isStatic(fields.getModifiers());
                var isMutable = !Modifier.isFinal(fields.getModifiers());
                var isArray = fields.getType().isArray();
                typeSymbol.addChild(new VariableSymbol(fieldName, null, Type.getType(fieldType), isPublic, isStatic, isMutable, isArray, false, false, null));
            }

            for(var method : clazz.getMethods()) {
                var methodName = method.getName();
                var returnType = method.getReturnType();
                var isPublic = Modifier.isPublic(method.getModifiers());
                var isStatic = Modifier.isStatic(method.getModifiers());

                typeSymbol.addChild(new FunctionSymbol(methodName, null, Type.getType(returnType), isPublic, isStatic));

                for(var parameter : method.getParameters()) {
                    var parameterName = parameter.getName();
                    var parameterType = parameter.getType();
                    var isArray = parameter.getType().isArray();
                    typeSymbol.addChild(new VariableSymbol(parameterName, null, Type.getType(parameterType), isPublic, isStatic, true, isArray, true, false, null));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
