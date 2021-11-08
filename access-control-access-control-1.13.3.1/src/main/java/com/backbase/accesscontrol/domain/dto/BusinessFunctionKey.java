package com.backbase.accesscontrol.domain.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class BusinessFunctionKey {

    private String resourceName;
    private String businessFunctionName;
    private String functionId;
    private String functionCode;

}
