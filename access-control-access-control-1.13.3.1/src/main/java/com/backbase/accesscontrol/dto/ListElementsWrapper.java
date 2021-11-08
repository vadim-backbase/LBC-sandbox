package com.backbase.accesscontrol.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class ListElementsWrapper<T> {

    private List<T> records;
    private Long totalNumberOfRecords;
}
