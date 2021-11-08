package com.backbase.accesscontrol.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaginationDto<E> {

    private Long totalNumberOfRecords;
    private List<E> records;

}
