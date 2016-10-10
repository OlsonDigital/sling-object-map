package com.icfolson.sling.slingmap.runtime.registry.types.basic;

import com.google.common.base.Objects;
import com.icfolson.sling.slingmap.api.basictype.BasicTypeWriter;
import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectMerger;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

/**
 * Merges a basic value as a resource (rather than a property, which might be needed when the object is a part of a
 * collection, or the value is written directly to the Sling resource tree).
 */
public class BasicValueObjectMerger extends ClassObjectMerger {

    private final BasicTypeWriter writer;

    public BasicValueObjectMerger(final BasicTypeWriter writer) {
        this.writer = writer;
    }

    @Override
    protected void doMerge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {

        final Object oldValue = resource.getValueMap().get(JcrProperties.BASIC_VALUE);
        final Object newValue = writer.objectToResourcePropertyValue(object);
        if (!Objects.equal(oldValue, newValue)) {
            final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            valueMap.put(JcrProperties.BASIC_VALUE, newValue);
            mergeContext.changeWritten(resource, null, ChangeType.UPDATE);
        }
    }
}
