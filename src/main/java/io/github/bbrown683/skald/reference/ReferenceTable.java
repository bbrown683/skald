package io.github.bbrown683.skald.reference;

import java.util.HashMap;
import java.util.Map;

public class ReferenceTable {
    private final ReferenceLoader referenceLoader = new ReferenceLoader();
    private final Map<String,Map<String,Reference>> referenceMap = new HashMap<>();

    public ReferenceTable() {
        // Add the java.lang package by default as it is always imported by the JVM
        addImport("java.lang");
    }

    public void addImport(String packagePath) {
        if(referenceMap.containsKey(packagePath)) {
            return;
        }
        referenceMap.put(packagePath, referenceLoader.getReferences(packagePath));
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
