package io.github.bbrown683.skald.symbol.external;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ExternalSymbolTable {
    private final ExternalReferenceLoader symbolLoader = new ExternalReferenceLoader();
    private final Map<String,Map<String, ExternalSymbol>> symbolMap = new HashMap<>();

    public ExternalSymbolTable() {
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

        var symbols = symbolLoader.getSymbols(path, isPackage);
        if(symbolMap.containsKey(packagePath)) {
            symbolMap.get(packagePath).putAll(symbols);
        } else {
            symbolMap.put(packagePath, symbols);
        }
    }

    // TODO: Should probably be a list, in the event of overloading
    public <T extends ExternalSymbol> List<T> getSymbol(String symbolName, String symbolParentName, Class<T> clazz) {
        for (var packagePath : symbolMap.keySet()) {
            var symbol = getSymbol(symbolName, symbolParentName, packagePath, clazz);
            if (!symbol.isEmpty()) {
                return symbol;
            }
        }
        return Collections.emptyList();
    }

    public <T extends ExternalSymbol> List<T> getSymbol(String symbolName, String symbolParentName, String packagePath, Class<T> clazz) {
        var symbols = new ArrayList<T>();

        var packageSymbols = symbolMap.get(packagePath);
        if(packageSymbols != null) {
            if(clazz == ExternalTypeSymbol.class) {
                symbols.add(clazz.cast(packageSymbols.get(symbolName)));
            } else {
                var symbol = packageSymbols.get(symbolParentName);
                if(symbol instanceof ExternalTypeSymbol typeReference) {
                    if (clazz == ExternalVariableSymbol.class) {
                        for (var field : typeReference.getFields()) {
                            if (field.getName().equals(symbolName)) {
                                symbols.add(clazz.cast(field));
                            }
                        }
                    } else if (clazz == ExternalFunctionSymbol.class) {
                        for (var function : typeReference.getFunctions()) {
                            if (function.getName().equals(symbolName)) {
                                symbols.add(clazz.cast(function));
                            }
                        }
                    }
                }
            }
        }
        return symbols;
    }
}
