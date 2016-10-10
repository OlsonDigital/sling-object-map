package com.icfolson.sling.slingmap.runtime.registry.types.reference;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

public class ReferenceObjectMerger implements ObjectMerger {

    private final Reference reference;

    public ReferenceObjectMerger(final Reference reference) {
        this.reference = reference;
    }

    @Override
    public void merge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {

        final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        valueMap.put(JcrProperties.REFERENCE_PATH, reference.getReferencePath());

        // If the referenced item is included, write to target resource
        if (reference.getReferenced() != null) {
            final Resource target = resource.getResourceResolver().resolve(reference.getReferencePath());
            mergeContext.queueMerge(reference.getReferenced(), target);
        }
    }
}
