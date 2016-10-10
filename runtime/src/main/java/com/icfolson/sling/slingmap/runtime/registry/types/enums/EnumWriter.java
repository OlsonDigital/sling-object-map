package com.icfolson.sling.slingmap.runtime.registry.types.enums;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectWriter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

public class EnumWriter extends ClassObjectWriter {

    public static final EnumWriter INSTANCE = new EnumWriter();

    private EnumWriter() { }

    @Override
    protected Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext) {
        if (!(object instanceof Enum)) {
            throw new IllegalArgumentException("Writer is not valid for " + object.getClass());
        }
        Enum e = (Enum) object;
        ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        valueMap.put(JcrProperties.ENUM_VALUE, e.name());
        return resource;
    }
}
