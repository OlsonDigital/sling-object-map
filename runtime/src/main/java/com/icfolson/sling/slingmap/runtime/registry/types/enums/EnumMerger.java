package com.icfolson.sling.slingmap.runtime.registry.types.enums;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.runtime.registry.types.classes.ClassObjectMerger;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

public class EnumMerger extends ClassObjectMerger {

    public static final EnumMerger INSTANCE = new EnumMerger();

    private EnumMerger() { }

    @Override
    protected void doMerge(final Object object, final Resource resource, final MergeContext mergeContext)
        throws MappingException {
        if (!(object instanceof Enum)) {
            throw new IllegalArgumentException("Merger is not valid for " + object.getClass());
        }
        Enum e = (Enum) object;
        ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        final String currentName = valueMap.get(JcrProperties.ENUM_VALUE, String.class);
        final String updatedName = e.name();
        if (!updatedName.equals(currentName)) {
            valueMap.put(JcrProperties.ENUM_VALUE, e.name());
            mergeContext.changeWritten(resource, null, ChangeType.UPDATE);
        }
    }
}
