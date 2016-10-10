package com.icfolson.sling.slingmap.runtime.registry.types.map;

/**
 * Maps an Object to the relative path at which the entry will be stored.  The returned string should be a valid sling
 * resource name, and the algorithm must ensure that if {@code}keyA.equals(keyB){@code} then
 * {@code}relativePathA.equals(relativePathB){@code}, and if {@code}!keyA.equals(keyB){@code} then
 * {@code}!relativePathA.equals(relativePathB){@code}.
 */
public interface KeyMapper {

    String getRelativePathForKey(final Object mapKey);

}
