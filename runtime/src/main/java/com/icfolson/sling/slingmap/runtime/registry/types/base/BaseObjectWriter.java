package com.icfolson.sling.slingmap.runtime.registry.types.base;

import com.icfolson.sling.slingmap.api.domain.ObjectWriter;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;


public abstract class BaseObjectWriter implements ObjectWriter {

    public final Resource write(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException {

        final Resource intermediate;
        if (ResourceUtil.isNonExistingResource(resource)) {
            intermediate = create(object, resource);
        } else {
            intermediate = update(object, resource);
        }
        return writeItems(object, intermediate, writeContext);
    }

    protected abstract Resource writeItems(final Object object, final Resource resource, final WriteContext context)
        throws MappingException;

    private static Resource create(final Object object, final Resource resource) throws MappingException {
        return MappedResourceUtil.initialize(resource);
    }

    private static Resource update(final Object object, final Resource resource) throws MappingException {
        return MappedResourceUtil.reinitialize(resource);
    }

}
