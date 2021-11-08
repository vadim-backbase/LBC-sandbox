package com.backbase.accesscontrol.util;

import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ServiceAgreementsUtils {

    private static final int NUMBER_OF_PARTICIPANTS_IN_MASTER_SA = 1;

    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;

    public List<PersistenceServiceAgreement> transformToPersistenceServiceAgreements(
        List<ServiceAgreement> serviceAgreements) {
        return serviceAgreements.stream()
            .map(serviceAgreement -> {
                PersistenceServiceAgreement responseBody = serviceAgreementTransformerPersistence
                    .transformServiceAgreement(PersistenceServiceAgreement.class, serviceAgreement);
                responseBody.setCreatorLegalEntity(serviceAgreement.getCreatorLegalEntity().getId());
                responseBody.setCreatorLegalEntityName(serviceAgreement.getCreatorLegalEntity().getName());
                responseBody.setStatus(Status.fromValue(serviceAgreement.getState().toString()));
                responseBody.setNumberOfParticipants(BigDecimal.valueOf(getNumberOfParticipants(serviceAgreement)));
                responseBody.setAdditions(serviceAgreement.getAdditions());
                responseBody.setValidFrom(serviceAgreement.getStartDate());
                responseBody.setValidUntil(serviceAgreement.getEndDate());
                return responseBody;
            })
            .collect(toList());
    }

    private int getNumberOfParticipants(ServiceAgreement serviceAgreement) {
        int numberOfParticipants = NUMBER_OF_PARTICIPANTS_IN_MASTER_SA;
        if (!serviceAgreement.isMaster()) {
            numberOfParticipants = serviceAgreement.getParticipants().size();
        }
        return numberOfParticipants;
    }
}
