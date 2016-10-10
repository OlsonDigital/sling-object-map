package com.icfolson.sling.slingmap.runtime.registry.types.basic;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeWriter;
import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectWriter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

/**
 * Writes a basic value to a resource (rather than a property, which might be needed when the object is a part of a
 * collection, or the value is written directly to the Sling resource tree).
 */
public class BasicValueObjectWriter extends ClassObjectWriter {

    private final BasicTypeWriter writer;

    public BasicValueObjectWriter(final BasicTypeWriter writer) {
        this.writer = writer;
    }

    @Override
    protected Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException {
        final Object basicValue = writer.objectToResourcePropertyValue(object);
        resource.adaptTo(ModifiableValueMap.class).put(JcrProperties.BASIC_VALUE, basicValue);
        return resource;
    }
}
