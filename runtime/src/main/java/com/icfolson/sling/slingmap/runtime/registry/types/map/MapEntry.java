package com.icfolson.sling.slingmap.runtime.registry.types.map;

public class MapEntry {

    private Object key;
    private Object value;

    public Object getKey() {
        return key;
    }

    public void setKey(final Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }
}
