package com.icfolson.sling.slingmap.api.generator;

import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;

/**
 * Defines an interface used to generate functions to read, write, and merge objects to/from a sling resource tree.
 */
public interface MappingGenerator {

    ObjectReader generateObjectReader(final Class<?> outputClass);

    ObjectWriter generateObjectWriter(final Class<?> inputClass);

    ObjectMerger generateObjectMerger(final Class<?> inputClass);

}
