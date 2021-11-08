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
public class ServiceAgreementIdApprovalIdParameterHolder implements GenericParameterHolder {

    private String serviceAgreementId;
    private String approvalId;
}
