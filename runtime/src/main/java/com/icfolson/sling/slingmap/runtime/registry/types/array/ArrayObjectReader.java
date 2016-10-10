package com.icfolson.sling.slingmap.runtime.registry.types.array;

import com.google.common.collect.Iterables;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayObjectReader extends ClassObjectReader {

    public static final ArrayObjectReader INSTANCE = new ArrayObjectReader();

    private ArrayObjectReader() { }

    @Override
    protected Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException {

        if (!objClass.isArray()) {
            throw new IllegalArgumentException("This reader is not valid for type " + objClass.getName());
        }

        final Class<?> componentType = objClass.getComponentType();
        final List<Resource> children = new ArrayList<>();
        Iterables.addAll(children, resource.getChildren());
        final Object array = Array.newInstance(componentType, children.size());
        for (int i = 0; i < children.size(); i++) {
            final int index = i;
            readContext.recursiveRead(children.get(index), child -> Array.set(array, index, child));
        }

        return array;
    }
}
