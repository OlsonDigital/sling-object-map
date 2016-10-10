package com.icfolson.sling.slingmap.runtime.registry.types.map;

import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectReader;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MapObjectReader extends ClassObjectReader {

    private static final Logger LOG = LoggerFactory.getLogger(MapObjectReader.class);

    @Override
    protected Object doRead(final Class<?> objClass, final Resource resource, final ReadContext readContext)
        throws MappingException {

        if (!(Map.class.isAssignableFrom(objClass))) {
            throw new IllegalArgumentException("Writer is not valid for objects of type: " + objClass);
        }

        Map map = null;
        try {
            map = (Map) objClass.newInstance();

            for (final Resource child : resource.getChildren()) {
                final MapCallback mapCallback = new MapCallback(map);
                readContext.recursiveRead(child, mapCallback);
            }

        } catch (InstantiationException e) {
            LOG.error("Error instantiating map", e);
        } catch (IllegalAccessException e) {
            LOG.error("Unable to instantiate map", e);
        }
        return map;
    }

    private static class MapCallback implements ReadContext.Callback {

        private final Map map;

        private MapCallback(final Map map) {
            this.map = map;
        }

        @Override
        public void objectRead(final Object read) throws MappingException {
            if (!(read instanceof MapEntry)) {
                throw new MappingException("Invalid entry type found in map: " + read.getClass());
            }
            final MapEntry entry = (MapEntry) read;
            map.put(entry.getKey(), entry.getValue());
        }
    }
}
