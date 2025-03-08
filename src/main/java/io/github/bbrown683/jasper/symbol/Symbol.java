package io.github.bbrown683.jasper.symbol;

import org.apache.bcel.generic.Type;

public class Symbol {
    private String name;
    private Type type;
    private Object value;
    private boolean isStatic;
    private boolean isMutable;
    private Symbol parent;
}
