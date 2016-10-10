package com.icfolson.sling.slingmap.api.domain;

import java.util.Objects;

public enum ChangeType {

    ADD,
    UPDATE,
    DELETE;

    public static ChangeType fromVersions(final Object old, final Object updated) {
        if (Objects.deepEquals(old, updated)) {
            return null;
        } else if (old == null) {
            return ADD;
        } else if (updated == null) {
            return DELETE;
        } else {
            return UPDATE;
        }
    }

}
