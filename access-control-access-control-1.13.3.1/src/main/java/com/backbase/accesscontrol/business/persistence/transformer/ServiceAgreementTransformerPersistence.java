package com.backbase.accesscontrol.business.persistence.transformer;


import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Transform {@link ServiceAgreement} to object that extends {@link ServiceAgreementBase}.
 */
@Service
public class ServiceAgreementTransformerPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementTransformerPersistence.class);

    /**
     * Transform Service Agreement from domain in appropriate Service Agreement response body.
     *
     * @param clazz            - class type to transform in
     * @param serviceAgreement - service agreement to be transformed
     * @param <T>              - type which extends {@link ServiceAgreementBase}
     * @return specific type of service agreement
     */
    public <T extends ServiceAgreementBase> T transformServiceAgreement(Class<T> clazz,
        ServiceAgreement serviceAgreement) {
        LOGGER.info("Transforming service agreement");
        T t = createInstance(clazz);
        t.withId(serviceAgreement.getId())
            .withExternalId(serviceAgreement.getExternalId())
            .withDescription(serviceAgreement.getDescription())
            .withIsMaster(serviceAgreement.isMaster())
            .withValidFrom(serviceAgreement.getStartDate())
            .withValidUntil(serviceAgreement.getEndDate())
            .withName(serviceAgreement.getName());
        return t;
    }

    @SuppressWarnings("squid:S2139")
    private <T extends ServiceAgreementBase> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            LOGGER.warn("Unable to create response body", e);
            throw getInternalServerErrorException(e.getMessage());
        }
    }

}
