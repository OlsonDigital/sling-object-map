package com.icfolson.sling.slingmap.api.constants;

public final class JcrProperties {

    private static final String SEPARATOR = ":";

    public static final String RUNTIME_CLASS = Namespaces.MAP + SEPARATOR + "runtimeClass";
    public static final String BUNDLE_NAME = Namespaces.MAP + SEPARATOR + "bundleName";
    public static final String ENUM_VALUE = Namespaces.MAP + SEPARATOR + "enumValue";
    public static final String MODEL_CLASS = Namespaces.MAP + SEPARATOR + "modelClass";
    public static final String REFERENCE_PATH = Namespaces.MAP + SEPARATOR + "referencePath";
    public static final String VERSION = Namespaces.MAP + SEPARATOR + "version";
    public static final String BASIC_VALUE = Namespaces.MAP + SEPARATOR + "value";

    private JcrProperties() { }
}
