package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;

import java.util.Objects;

public class ServiceAgreementUtil {

    public static ServiceAgreement createServiceAgreement(String name,
        String externalId,
        String description,
        LegalEntity legalEntity,
        String consumerId, String providerId) {

        //fix for oracle null exception for empty string
        if (Objects.isNull(description) || description.isEmpty()) {
            description = "-";
        }
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName(name);
        serviceAgreement.setExternalId(externalId);
        serviceAgreement.setDescription(description);
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        if (providerId != null && providerId.equals(consumerId)) {
            serviceAgreement.addParticipant(new Participant(), consumerId, true, true);
        } else {
            if (consumerId != null) {
                serviceAgreement.addParticipant(new Participant(), consumerId, false, true);
            }
            if (providerId != null) {
                serviceAgreement.addParticipant(new Participant(), providerId, true, false);
            }
        }
        return serviceAgreement;
    }

    public static Participant createParticipantWithAdmin(String providerAdminId, boolean shareUsers,
        boolean shareAccounts) {
        Participant provider = new Participant();
        provider.addAdmin(providerAdminId);
        provider.setShareUsers(shareUsers);
        provider.setShareAccounts(shareAccounts);
        return provider;
    }

    public static Participant createParticipant(boolean shareUsers, boolean shareAccounts, LegalEntity legalEntity) {
        Participant participant = new Participant();
        participant.setShareUsers(shareUsers);
        participant.setShareAccounts(shareAccounts);
        participant.setLegalEntity(legalEntity);
        return participant;
    }
}
