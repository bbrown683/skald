package io.github.bbrown683.skald.reference;

import org.apache.bcel.generic.Type;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class ReferenceLoader extends ClassLoader {
    private final Map<String,byte[]> referenceMap = new HashMap<>();

    public Map<String,Reference> getReferences(String packagePath) {
        var references = new HashMap<String,Reference>();
        if(packagePath.startsWith("java")) {
            for(var clazz : getBaseClasses(packagePath)) {
                var reference = getReference(clazz);
                references.put(reference.getName(), reference);
            }
        } else { // Look at the standard class paths
            var externalClassPaths = getExternalClassPaths(packagePath);
            for(var path : externalClassPaths) {
                var bytes = getClassBytes(path);
                if(bytes == null) {
                    continue;
                }
                String classFileName = path.getFileName().toString();
                String filename = packagePath + "." + classFileName.replace(".class", "");
                referenceMap.put(filename, bytes);
                try {
                    var clazz = findClass(filename);
                    var reference = getReference(clazz);
                    references.put(reference.getName(), reference);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return references;
    }

    // Classes under the java path are not in the classpath,
    // but are instead in the java.base module which is under the JRT filesystem
    // and must be loaded differently. We also cannot create an instance of the class
    // as the java package is prohibited for use.
    private List<Class<?>> getBaseClasses(String packagePath) {
        var classes = new ArrayList<Class<?>>();

        FileSystem filesystem = FileSystems.getFileSystem(URI.create("jrt:/"));
        var path = filesystem.getPath("modules","java.base", packagePath.replace(".", "/"));
        // Get all files under the path
        try (var stream = Files.walk(path)) {
            var classNames = stream
                    .filter(Files::isRegularFile)
                    .filter(Predicate.not(Files::isDirectory))
                    .filter(f -> {
                        // Filename checks:
                        // 1. Must be a class file
                        // 2. Must not be an inner class
                        // 3. Must be directly under lang package.
                        var filename = f.getFileName().toString();
                        var parentFilename = f.getParent().getFileName().toString();
                        return filename.endsWith(".class") && !filename.contains("$") && parentFilename.equals("lang");
                    })
                    .map(f -> {
                        var filename = f.toString();
                        return filename
                                .replace(".class", "")
                                .replace("modules/java.base/", "")
                                .replace("/", ".");
                    })
                    .toList();
            for(var className : classNames) {
                classes.add(Class.forName(className));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private List<Path> getExternalClassPaths(String packagePath) {
        return Collections.emptyList();
    }


    private byte[] getClassBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Reference getReference(Class<?> clazz) {
        var typeFields = new ArrayList<VariableReference>();
        boolean isEnum = clazz.isEnum();
        for(var fields : clazz.getFields()) {
            var fieldName = fields.getName();
            var fieldType = fields.getType();
            var isPublic = Modifier.isPublic(fields.getModifiers());
            var isStatic = Modifier.isStatic(fields.getModifiers());
            var isMutable = !Modifier.isFinal(fields.getModifiers());
            var isArray = fields.getType().isArray();
            typeFields.add(new VariableReference(fieldName, isPublic, isStatic, Type.getType(fieldType), isMutable, isArray));
        }

        var typeFunctions = new ArrayList<FunctionReference>();
        for(var method : clazz.getMethods()) {
            var methodName = method.getName();
            var returnType = method.getReturnType();
            var isPublic = Modifier.isPublic(method.getModifiers());
            var isStatic = Modifier.isStatic(method.getModifiers());

            var functionParameters = new ArrayList<VariableReference>();
            for(var parameter : method.getParameters()) {
                var parameterName = parameter.getName();
                var parameterType = parameter.getType();
                var isArray = parameter.getType().isArray();
                functionParameters.add(new VariableReference(parameterName, false, false, Type.getType(parameterType), true, isArray));
            }
            typeFunctions.add(new FunctionReference(methodName, isPublic, isStatic, Type.getType(returnType), functionParameters));
        }

        var superClass = clazz.getSuperclass();
        String superClassName = superClass == null ? null : superClass.getName();
        //var types = clazz.getClasses();
        if(isEnum) { // TODO: To be tested.
            return new EnumReference(clazz.getSimpleName(), true, false);
        } else {
            return new TypeReference(clazz.getSimpleName(), true, false, typeFields, typeFunctions, null, superClassName);
        }
    }

    @Override
    public Class findClass(String name) {
        var bytes = referenceMap.get(name);
        if (bytes == null) {
            return null;
        }
        return defineClass(name, bytes, 0, bytes.length);
    }
}
