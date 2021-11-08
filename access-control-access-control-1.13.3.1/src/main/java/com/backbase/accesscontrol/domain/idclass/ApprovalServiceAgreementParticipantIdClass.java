package com.backbase.accesscontrol.domain.idclass;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApprovalServiceAgreementParticipantIdClass implements Serializable {

    private Long approvalServiceAgreement;
    private String legalEntityId;
}
