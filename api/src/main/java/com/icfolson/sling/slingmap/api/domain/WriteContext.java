package com.icfolson.sling.slingmap.api.domain;

import org.apache.sling.api.resource.Resource;

/**
 * Defines capability to be used by {@link ObjectWriter} implementations
 */
public interface WriteContext {

    void queueWrite(final Object object, final Resource resource);

}