package com.icfolson.sling.slingmap.api.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.Resource;

/**
 * Defines capability to be used by ObjectReader implementations
 */
public interface ReadContext {

    interface Callback {

        void objectRead(final Object read) throws MappingException;

    }

    /**
     * Perform a recursive read, converting a resource into its Object representation.  In order to properly handle
     * reference nodes, the objects are initialized asynchronously using a breadth-first algorithm.  The provided
     * callback will be called when the requested object (and all recursively-read objects which it references) are
     * initialized.
     *
     * @param target
     * @param callback
     * @throws MappingException
     */
    void recursiveRead(final Resource target, final Callback callback) throws MappingException;

}