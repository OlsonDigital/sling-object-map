package com.icfolson.sling.slingmap.runtime.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import org.apache.sling.api.resource.Resource;

public interface ItemMerger {

    void merge(final Object instance, final Resource resource, final MergeContext context) throws MappingException;
}
