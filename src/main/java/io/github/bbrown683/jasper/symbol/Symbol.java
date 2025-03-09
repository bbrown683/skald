package io.github.bbrown683.jasper.symbol;

import lombok.*;
import org.apache.bcel.generic.Type;

@Getter
@Setter
public abstract class Symbol {
    protected String name;
    protected String line;
    protected Symbol parent;

    public Symbol(String name) {
        this.name = name;
    }
}
