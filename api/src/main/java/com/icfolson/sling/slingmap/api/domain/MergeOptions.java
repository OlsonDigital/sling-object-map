package com.icfolson.sling.slingmap.api.domain;

public interface MergeOptions {

    /**
     * Indicates that null values on the Java object being merged should result in the deletion of the mapped properties
     * (Default = true).
     */
    default boolean isDeleteProperties() {
        return true;
    }

    /**
     * Indicates that null values on the Java object being merged should result in the deletion of child resources to
     * which the properties are mapped. (Default = true).
     */
    default boolean isDeleteChildren() {
        return true;
    }

}
