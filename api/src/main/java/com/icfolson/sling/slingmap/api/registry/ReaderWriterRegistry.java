package com.icfolson.sling.slingmap.api.registry;

import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;

/**
 * Interface defining a registry for object reader, writer and merger functions based on class.  In addition to explicit
 * registration, implementations may provide implicit mapping functions for certain types.
 */
public interface ReaderWriterRegistry {

    void registerReader(final Class<?> outputClass, final ObjectReader reader);

    void registerWriter(final Class<?> inputClass, final ObjectWriter writer);

    void registerMerger(final Class<?> inputClass, final ObjectMerger writer);

    ObjectReader getReader(final Class<?> className);

    ObjectWriter getWriter(final Class<?> className);

    ObjectMerger getMerger(final Class<?> className);

}
