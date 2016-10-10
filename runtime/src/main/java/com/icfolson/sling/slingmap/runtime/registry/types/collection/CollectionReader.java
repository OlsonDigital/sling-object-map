package com.icfolson.sling.slingmap.runtime.registry.types.collection;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class CollectionReader extends ClassObjectReader {

    public static final CollectionReader INSTANCE = new CollectionReader();

    private static final Logger LOG = LoggerFactory.getLogger(CollectionReader.class);

    private CollectionReader() { }

    @Override
    protected Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException {

        if (!Collection.class.isAssignableFrom(objClass)) {
            throw new IllegalArgumentException("Reader not valid for type " + objClass);
        }
        Collection collection = null;
        try {
            collection = (Collection) objClass.newInstance();

            for (final Resource child : resource.getChildren()) {
                readContext.recursiveRead(child, collection::add);
            }

        } catch (InstantiationException e) {
            LOG.error("Error instantiating collection", e);
        } catch (IllegalAccessException e) {
            LOG.error("Unable to instantiate collection", e);
        }
        return collection;
    }
}
