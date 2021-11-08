package com.backbase.accesscontrol.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
public class SearchAndPaginationParameters {

    private Integer from;
    private Integer size;
    private String query;
    private String cursor;

    /**
     * Constructor.
     *
     * @param from - from page
     * @param size - num of items in page
     * @param query - query
     * @param cursor - cursor
     */
    public SearchAndPaginationParameters(Integer from, Integer size, String query, String cursor) {
        this.from = from;
        this.size = size;
        this.cursor = cursor;
        if (query != null) {
            this.query = query.trim().toUpperCase();
        }
    }
}
