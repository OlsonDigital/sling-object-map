package com.icfolson.sling.slingmap.runtime.registry.types.classes;

import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.runtime.util.ClassUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

/**
 * An abstract reader which attempt to read class info from the target resource for use by subtypes.
 */
public abstract class ClassObjectReader implements ObjectReader {

    @Override
    public final Object read(final Resource resource, final ReadContext readContext) throws MappingException {

        if (resource == null || ResourceUtil.isNonExistingResource(resource)) {
            return null;
        }

        Class<?> objectClass = ClassUtil.tryLoadClassForSerializedResource(resource);
        if (objectClass != null) {
            return doRead(objectClass, resource, readContext);
        }

        return null;
    }

    @Override
    public final Object read(final Resource resource, final ReadContext readContext, final Class<?> modelType)
            throws MappingException {

        if (resource == null || ResourceUtil.isNonExistingResource(resource)) {
            return null;
        }

        return doRead(modelType, resource, readContext);
    }

    protected abstract Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException;

}
