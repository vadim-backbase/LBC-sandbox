package com.backbase.accesscontrol.dto.parameterholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
public class UserPermissionsApprovalParameterHolder implements GenericParameterHolder {

    private String userId;
    private String serviceAgreementId;
    private String legalEntityId;
    private String approvalId;
}
