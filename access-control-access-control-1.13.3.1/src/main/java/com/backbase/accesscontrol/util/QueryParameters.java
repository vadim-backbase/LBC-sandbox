package com.backbase.accesscontrol.util;

import javax.validation.constraints.Size;

public class QueryParameters {

    @Size(min = 0, max = 255)
    private String query;

    public QueryParameters withSearchQuery(String query) {
        this.query = query;
        return this;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
