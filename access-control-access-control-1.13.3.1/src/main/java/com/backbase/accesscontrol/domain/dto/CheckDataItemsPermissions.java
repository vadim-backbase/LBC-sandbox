package com.backbase.accesscontrol.domain.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class CheckDataItemsPermissions {

    private Long combinationId;
    private Long countTypesInCombination;
    private Long checkSumTypesAndItems;
}
