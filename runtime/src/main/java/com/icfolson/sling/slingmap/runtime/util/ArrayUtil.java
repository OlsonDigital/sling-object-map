package com.icfolson.sling.slingmap.runtime.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;

public final class ArrayUtil {

    public static Object[] castToObjectArray(final Object array) {
        Preconditions.checkNotNull(array);
        final Class<?> type = array.getClass();
        if (!type.isArray()) {
            throw new IllegalArgumentException("Attempted to cast non-array type: " + type);
        }
        final Class<?> componentType = type.getComponentType();
        final Object[] objArray;
        if (componentType.isPrimitive()) {
            if (array instanceof boolean[]) {
                objArray = ArrayUtils.toObject((boolean[]) array);
            } else if (array instanceof byte[]) {
                objArray = ArrayUtils.toObject((byte[]) array);
            } else if (array instanceof char[]) {
                objArray = ArrayUtils.toObject((char[]) array);
            } else if (array instanceof short[]) {
                objArray = ArrayUtils.toObject((short[]) array);
            } else if (array instanceof int[]) {
                objArray = ArrayUtils.toObject((int[]) array);
            } else if (array instanceof long[]) {
                objArray = ArrayUtils.toObject((long[]) array);
            } else if (array instanceof float[]) {
                objArray = ArrayUtils.toObject((float[]) array);
            } else if (array instanceof double[]) {
                objArray = ArrayUtils.toObject((double[]) array);
            } else {
                throw new IllegalStateException("Unknown primitive type " + componentType);
            }
        } else {
            objArray = (Object[]) array;
        }
        return objArray;
    }

    private ArrayUtil() { }
}
