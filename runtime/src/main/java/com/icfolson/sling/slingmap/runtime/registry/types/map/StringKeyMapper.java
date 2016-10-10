package com.icfolson.sling.slingmap.runtime.registry.types.map;

import java.util.HashMap;
import java.util.Map;

public class StringKeyMapper implements KeyMapper {

    private static final String PREFIX = "entry ";

    public static final StringKeyMapper INSTANCE = new StringKeyMapper();

    private StringKeyMapper() { }

    private static final Map<String, String> REPLACEMENTS = new HashMap<>();
    static {
        REPLACEMENTS.put("\\*", "");
        REPLACEMENTS.put("/", "");
        REPLACEMENTS.put(":", "");
        REPLACEMENTS.put("\\[", "");
        REPLACEMENTS.put("\\]", "");
        REPLACEMENTS.put("\\|", "");
    }

    @Override
    public String getRelativePathForKey(final Object mapKey) {
        if (!(mapKey instanceof String)) {
            throw new IllegalArgumentException("Mapper not valid for keys of type: " + mapKey.getClass());
        }
        String replaced = (String) mapKey;
        for (final Map.Entry<String, String> e : REPLACEMENTS.entrySet()) {
            replaced = replaced.replaceAll("e.getKey()", e.getValue());
        }
        return PREFIX + replaced;
    }
}
