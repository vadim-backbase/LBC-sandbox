package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.service.ServiceAgreementIngestService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestServiceAgreementHandlerTest {

    @InjectMocks
    private IngestServiceAgreementHandler ingestServiceAgreementHandler;
    @Mock
    private ServiceAgreementIngestService serviceAgreementIngestService;
    @Mock
    private EventBus eventBus;

    @Test
    public void shouldInvokeService() {
        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> requestData = new ServiceAgreementData<>(
            new ServiceAgreementIngestPostRequestBody(), new HashMap<>());

        when(serviceAgreementIngestService.ingestServiceAgreement(eq(requestData)))
            .thenReturn("id");
        ServiceAgreementIngestPostResponseBody serviceAgreementIngestPostResponseBody = ingestServiceAgreementHandler
            .handleRequest(null, requestData);
        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        assertEquals("id", serviceAgreementIngestPostResponseBody.getId());
    }

    @Test
    public void shouldCreateSucessfulEvent() {
        ServiceAgreementEvent successEvent = ingestServiceAgreementHandler
            .createSuccessEvent(null, new ServiceAgreementData<>(
                new ServiceAgreementIngestPostRequestBody(), new HashMap<>()),
                new ServiceAgreementIngestPostResponseBody().withId("id"));

        assertNotNull(successEvent);
        assertEquals(Action.ADD, successEvent.getAction());
        assertEquals("id", successEvent.getId());
    }

    @Test
    public void shouldCreateFailedEvent() {
        Event failureEvent = ingestServiceAgreementHandler
            .createFailureEvent(null, null, new RuntimeException("message"));
        assertNull(failureEvent);
    }
}