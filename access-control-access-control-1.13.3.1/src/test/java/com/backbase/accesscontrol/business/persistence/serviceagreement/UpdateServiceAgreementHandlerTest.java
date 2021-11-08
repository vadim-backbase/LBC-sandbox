package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private UpdateServiceAgreementHandler updateServiceAgreementHandler;

    @Test
    public void testExecuteRequest() {
        String serviceAgreementId = "sa id";
        ServiceAgreementPutRequestBody requestData = new ServiceAgreementPutRequestBody();
        updateServiceAgreementHandler.executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).updateServiceAgreement(serviceAgreementId, requestData);

    }

    @Test
    public void testHandleRequest() {
        String serviceAgreementId = "sa id";
        ServiceAgreementPutRequestBody requestData = new ServiceAgreementPutRequestBody();
        updateServiceAgreementHandler.handleRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).updateServiceAgreement(serviceAgreementId, requestData);
        verify(eventBus, times(1)).emitEvent(any());
    }

    @Test
    public void testCreateSuccessEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        ServiceAgreementEvent successEvent = updateServiceAgreementHandler
            .createSuccessEvent(parameterHolder, null, null);

        assertNotNull(successEvent);
        Assert.assertEquals(Action.UPDATE, successEvent.getAction());
        Assert.assertEquals(serviceAgreementId, successEvent.getId());
    }

    @Test
    public void testCreateFailureEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Exception failure = new Exception("error msg");
        Event failureEvent = updateServiceAgreementHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }
}