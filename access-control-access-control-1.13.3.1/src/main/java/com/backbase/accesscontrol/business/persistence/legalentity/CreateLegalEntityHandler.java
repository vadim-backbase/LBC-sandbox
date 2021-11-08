package com.backbase.accesscontrol.business.persistence.legalentity;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CreateLegalEntityHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, CreateLegalEntitiesPostRequestBody, CreateLegalEntitiesPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntityHandler.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;

    public CreateLegalEntityHandler(EventBus eventBus, PersistenceLegalEntityService persistenceLegalEntityService) {
        super(eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CreateLegalEntitiesPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        CreateLegalEntitiesPostRequestBody request) {
        LOGGER.info("Executing request for creating legal entity {}", parameterHolder);
        LegalEntity legalEntity = persistenceLegalEntityService.createLegalEntity(request);
        CreateLegalEntitiesPostResponseBody legalEntityPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId(legalEntity.getId());
        legalEntityPostResponseBody.setAdditions(request.getAdditions());
        return legalEntityPostResponseBody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        CreateLegalEntitiesPostRequestBody request, CreateLegalEntitiesPostResponseBody response) {
        return new LegalEntityEvent().withId(response.getId()).withAction(Action.ADD);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        CreateLegalEntitiesPostRequestBody request, Exception failure) {
        return null;
    }
}
