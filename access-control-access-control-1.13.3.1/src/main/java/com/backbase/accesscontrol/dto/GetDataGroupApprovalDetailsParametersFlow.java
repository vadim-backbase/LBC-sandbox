package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Data
@With
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GetDataGroupApprovalDetailsParametersFlow {

    private String approvalId;
    private String serviceAgreementId;
    private String userId;

}
