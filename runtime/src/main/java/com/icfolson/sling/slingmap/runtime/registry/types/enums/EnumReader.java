package com.icfolson.sling.slingmap.runtime.registry.types.enums;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;

public class EnumReader extends ClassObjectReader {

    public static final EnumReader INSTANCE = new EnumReader();

    private EnumReader() { }

    @Override
    protected Object doRead(final Class<?> objectClass, final Resource resource, final ReadContext readContext) {
        if (!objectClass.isEnum()) {
            throw new IllegalStateException("Reader not valid for class " + objectClass.getName());
        }
        String enumName = resource.getValueMap().get(JcrProperties.ENUM_VALUE, String.class);
        if (enumName != null) {
            for (final Object enumVal : objectClass.getEnumConstants()) {
                Enum e = (Enum) enumVal;
                if (e.name().equals(enumName)) {
                    return e;
                }
            }
        }
        return null;
    }
}
