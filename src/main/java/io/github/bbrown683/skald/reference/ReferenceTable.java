package io.github.bbrown683.skald.reference;

import org.apache.commons.lang3.StringUtils;

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
            packagePath = StringUtils.substringBefore(importPath, ".*");
            path = packagePath;
        } else { // Strip the class name from the end of the package path
            packagePath = StringUtils.substringBeforeLast(importPath, ".");
            path = importPath;
        }

        var references = referenceLoader.getReferences(path, isPackage);
        if(referenceMap.containsKey(packagePath)) {
            referenceMap.get(packagePath).putAll(references);
        } else {
            referenceMap.put(packagePath, references);
        }
    }

    public Reference getReference(String packagePath, String referenceName, ReferenceSearchType searchType, String referenceParent) {
        var packageReferences = referenceMap.get(packagePath);
        if(packageReferences != null) {
            if(searchType == ReferenceSearchType.TYPE) {
                return packageReferences.get(referenceName);
            } else {
                var reference = packageReferences.get(referenceParent);
                if(reference instanceof TypeReference typeReference) {
                    if (searchType == ReferenceSearchType.FIELD) {
                        for (var field : typeReference.getFields()) {
                            if (field.getName().equals(referenceName)) {
                                return field;
                            }
                        }
                    } else if (searchType == ReferenceSearchType.FUNCTION) {
                        for (var function : typeReference.getFunctions()) {
                            if (function.getName().equals(referenceName)) {
                                return function;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
