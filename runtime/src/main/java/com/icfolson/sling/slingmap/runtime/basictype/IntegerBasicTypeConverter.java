package com.icfolson.sling.slingmap.runtime.basictype;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeConverter;

/**
 * Converts an Integer property on a Java object to a Long for storage on a JCR-based Sling Resource
 */
public class IntegerBasicTypeConverter implements BasicTypeConverter {

    public static final IntegerBasicTypeConverter INSTANCE = new IntegerBasicTypeConverter();

    private IntegerBasicTypeConverter() { }

    @Override
    public Object resourceToObjectPropertyValue(final Object resourcePropertyValue) {
        if (resourcePropertyValue instanceof Long) {
            return ((Long) resourcePropertyValue).intValue();
        } else if (resourcePropertyValue instanceof Integer) {
            return resourcePropertyValue;
        }
        return null;
    }

    @Override
    public Object objectToResourcePropertyValue(final Object primitiveValue) {
        if (primitiveValue instanceof Integer) {
            return Long.valueOf((int) primitiveValue);
        }
        return primitiveValue;
    }
}
