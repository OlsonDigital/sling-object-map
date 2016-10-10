package com.icfolson.sling.slingmap.runtime.registry.types.reference;

public class Reference {

    private Object referenced;
    private String referencePath;

    public Object getReferenced() {
        return referenced;
    }

    public void setReferenced(final Object referenced) {
        this.referenced = referenced;
    }

    public String getReferencePath() {
        return referencePath;
    }

    public void setReferencePath(final String referencePath) {
        this.referencePath = referencePath;
    }
}
