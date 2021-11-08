package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EditServiceAgreementHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @InjectMocks
    private EditServiceAgreementHandler editServiceAgreementHandler;

    @Test
    public void testExecuteRequest() {
        String serviceAgreementId = "sa id";
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave requestData = new ServiceAgreementSave();
        editServiceAgreementHandler.executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).save(serviceAgreementId, requestData);

    }

    @Test
    public void testCreateSuccessEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        ServiceAgreementEvent successEvent = editServiceAgreementHandler
            .createSuccessEvent(parameterHolder, null, null);
        assertNotNull(successEvent);
        assertEquals(Action.UPDATE, successEvent.getAction());
        assertEquals(serviceAgreementId, successEvent.getId());
    }

    @Test
    public void testCreateFailureEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Exception failure = new Exception("error msg");
        Event failureEvent = editServiceAgreementHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }
}