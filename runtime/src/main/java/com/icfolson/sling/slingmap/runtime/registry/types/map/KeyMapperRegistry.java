package com.icfolson.sling.slingmap.runtime.registry.types.map;

public interface KeyMapperRegistry {

    KeyMapper getKeyMapper(final Class<?> inputClass);

}
