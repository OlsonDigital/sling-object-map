package com.icfolson.sling.slingmap.api.domain;

import org.apache.sling.api.resource.Resource;

/**
 * Provides functionality to {@link ObjectMerger} implementations during merges.
 */
public interface MergeContext {

    /**
     * Queues a merge.  Clients should use this method when merging a complex object graph to a tree of Sling resources.
     * Passing a null object will result in the resource.  The passed-in resource cannot null resolve a
     * NonExistingResource instead.
     * @param object
     * @param resource
     */
    void queueMerge(final Object object, final Resource resource);

    /**
     * Used to register an added/deleted/updated property or resource.  When an entire resource is added or deleted,
     * use a null propertyName argument.
     * @param resource
     * @param propertyName
     * @param changeType
     */
    void changeWritten(final Resource resource, final String propertyName, final ChangeType changeType);

    /**
     * Get the options controlling how this merge is to be performed
     * @return
     */
    MergeOptions getOptions();

}
