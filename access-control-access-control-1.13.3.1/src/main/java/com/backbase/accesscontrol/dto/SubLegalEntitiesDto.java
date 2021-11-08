package com.backbase.accesscontrol.dto;

import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubLegalEntitiesDto {

    private Long totalNumberOfRecords;
    private List<SubEntitiesGetResponseBody> records;

}
