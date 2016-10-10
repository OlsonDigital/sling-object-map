package com.icfolson.sling.slingmap.runtime.registry.types.collection;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectWriter;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Collection;

public final class CollectionWriter extends ClassObjectWriter {

    public static final CollectionWriter INSTANCE = new CollectionWriter();

    private CollectionWriter() { }

    @Override
    protected Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException {

        if (!(object instanceof Collection)) {
            throw new IllegalArgumentException("This writer is not valid for type " + object.getClass().getName());
        }
        final Collection collection = (Collection) object;

        final ResourceResolver resolver = resource.getResourceResolver();

        // Remove existing
        for (final Resource child : resource.getChildren()) {
            try {
                resolver.delete(child);
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }

        // Write current
        int index = 0;
        for (final Object o: collection) {
            final String name = Integer.toString(index++);
            final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, name);
            writeContext.queueWrite(o, child);
        }

        return resource;
    }

}
