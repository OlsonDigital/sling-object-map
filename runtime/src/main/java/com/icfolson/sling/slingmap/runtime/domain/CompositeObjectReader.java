package com.icfolson.sling.slingmap.runtime.domain;

import com.google.common.base.Preconditions;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CompositeObjectReader extends ClassObjectReader {

    private final Map<String, ItemReader> readers;

    public CompositeObjectReader() {
         readers = new HashMap<>();
    }

    @Override
    protected Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException {

        final Object object;
        try {
            object = objClass.newInstance();
            for (final String itemName : getItemNames()) {
                final ItemReader reader = getItemReader(itemName);
                reader.read(resource, object, readContext);
            }
            return object;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    public final List<String> getItemNames() {
        return new ArrayList<>(readers.keySet());
    }

    public final ItemReader getItemReader(final String itemName) {
        return readers.get(itemName);
    }

    public final void setItemReader(final String itemName, final ItemReader itemReader) {
        Preconditions.checkNotNull(itemReader, "Item reader cannot be null");
        readers.put(itemName, itemReader);
    }

    public final void removeItemReader(final String itemName) {
        readers.remove(itemName);
    }
}
