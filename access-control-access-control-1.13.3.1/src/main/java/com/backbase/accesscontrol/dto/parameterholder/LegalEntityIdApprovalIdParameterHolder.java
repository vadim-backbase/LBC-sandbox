package com.backbase.accesscontrol.dto.parameterholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@NoArgsConstructor
@AllArgsConstructor
@With
@Getter
@Setter
public class LegalEntityIdApprovalIdParameterHolder implements GenericParameterHolder {

    private String legalEntityId;
    private String approvalId;
}
