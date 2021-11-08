package com.backbase.accesscontrol.dto.parameterholder;

import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@With
@EqualsAndHashCode
public class GetLegalEntitySegmentationHolder implements GenericParameterHolder {

    private String businessFunction;
    private String privilege;
    private String serviceAgreementId;
    private String userId;
    private String legalEntityId;
    private SearchAndPaginationParameters searchAndPaginationParameters;
}
