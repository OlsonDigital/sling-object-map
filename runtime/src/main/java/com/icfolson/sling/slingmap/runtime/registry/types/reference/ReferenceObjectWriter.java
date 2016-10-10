package com.icfolson.sling.slingmap.runtime.registry.types.reference;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.runtime.registry.types.base.BaseObjectWriter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

public class ReferenceObjectWriter extends BaseObjectWriter {

    private final Reference reference;

    public ReferenceObjectWriter(final Reference reference) {
        if (reference == null) {
            throw new NullPointerException("Reference cannot be null");
        }
        if (reference.getReferencePath() == null) {
            throw new NullPointerException("Reference cannot have null path");
        }
        this.reference = reference;
    }

    @Override
    protected Resource writeItems(final Object object, final Resource resource, final WriteContext context)
        throws MappingException {

        final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        valueMap.put(JcrProperties.REFERENCE_PATH, reference.getReferencePath());

        // If the referenced item is included, write to target resource
        if (reference.getReferenced() != null) {
            final Resource target = resource.getResourceResolver().resolve(reference.getReferencePath());
            context.queueWrite(reference.getReferenced(), target);
        }

        return resource;
    }
}
