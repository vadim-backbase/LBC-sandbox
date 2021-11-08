package com.backbase.accesscontrol.business.persistence.legalentity;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddLegalEntityHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, LegalEntitiesPostRequestBody, LegalEntitiesPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddLegalEntityHandler.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;

    public AddLegalEntityHandler(EventBus eventBus, PersistenceLegalEntityService persistenceLegalEntityService) {
        super(eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntitiesPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        LegalEntitiesPostRequestBody request) {
        LOGGER.info("Executing request for creating legal entity {}", parameterHolder);
        LegalEntity legalEntity = persistenceLegalEntityService.addLegalEntity(request);
        LegalEntitiesPostResponseBody legalEntityPostResponseBody = new LegalEntitiesPostResponseBody()
            .withId(legalEntity.getId());
        legalEntityPostResponseBody.setAdditions(request.getAdditions());
        return legalEntityPostResponseBody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        LegalEntitiesPostRequestBody request, LegalEntitiesPostResponseBody response) {
        return new LegalEntityEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityEvent createFailureEvent(EmptyParameterHolder parameterHolder,
        LegalEntitiesPostRequestBody request, Exception failure) {
        return null;
    }
}
