package com.icfolson.sling.slingmap.runtime.basictype;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeConverter;

import java.util.Calendar;
import java.util.Date;

/**
 * Converts Date properties on Java objects to an equivalent Calendar instance for storage on a (JCR-based) Sling
 * resource.
 */
public final class DateBasicTypeConverter implements BasicTypeConverter {

    public static final DateBasicTypeConverter INSTANCE = new DateBasicTypeConverter();

    private DateBasicTypeConverter() { }

    public Object objectToResourcePropertyValue(final Object primitiveValue) {
        if (primitiveValue instanceof Date) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) primitiveValue);
            return calendar;
        }
        throw new IllegalArgumentException();
    }

    public Date resourceToObjectPropertyValue(final Object resourcePropertyValue) {
        if (resourcePropertyValue instanceof Calendar) {
            Calendar calendar = (Calendar) resourcePropertyValue;
            return new Date(calendar.getTimeInMillis());
        }
        return null;
    }
}
