package com.icfolson.sling.slingmap.runtime.registry.types.classes;

import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.runtime.util.ClassUtil;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.Resource;

import java.util.Objects;

/**
 * An abstract object merger that writes class info to the resource for subsequent use by readers
 * (see {@link ClassObjectReader})
 */
public abstract class ClassObjectMerger implements ObjectMerger {

    @Override
    public void merge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {
        final Class<?> existing = ClassUtil.tryLoadClassForSerializedResource(resource);
        final Class<?> updated = object.getClass();
        if (!Objects.equals(existing, updated)) {
            // Writing an object of a different class -- don't attempt merge, just do complete rewrite
            mergeContext.changeWritten(resource, null, ChangeType.UPDATE);
            final Resource reinitialized = MappedResourceUtil.reinitialize(resource);
            ClassUtil.writeClassDataToResource(updated, reinitialized);
            doMerge(object, reinitialized, mergeContext);
        } else {
            doMerge(object, resource, mergeContext);
        }
    }

    protected abstract void doMerge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException;
}
