package com.icfolson.sling.slingmap.runtime.registry.types.classes;

import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.base.BaseObjectWriter;
import com.icfolson.sling.slingmap.runtime.util.ClassUtil;
import org.apache.sling.api.resource.Resource;

/**
 * An abstract object writer that writes class info to the resource for subsequent use by readers
 * (see {@link ClassObjectReader})
 */
public abstract class ClassObjectWriter extends BaseObjectWriter {

    @Override
    protected final Resource writeItems(final Object object, final Resource resource, final WriteContext context)
        throws MappingException {

        ClassUtil.writeClassDataToResource(object.getClass(), resource);
        return doWrite(object, resource, context);
    }

    protected abstract Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException;
}
