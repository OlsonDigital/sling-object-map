package com.icfolson.sling.slingmap.api.basictype;

/**
 * Defines a function that converts a Java object value to its Sling resource property equivalent.  This function may
 * simply return the input for types which are already valid resource property value types (e.g., String).
 */
public interface BasicTypeWriter {

    Object objectToResourcePropertyValue(Object primitiveValue);

}
