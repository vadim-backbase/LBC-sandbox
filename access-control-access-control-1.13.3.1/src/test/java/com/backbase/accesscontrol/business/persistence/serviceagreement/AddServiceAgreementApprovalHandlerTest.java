package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.LegalEntityIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddServiceAgreementApprovalHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @InjectMocks
    private AddServiceAgreementApprovalHandler addServiceAgreementApprovalHandler;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        String serviceAgreementName = "name";
        String leId = "id";
        String approvalId = "approvalId";

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody()
            .withName(serviceAgreementName)
            .withStatus(CreateStatus.DISABLED);

        doReturn(approvalId).when(persistenceServiceAgreementService)
            .saveServiceAgreementApproval(eq(serviceAgreementPostRequestBody), eq(leId), eq(approvalId));

        ServiceAgreementPostResponseBody responseBody = addServiceAgreementApprovalHandler
            .executeRequest(new LegalEntityIdApprovalIdParameterHolder(leId, approvalId),
                serviceAgreementPostRequestBody);

        verify(persistenceServiceAgreementService)
            .saveServiceAgreementApproval(eq(serviceAgreementPostRequestBody), eq(leId), eq(approvalId));
        assertEquals(approvalId, responseBody.getId());
    }

    @Test
    public void testCreateSuccessEvent() {
        LegalEntityIdApprovalIdParameterHolder parameterHolder = new LegalEntityIdApprovalIdParameterHolder("leId",
            "approvalId");
        Event successEvent = addServiceAgreementApprovalHandler
            .createSuccessEvent(parameterHolder, null, null);
        assertNull(successEvent);
    }

    @Test
    public void testCreateFailureEvent() {
        LegalEntityIdApprovalIdParameterHolder parameterHolder = new LegalEntityIdApprovalIdParameterHolder("leId",
            "approvalId");
        Exception failure = new Exception("error msg");
        Event failureEvent = addServiceAgreementApprovalHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }
}
