package com.icfolson.sling.slingmap.runtime.registry.types.map;

import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectMerger;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapObjectMerger extends ClassObjectMerger {

    private final KeyMapperRegistry keyMapperRegistry;

    public MapObjectMerger(final KeyMapperRegistry keyMapperRegistry) {
        this.keyMapperRegistry = keyMapperRegistry;
    }

    @Override
    protected void doMerge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {

        if (!(object instanceof Map)) {
            throw new IllegalArgumentException("Writer is not valid for objects of type: " + object.getClass());
        }

        final Map map = (Map) object;
        final Set<String> foundKeys = new HashSet<>();
        final Set<Map.Entry> entries = map.entrySet();
        for (final Map.Entry entry: entries) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            final KeyMapper mapper = keyMapperRegistry.getKeyMapper(key.getClass());
            if (mapper == null) {
                throw new MappingException("Mapper not found for keys of type " + key.getClass());
            }
            final String path = mapper.getRelativePathForKey(key);
            foundKeys.add(path);
            final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, path);
            final MapEntry e = new MapEntry();
            e.setKey(key);
            e.setValue(value);
            mergeContext.queueMerge(e, child);
        }

        final ResourceResolver resolver = resource.getResourceResolver();
        for (final Resource child : resource.getChildren()) {
            if (!foundKeys.contains(child.getName())) {
                mergeContext.changeWritten(child, null, ChangeType.DELETE);
                try {
                    resolver.delete(child);
                } catch (PersistenceException e) {
                    throw new MappingException(e);
                }
            }
        }
    }
}
