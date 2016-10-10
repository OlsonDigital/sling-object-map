package com.icfolson.sling.slingmap.runtime.registry.types.basic;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeReader;
import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;

/**
 * Reads a basic value when stored as a node (which might occur when the object is a part of a collection, or the value
 * is written directly to the Sling resource tree).
 */
public class BasicValueObjectReader extends ClassObjectReader {

    private final BasicTypeReader reader;

    public BasicValueObjectReader(final BasicTypeReader reader) {
        this.reader = reader;
    }

    @Override
    protected Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException {
        return reader.resourceToObjectPropertyValue(resource.getValueMap().get(JcrProperties.BASIC_VALUE));
    }
}
