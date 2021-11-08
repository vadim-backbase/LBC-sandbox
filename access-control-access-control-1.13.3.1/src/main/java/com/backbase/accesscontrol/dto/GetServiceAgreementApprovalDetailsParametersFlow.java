package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetServiceAgreementApprovalDetailsParametersFlow {

    private String approvalId;
    private String serviceAgreementId;
    private String userId;
}
