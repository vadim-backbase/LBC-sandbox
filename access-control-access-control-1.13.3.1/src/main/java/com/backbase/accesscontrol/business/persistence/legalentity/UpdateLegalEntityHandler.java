package com.backbase.accesscontrol.business.persistence.legalentity;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateLegalEntityHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, LegalEntityByExternalIdPutRequestBody, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLegalEntityHandler.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;

    public UpdateLegalEntityHandler(EventBus eventBus, PersistenceLegalEntityService persistenceLegalEntityService) {
        super(eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String executeRequest(SingleParameterHolder<String> parameterHolder,
        LegalEntityByExternalIdPutRequestBody request) {
        LOGGER.info("Executing request {} to update legal entity", parameterHolder);
        return persistenceLegalEntityService.updateLegalEntity(parameterHolder.getParameter(), request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        LegalEntityByExternalIdPutRequestBody request, String internalId) {
        return new LegalEntityEvent().withId(internalId).withAction(Action.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        LegalEntityByExternalIdPutRequestBody request, Exception failure) {
        return null;
    }
}
