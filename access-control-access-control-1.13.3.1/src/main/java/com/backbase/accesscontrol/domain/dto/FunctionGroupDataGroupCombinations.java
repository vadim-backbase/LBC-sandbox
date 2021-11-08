package com.backbase.accesscontrol.domain.dto;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class FunctionGroupDataGroupCombinations {

    @Include
    private Long fgDgCombinationId;
    private Map<String, Set<String>> dgTypeIds;
}
