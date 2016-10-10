package com.icfolson.sling.slingmap.api.domain;

public class DefaultMergeOptions implements MergeOptions {

    private boolean deleteProperties = true;
    private boolean deleteChildren = true;

    @Override
    public boolean isDeleteProperties() {
        return deleteProperties;
    }

    @Override
    public boolean isDeleteChildren() {
        return deleteChildren;
    }

    public void setDeleteProperties(final boolean deleteProperties) {
        this.deleteProperties = deleteProperties;
    }

    public void setDeleteChildren(final boolean deleteChildren) {
        this.deleteChildren = deleteChildren;
    }
}
