package com.backbase.accesscontrol.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GetLegalEntitiesRequestDto {

    private String parentEntityId;
    private Set<String> excludeIds;
    private String cursor;
    private Integer from;
    private Integer size;
    private String query;
}
