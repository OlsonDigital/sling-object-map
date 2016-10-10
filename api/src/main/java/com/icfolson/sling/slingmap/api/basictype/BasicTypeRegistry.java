package com.icfolson.sling.slingmap.api.basictype;

/**
 * Defines a registry for Basic Types.  Clients can use this interface to register support for new types that should
 * be serialized to resource properties, as opposed to child resources.
 */
public interface BasicTypeRegistry {

    /**
     * Register a basic type with no conversion.  The type should be valid as a Sling Resource property.
     * @param basicType
     */
    void registerBasicType(Class<?> basicType);

    /**
     * Register a basic type with conversion.  The provided converter should define a bijection for converting values
     * to/from a type valid as a Sling resource property.
     * @param basicType
     * @param converter
     */
    void registerBasicType(Class<?> basicType, BasicTypeConverter converter);

    /**
     * Checks whether the provided class is registered as a basic type
     * @param type
     * @return
     */
    boolean isBasicType(Class<?> type);

    /**
     * Retrieves the converter registered for the provided basic type.  For basic types without a registered converter,
     * a non-converting implementation will be returned.
     * @param basicType
     * @return
     */
    BasicTypeConverter getBasicConverter(Class<?> basicType);

}
