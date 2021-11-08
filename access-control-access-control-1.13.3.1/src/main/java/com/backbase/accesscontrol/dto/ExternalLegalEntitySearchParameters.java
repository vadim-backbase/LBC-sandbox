package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ExternalLegalEntitySearchParameters {

    private String field;
    private String term;
    private Integer from;
    private String cursor;
    private Integer size;
}
