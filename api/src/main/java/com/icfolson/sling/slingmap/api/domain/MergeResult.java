package com.icfolson.sling.slingmap.api.domain;

import java.util.List;

public interface MergeResult {

    interface MergeChange {

        String getPath();

        ChangeType getType();

    }

    List<MergeChange> getChanges();

}
