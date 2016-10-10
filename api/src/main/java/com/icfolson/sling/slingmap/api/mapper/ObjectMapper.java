package com.icfolson.sling.slingmap.api.mapper;

import com.icfolson.sling.slingmap.api.domain.MergeOptions;
import com.icfolson.sling.slingmap.api.domain.MergeResult;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;

/**
 * The primary interface used to map java objects to/from sling resources.
 */
public interface ObjectMapper {

    /**
     * Read a java object from a sling resource.  The type of the returned object will be the same as the type of the
     * object originally serialized.
     * @param resource
     * @return
     */
    Object readObject(final Resource resource) throws MappingException;

    /**
     * Read an object from a sling resource, manually specifying the type.
     * @param resource
     * @param type
     * @param <T>
     * @return
     */
    <T> T readObject(final Resource resource, final Class<T> type) throws MappingException;

    /**
     * Write an object to a sling resource.  This method will overwrite any data that was previously stored in the
     * resource.  In order to create a new object, pass in a {@link NonExistingResource} instance (not null)
     * @param object the object to write to the sling resource tree
     * @param target the resource indicating the path to write to (and the session to write as)
     * @throws MappingException
     */
    void writeObject(final Object object, final Resource target) throws MappingException;

    /**
     * Merge an object to a sling resource.  The target resource will be overwritten, but data will be left unchanged
     * when missing in the provided object (e.g., null values).  The method will return {@code true} if the resource
     * was changed.
     * @param object the object to write to the sling resource tree
     * @param target the resource indicating the path to write to (and the session to write as)
     * @return {@code true} if the resource was changed
     * @throws MappingException
     */
    MergeResult mergeObject(final Object object, final Resource target) throws MappingException;

    /**
     * Merge an object to a sling resource.  The target resource will be overwritten, but data will be left unchanged
     * when missing in the provided object (e.g., null values).  The method will return {@code true} if the resource
     * was changed.
     * @param object the object to write to the sling resource tree
     * @param target the resource indicating the path to write to (and the session to write as)
     * @param options
     * @return {@code true} if the resource was changed
     * @throws MappingException
     */
    MergeResult mergeObject(final Object object, final Resource target, final MergeOptions options)
        throws MappingException;

}
