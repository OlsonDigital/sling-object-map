package com.icfolson.sling.slingmap.runtime.domain;

import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import org.apache.sling.api.resource.Resource;

public interface ItemReader {

    void read(final Resource resource, final Object instance, final ReadContext context) throws MappingException;

}
