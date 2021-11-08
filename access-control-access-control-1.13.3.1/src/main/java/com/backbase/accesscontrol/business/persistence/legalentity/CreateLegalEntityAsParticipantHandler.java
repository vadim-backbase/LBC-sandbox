package com.backbase.accesscontrol.business.persistence.legalentity;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventsEmitter;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateLegalEntityAsParticipantHandler extends
                LeanGenericEventsEmitter<SingleParameterHolder<String>, LegalEntityAsParticipantPostRequestBody, LegalEntityAsParticipantPostResponseBody> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntityAsParticipantHandler.class);
    
    private PersistenceLegalEntityService persistenceLegalEntityService;
    
    public CreateLegalEntityAsParticipantHandler(EventBus eventBus, PersistenceLegalEntityService persistenceLegalEntityService) {
        super(eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityAsParticipantPostResponseBody executeRequest(SingleParameterHolder<String> parameterHolder,
                    LegalEntityAsParticipantPostRequestBody request) {
        LOGGER.info("Executing request for creating legal entity as participant {}", parameterHolder);
        return persistenceLegalEntityService.createLegalEntityAsParticipant(request, parameterHolder.getParameter());
    }
    
    @Override
    protected List<Event> createSuccessEvents(SingleParameterHolder<String> parameterHolder,
                    LegalEntityAsParticipantPostRequestBody request,
                    LegalEntityAsParticipantPostResponseBody response) {

        List<Event> events = new ArrayList<>();
        
        LegalEntityEvent legalEntityEvent = new LegalEntityEvent().withId(response.getLegalEntityId()).withAction(Action.ADD);
        events.add(legalEntityEvent);
      
        ServiceAgreementEvent serviceAgreementEvent = new ServiceAgreementEvent().withId(response.getServiceAgreementId());
        
        if (response.getServiceAgreementId() != null) {
            serviceAgreementEvent.withAction(Action.ADD);
        } else {
            serviceAgreementEvent.withAction(Action.UPDATE);
        }
    
        events.add(serviceAgreementEvent);
        
        return events;
    }
    
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
                    LegalEntityAsParticipantPostRequestBody request, Exception failure) {
        return null;
    }

    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder,
                    LegalEntityAsParticipantPostRequestBody request,
                    LegalEntityAsParticipantPostResponseBody response) {
        return null;
    }


}
