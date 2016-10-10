package com.icfolson.sling.slingmap.runtime.registry.types.collection;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectMerger;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Collection;
import java.util.Iterator;

public class CollectionMerger extends ClassObjectMerger {

    public static final CollectionMerger INSTANCE = new CollectionMerger();

    private CollectionMerger() { }

    @Override
    protected void doMerge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {
        if (!(object instanceof Collection)) {
            throw new IllegalArgumentException("This merger is not valid for type " + object.getClass().getName());
        }
        final Collection collection = (Collection) object;

        final ResourceResolver resolver = resource.getResourceResolver();

        // Get, increment, and update the collection version, which acts as a namespace to avoid name collisions
        // with resources written during previous merges
        final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        Long collectionVersion = valueMap.get(JcrProperties.VERSION, 0L);
        collectionVersion++;
        valueMap.put(JcrProperties.VERSION, collectionVersion);

        // As long as both last, iterate over resources and objects and merge the pairs
        final Iterator objectIterator = collection.iterator();
        final Iterator<Resource> resourceIterator = resource.listChildren();
        int index = 0;
        while (resourceIterator.hasNext() && objectIterator.hasNext()) {
            index++;
            final Object itemObject = objectIterator.next();
            final Resource itemResource = resourceIterator.next();
            mergeContext.queueMerge(itemObject, itemResource);
        }

        // Delete any remaining resources
        while (resourceIterator.hasNext()) {
            try {
                final Resource child = resourceIterator.next();
                mergeContext.changeWritten(child, null, ChangeType.DELETE);
                resolver.delete(child);
            } catch (PersistenceException e) {
                throw new MappingException(e);
            }
        }

        // Write any remaining objects
        while (objectIterator.hasNext()) {
            final Object item = objectIterator.next();
            String prefix = Long.toString(collectionVersion) + "-";
            final String name = prefix + Integer.toString(index++);
            final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, name);
            mergeContext.queueMerge(item, child);
        }
    }
}
