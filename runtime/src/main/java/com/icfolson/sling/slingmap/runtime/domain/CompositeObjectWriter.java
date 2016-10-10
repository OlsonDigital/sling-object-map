package com.icfolson.sling.slingmap.runtime.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectWriter;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CompositeObjectWriter extends ClassObjectWriter {

    private final Map<String, ItemWriter> writers = new HashMap<>();

    @Override
    protected Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException {

        for (final String itemName : getItemNames()) {
            final ItemWriter itemWriter = getItemWriter(itemName);
            itemWriter.write(object, resource, writeContext);
        }
        return resource;
    }

    public final List<String> getItemNames() {
        return new ArrayList<>(writers.keySet());
    }

    public final ItemWriter getItemWriter(final String itemName) {
        return writers.get(itemName);
    }

    public final void setItemWriter(final String itemName, final ItemWriter itemWriter) {
        if (itemWriter == null) {
            throw new NullPointerException("Item writer cannot be null");
        }
        writers.put(itemName, itemWriter);
    }

    public final void removeItemWriter(final String itemName) {
        writers.remove(itemName);
    }
}
