package com.backbase.legalentity.integration.external.mock.util;

import java.util.List;

public class PaginationDto<E> {

    private Long totalNumberOfRecords;
    private List<E> records;

    public PaginationDto(long totalNumberOfRecords, List<E> records) {
        this.totalNumberOfRecords = totalNumberOfRecords;
        this.records = records;
    }

    public Long getTotalNumberOfRecords() {
        return totalNumberOfRecords;
    }

    public List<E> getRecords() {
        return records;
    }

}
