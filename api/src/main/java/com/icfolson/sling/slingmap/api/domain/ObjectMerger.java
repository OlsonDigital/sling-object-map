package com.icfolson.sling.slingmap.api.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.Resource;

/**
 * Defines a function used to merge a Java object into a Sling resource representation of that object.
 */
public interface ObjectMerger {

    /**
     * Merge an object with an existing resource representation of that object.  Note that, unlike
     * {@link ObjectWriter#write(Object, Resource, WriteContext)}, the resource passed into this method will never be a
     * NonExistingResource
     * @param object
     * @param resource
     * @param mergeContext
     * @throws MappingException
     */
    void merge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException;

}
