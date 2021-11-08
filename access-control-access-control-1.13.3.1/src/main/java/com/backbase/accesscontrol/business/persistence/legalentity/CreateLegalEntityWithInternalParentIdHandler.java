package com.backbase.accesscontrol.business.persistence.legalentity;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateLegalEntityWithInternalParentIdHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, PresentationCreateLegalEntityItemPostRequestBody,
        LegalEntitiesPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntityWithInternalParentIdHandler.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;

    public CreateLegalEntityWithInternalParentIdHandler(
        EventBus eventBus, PersistenceLegalEntityService persistenceLegalEntityService) {
        super(eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntitiesPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        PresentationCreateLegalEntityItemPostRequestBody request) {
        LOGGER.info("Executing request for creating legal entity {}", parameterHolder);
        LegalEntity legalEntity = persistenceLegalEntityService.createLegalEntityWithInternalParentId(request);
        LegalEntitiesPostResponseBody legalEntitiesPostResponseBody =
            new LegalEntitiesPostResponseBody();
        legalEntitiesPostResponseBody.setId(legalEntity.getId());
        legalEntitiesPostResponseBody.setAdditions(request.getAdditions());
        return legalEntitiesPostResponseBody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        PresentationCreateLegalEntityItemPostRequestBody request,
        LegalEntitiesPostResponseBody response) {
        return new LegalEntityEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        PresentationCreateLegalEntityItemPostRequestBody request, Exception failure) {
        return null;
    }
}
