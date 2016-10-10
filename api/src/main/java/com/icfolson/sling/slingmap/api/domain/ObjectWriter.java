package com.icfolson.sling.slingmap.api.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.Resource;

/**
 * Defines a function used to write a java object to a Sling resource.
 */
public interface ObjectWriter {

    /**
     * Write a Java object to a Sling resource.  If the resource exists, it should be overwritten.  If not, a
     * NonExistingResource will be passed-in (never null).  A null object should result in the deletion of the resource.
     * @param object
     * @param resource
     * @param writeContext
     * @return
     * @throws MappingException
     */
    Resource write(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException;

}
