package com.icfolson.sling.slingmap.api.basictype;

/**
 * Defines a function that converts a Sling resource property value to its Java object equivalent.  This function may
 * simply return the input for types which are already valid resource property value types (e.g., String).
 */
public interface BasicTypeReader {

    Object resourceToObjectPropertyValue(Object resourcePropertyValue);

}
