package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddServiceAgreementHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @InjectMocks
    private AddServiceAgreementHandler addServiceAgreementHandler;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        String serviceAgreementName = "name";
        String leId = "id";

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody =
            new ServiceAgreementPostRequestBody()
                .withName(serviceAgreementName)
                .withStatus(CreateStatus.DISABLED);

        ArgumentCaptor<ServiceAgreementPostRequestBody> captor = ArgumentCaptor
            .forClass(ServiceAgreementPostRequestBody.class);
        doReturn(createServiceAgreement(serviceAgreementName)).when(persistenceServiceAgreementService)
            .save(serviceAgreementPostRequestBody, leId);
        addServiceAgreementHandler.executeRequest(new SingleParameterHolder<>(leId), serviceAgreementPostRequestBody);

        verify(persistenceServiceAgreementService).save(captor.capture(), eq(leId));

        ServiceAgreementPostRequestBody value = captor.getValue();
        assertEquals(serviceAgreementName, value.getName());
        assertEquals(Status.DISABLED.toString(), value.getStatus().toString());
    }

    private ServiceAgreement createServiceAgreement(
        String serviceAgreementName) {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(UUID.randomUUID().toString());
        serviceAgreement.setName(serviceAgreementName);
        return serviceAgreement;
    }

    @Test
    public void testCreateSuccessEvent() {

        ServiceAgreementEvent successEvent = addServiceAgreementHandler
            .createSuccessEvent(new SingleParameterHolder<>("value"), new ServiceAgreementPostRequestBody(),
                new ServiceAgreementPostResponseBody().withId("id"));

        assertNotNull(successEvent);
        assertEquals(Action.ADD, successEvent.getAction());
        assertEquals("id", successEvent.getId());

    }

}
