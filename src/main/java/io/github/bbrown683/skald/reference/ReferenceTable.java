package io.github.bbrown683.skald.reference;

import java.util.HashMap;
import java.util.Map;

public class ReferenceTable {
    private final ReferenceLoader referenceLoader = new ReferenceLoader();
    private final Map<String,Map<String,Reference>> referenceMap = new HashMap<>();

    public ReferenceTable() {
        // Add the java.lang package by default as it is always imported by the JVM
        addImport("java.lang.*");
    }

    public void addImport(String importPath) {
        String path;
        String packagePath;

        boolean isPackage = importPath.endsWith(".*");
        if (isPackage) { // Strip the .* from the end of the package path
            packagePath = importPath.substring(0, importPath.lastIndexOf(".*"));
            path = packagePath;
        } else { // Strip the class name from the end of the package path
            packagePath = importPath.substring(0, importPath.lastIndexOf("."));
            path = importPath;
        }

        var references = referenceLoader.getReferences(path, isPackage);
        if(referenceMap.containsKey(packagePath)) {
            referenceMap.get(packagePath).putAll(references);
        } else {
            referenceMap.put(packagePath, references);
        }
    }

    public Reference getReference(String name) {
        var reference = referenceMap.get(name);
        if(reference == null) {
            for(var importPath : referenceMap.keySet()) {
                reference = referenceMap.get(importPath + "." + name);
                if(reference != null) {
                    break;
                }
            }
        }
        return null;
    }
}
