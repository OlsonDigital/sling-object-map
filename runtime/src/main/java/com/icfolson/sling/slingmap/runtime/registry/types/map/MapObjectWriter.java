package com.icfolson.sling.slingmap.runtime.registry.types.map;

import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectWriter;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.Resource;

import java.util.Map;
import java.util.Set;

public class MapObjectWriter extends ClassObjectWriter {
    
    private final KeyMapperRegistry keyMapperRegistry;

    public MapObjectWriter(final KeyMapperRegistry keyMapperRegistry) {
        this.keyMapperRegistry = keyMapperRegistry;
    }

    @Override
    protected Resource doWrite(final Object object, final Resource resource, final WriteContext writeContext)
        throws MappingException {
        
        if (!(object instanceof Map)) {
            throw new IllegalArgumentException("Writer is not valid for objects of type: " + object.getClass());
        }
        
        final Map map = (Map) object;
        final Set<Map.Entry> entries = map.entrySet();
        for (final Map.Entry entry: entries) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            final KeyMapper mapper = keyMapperRegistry.getKeyMapper(key.getClass());
            if (mapper == null) {
                throw new MappingException("Mapper not found for keys of type " + key.getClass());
            }
            final String path = mapper.getRelativePathForKey(key);
            final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, path);
            final MapEntry e = new MapEntry();
            e.setKey(key);
            e.setValue(value);
            writeContext.queueWrite(e, child);
        }
        
        return resource;
    }
}
