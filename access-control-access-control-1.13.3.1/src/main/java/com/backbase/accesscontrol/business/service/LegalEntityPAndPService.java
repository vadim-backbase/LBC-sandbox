package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.business.persistence.transformer.LegalEntityTransformer;
import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service component making calls to Access control Legal Entity query side.
 */
@Service
@AllArgsConstructor
public class LegalEntityPAndPService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityPAndPService.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;
    private LegalEntityTransformer legalEntityTransformer;


    /**
     * Returns the master service agreement for a legal entity with a given ID.
     *
     * @param legalEntityId legal entity internal (BB) ID.
     * @return master service agreement for a legal entity with a given ID.
     */
    public ServiceAgreementItem getMasterServiceAgreement(String legalEntityId) {
        LOGGER.info("Trying to get master service agreement by legal entity id");
        ServiceAgreement serviceAgreement = persistenceLegalEntityService
            .getMasterServiceAgreement(legalEntityId);
        ServiceAgreementItem masterServiceAgreementGetResponseBody = serviceAgreementTransformerPersistence
            .transformServiceAgreement(ServiceAgreementItem.class, serviceAgreement);
        masterServiceAgreementGetResponseBody.setCreatorLegalEntity(legalEntityId);
        masterServiceAgreementGetResponseBody.setAdditions(serviceAgreement.getAdditions());
        masterServiceAgreementGetResponseBody.setStatus(Status.valueOf(serviceAgreement.getState().toString()));
        return masterServiceAgreementGetResponseBody;
    }

    public LegalEntityGetResponseBody getLegalEntityByIdAsResponseBody(String id) {
        return legalEntityTransformer
            .transformLegalEntityWithParent(persistenceLegalEntityService.getLegalEntityById(id));
    }

}
