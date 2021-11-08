package com.backbase.accesscontrol.dto;

import lombok.Data;

@Data
public class PersistenceExtendedParticipant {

    private String externalServiceAgreementId;
    private String id;
    private String externalId;
    private String name;
    private boolean sharingUsers;
    private boolean sharingAccounts;
}
