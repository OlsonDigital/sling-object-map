package com.icfolson.sling.slingmap.runtime.basictype;

import java.util.Date;

/**
 * A registry that defines basic support for writing common types as JCR-based sling resource properties.
 */
public final class JcrBasicTypeRegistry extends AbstractBasicTypeRegistry {

    public JcrBasicTypeRegistry() {
        registerBasicType(String.class);
        registerBasicType(Long.class);
        registerBasicType(Float.class);
        registerBasicType(Double.class);
        registerBasicType(Boolean.class);
        registerBasicType(Date.class, DateBasicTypeConverter.INSTANCE);
        registerBasicType(Integer.class, IntegerBasicTypeConverter.INSTANCE);
    }


}
