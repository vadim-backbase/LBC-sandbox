
package com.backbase.accesscontrol.dto;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAgreementParticipantDto {

    private String id;
    private Boolean sharingUsers;
    private Boolean sharingAccounts;
    private Set<String> admins = new LinkedHashSet<>();

    public ServiceAgreementParticipantDto withId(String id) {
        this.id = id;
        return this;
    }

    public ServiceAgreementParticipantDto withSharingUsers(Boolean sharingUsers) {
        this.sharingUsers = sharingUsers;
        return this;
    }

    public ServiceAgreementParticipantDto withSharingAccounts(Boolean sharingAccounts) {
        this.sharingAccounts = sharingAccounts;
        return this;
    }

    public ServiceAgreementParticipantDto withAdmins(Set<String> admins) {
        this.admins = admins;
        return this;
    }

}
