package io.github.bbrown683.skald.symbol.external;

// External symbols are symbols that are defined in other files unlike LocalSymbols, and are referenced in the current file.
// They do not need scope information, as they are not defined in the current file.
public abstract class ExternalSymbol {
    private final String name;
    private final boolean isPublic;
    private final boolean isStatic;

    public ExternalSymbol(String name, boolean isPublic, boolean isStatic) {
        this.name = name;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
    }

    public String getName() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
