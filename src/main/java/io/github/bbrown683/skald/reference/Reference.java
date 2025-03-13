package io.github.bbrown683.skald.reference;

public abstract class Reference {
    private final String name;
    private final boolean isPublic;
    private final boolean isStatic;

    public Reference(String name, boolean isPublic, boolean isStatic) {
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
