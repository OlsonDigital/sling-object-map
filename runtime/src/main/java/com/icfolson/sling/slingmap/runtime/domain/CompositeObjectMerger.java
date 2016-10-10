package com.icfolson.sling.slingmap.runtime.domain;

import com.icfolson.sling.slingmap.api.constants.Namespaces;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompositeObjectMerger implements ObjectMerger {

    private final Map<String, ItemMerger> mergers = new HashMap<>();

    @Override
    public void merge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {

        final List<String> itemNames = getItemNames();
        for (final String itemName : itemNames) {
            final ItemMerger itemMerger = getItemMerger(itemName);
            itemMerger.merge(object, resource, mergeContext);
        }

        if (mergeContext.getOptions().isDeleteProperties()) {
            Set<String> removedProperties = new HashSet<>(resource.getValueMap().keySet());
            removedProperties.removeAll(itemNames);
            if (!removedProperties.isEmpty()) {
                final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
                for (final String removedProperty : removedProperties) {
                    if (!removedProperty.startsWith(Namespaces.MAP + ":")) {
                        valueMap.remove(removedProperty);
                        mergeContext.changeWritten(resource, removedProperty, ChangeType.DELETE);
                    }
                }
            }
        }

        if (mergeContext.getOptions().isDeleteChildren()) {
            final ResourceResolver resolver = resource.getResourceResolver();
            for (final Resource child : resource.getChildren()) {
                if (!itemNames.contains(child.getName())) {
                    try {
                        mergeContext.changeWritten(child, null, ChangeType.DELETE);
                        resolver.delete(child);
                    } catch (PersistenceException e) {
                        throw new MappingException(e);
                    }
                }
            }
        }
    }

    public final List<String> getItemNames() {
        return new ArrayList<>(mergers.keySet());
    }

    public final ItemMerger getItemMerger(final String itemName) {
        return mergers.get(itemName);
    }

    public final void setItemMerger(final String itemName, final ItemMerger itemMerger) {
        if (itemMerger == null) {
            throw new NullPointerException("Item merger cannot be null");
        }
        mergers.put(itemName, itemMerger);
    }

    public final void removeItemMerger(final String itemName) {
        mergers.remove(itemName);
    }
}
