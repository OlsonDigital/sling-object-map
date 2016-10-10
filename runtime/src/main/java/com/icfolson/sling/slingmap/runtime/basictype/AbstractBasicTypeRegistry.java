package com.icfolson.sling.slingmap.runtime.basictype;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeConverter;
import com.icfolson.sling.slingmap.api.basictype.BasicTypeRegistry;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBasicTypeRegistry implements BasicTypeRegistry {

    private final Map<Class<?>, BasicTypeConverter> converters = new HashMap<>();

    public void registerBasicType(final Class<?> basicType) {
        converters.put(basicType, null);
    }

    public void registerBasicType(final Class<?> basicType, final BasicTypeConverter converter) {
        converters.put(basicType, converter);
    }

    public boolean isBasicType(final Class<?> type) {
        return converters.containsKey(type);
    }

    public BasicTypeConverter getBasicConverter(final Class<?> basicType) {
        BasicTypeConverter converter = converters.get(basicType);
        if (converter == null) {
            converter = new NoOpConverter();
        }
        return converter;
    }

    private static class NoOpConverter implements BasicTypeConverter {

        @Override
        public Object resourceToObjectPropertyValue(final Object resourcePropertyValue) {
            return resourcePropertyValue;
        }

        @Override
        public Object objectToResourcePropertyValue(final Object primitiveValue) {
            return primitiveValue;
        }

    }
}
