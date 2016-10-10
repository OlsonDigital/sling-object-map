package com.icfolson.sling.slingmap.runtime.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import org.apache.sling.api.resource.Resource;

public interface ItemWriter {

    void write(final Object object, final Resource resource, final WriteContext context) throws MappingException;

}
