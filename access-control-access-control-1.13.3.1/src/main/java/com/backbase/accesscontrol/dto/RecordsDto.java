package com.backbase.accesscontrol.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecordsDto<T> {

    private Long totalNumberOfRecords;
    private List<T> records;
}
