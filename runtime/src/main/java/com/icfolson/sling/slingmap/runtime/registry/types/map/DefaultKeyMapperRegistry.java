package com.icfolson.sling.slingmap.runtime.registry.types.map;

import java.util.HashMap;
import java.util.Map;

public class DefaultKeyMapperRegistry implements KeyMapperRegistry {

    private final Map<Class<?>, KeyMapper> mappers = new HashMap<>();

    public DefaultKeyMapperRegistry() {
        registerKeyMapper(String.class, StringKeyMapper.INSTANCE);
    }

    @Override
    public KeyMapper getKeyMapper(final Class<?> inputClass) {
        return mappers.get(inputClass);
    }

    public void registerKeyMapper(final Class<?> inputClass, final KeyMapper mapper) {
        mappers.put(inputClass, mapper);
    }

}
