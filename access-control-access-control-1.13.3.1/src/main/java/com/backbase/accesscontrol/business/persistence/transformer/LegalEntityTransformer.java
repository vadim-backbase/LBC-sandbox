package com.backbase.accesscontrol.business.persistence.transformer;


import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Transform {@link LegalEntity} to object that extends {@link LegalEntityBase}.
 */
@Service
public class LegalEntityTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityTransformer.class);

    /**
     * Transform Legal Entity from domain in appropriate Legal Entity response body.
     *
     * @param clazz       - class type to transform in
     * @param legalEntity - legal entity to be transformed
     * @param <T>         - type which extends {@link LegalEntityBase}
     * @return specific type of legal entity
     */
    public <T extends LegalEntityBase> T transformLegalEntity(Class<T> clazz, LegalEntity legalEntity) {
        return transformLegalEntity(clazz, legalEntity, true);
    }

    /**
     * Transform Legal Entity from domain in appropriate Legal Entity response body.
     *
     * @param clazz            - class type to transform in
     * @param legalEntity      - legal entity to be transformed
     * @param includeAdditions - flag which indicates whether to map additions or not
     * @param <T>              - type which extends {@link LegalEntityBase}
     * @return specific type of legal entity
     */
    public <T extends LegalEntityBase> T transformLegalEntity(Class<T> clazz, LegalEntity legalEntity,
        boolean includeAdditions) {
        T t = transformLegalEntityWithoutAdditions(clazz, legalEntity);
        if (includeAdditions) {
            t.setAdditions(legalEntity.getAdditions());
        }
        return t;
    }

    /**
     * Transform legal entity.
     *
     * @param clazz         - destination class
     * @param legalEntities - list of {@link LegalEntity}
     * @param <T>           - destination type
     * @return - list of transformed legal entities.
     */
    public <T extends LegalEntityBase> List<T> transformLegalEntity(Class<T> clazz, List<LegalEntity> legalEntities) {
        return transformLegalEntity(clazz, legalEntities, true);
    }

    /**
     * Transform legal entity.
     *
     * @param clazz            - destination class
     * @param legalEntities    - list of {@link LegalEntity}
     * @param includeAdditions - flag which indicates whether to map additions or not
     * @param <T>              - destination type
     * @return - list of transformed legal entities.
     */
    public <T extends LegalEntityBase> List<T> transformLegalEntity(Class<T> clazz, List<LegalEntity> legalEntities,
        Boolean includeAdditions) {
        return legalEntities.stream()
            .map(legalEntity -> transformLegalEntity(clazz, legalEntity, includeAdditions))
            .collect(Collectors.toList());
    }

    /**
     * Transform Legal Entity from domain in appropriate Legal Entity response body.
     *
     * @param legalEntity - legal entity to be transformed
     * @return LegalEntityGetResponseBody
     */
    public LegalEntityGetResponseBody transformLegalEntityWithParent(LegalEntity legalEntity) {
        LegalEntityGetResponseBody legalEntityGetResponseBody = new LegalEntityGetResponseBody()
            .withId(legalEntity.getId())
            .withExternalId(legalEntity.getExternalId())
            .withName(legalEntity.getName())
            .withParentId(getParentId(legalEntity))
            .withIsParent(isParent(legalEntity))
            .withType(LegalEntityBase.Type.valueOf(legalEntity.getType().toString()));
        legalEntityGetResponseBody.setAdditions(legalEntity.getAdditions());
        return legalEntityGetResponseBody;
    }

    private <T extends LegalEntityBase> T transformLegalEntityWithoutAdditions(Class<T> clazz,
        LegalEntity legalEntity) {
        T t = createInstance(clazz);
        t.withId(legalEntity.getId())
            .withExternalId(legalEntity.getExternalId())
            .withName(legalEntity.getName())
            .withType(LegalEntityBase.Type.valueOf(legalEntity.getType().toString()));
        return t;
    }

    @SuppressWarnings("squid:S2139")
    private <T extends LegalEntityBase> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            LOGGER.warn("Unable to create response body", e);
            throw getInternalServerErrorException(e.getMessage());
        }

    }

    private String getParentId(LegalEntity legalEntity) {
        if (legalEntity.getParent() == null) {
            return null;
        }
        return legalEntity.getParent().getId();
    }

    private boolean isParent(LegalEntity legalEntity) {
        return !legalEntity.getChildren().isEmpty();
    }
}
