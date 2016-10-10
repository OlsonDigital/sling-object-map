package com.icfolson.sling.slingmap.runtime.util;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import java.util.HashMap;
import java.util.Map;

public final class MappedResourceUtil {

    private MappedResourceUtil() { }

    public static Resource getPotentiallyNonExistingChild(final Resource parent, final String childName) {
        final String fullPath = parent.getPath() + "/" + childName;
        final ResourceResolver resolver = parent.getResourceResolver();
        return resolver.resolve(fullPath);
    }

    public static Resource initialize(final Resource nonExisting) throws MappingException {
        final ResourceResolver resolver = nonExisting.getResourceResolver();
        final String path = nonExisting.getPath();
        final Resource parent = resolver.resolve(ResourceUtil.getParent(path));
        final String name = ResourceUtil.getName(path);
        try {
            return resolver.create(parent, name, null);
        } catch (PersistenceException e) {
            throw new MappingException(e);
        }
    }

    public static Resource reinitialize(final Resource existing) throws MappingException {
        final ResourceResolver resolver = existing.getResourceResolver();
        final Resource parent = existing.getParent();
        final String path = existing.getPath();
        final String name = ResourceUtil.getName(path);
        try {
            resolver.delete(existing);
            final Resource nonExisting = getPotentiallyNonExistingChild(parent, name);
            return initialize(nonExisting);
        } catch (PersistenceException e) {
            throw new MappingException(e);
        }
    }

    public static boolean equal(final Resource a, final Resource b) {
        if (!a.getValueMap().equals(b.getValueMap())) {
            return false;
        }
        final Map<String, Resource> aChildren = mapChildren(a);
        final Map<String, Resource> bChildren = mapChildren(b);
        if (!aChildren.keySet().equals(bChildren.keySet())) {
            return false;
        }
        for (final String name : aChildren.keySet()) {
            final Resource aChild = aChildren.get(name);
            final Resource bChild = bChildren.get(name);
            if (!equal(aChild, bChild)) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Resource> mapChildren(final Resource resource) {
        Map<String, Resource> out = new HashMap<>();
        resource.listChildren().forEachRemaining(child -> out.put(child.getName(), child));
        return out;
    }
}