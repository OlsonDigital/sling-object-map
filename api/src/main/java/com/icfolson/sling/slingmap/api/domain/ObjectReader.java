package com.icfolson.sling.slingmap.api.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.Resource;

/**
 * Defines a function used to read a Java object from a Sling resource representation
 */
public interface ObjectReader {

    Object read(final Resource resource, final ReadContext readContext, final Class<?> modelType) throws MappingException;

    Object read(final Resource resource, final ReadContext readContext) throws MappingException;

}
