package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.parameterholder.ServiceAgreementIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EditServiceAgreementApprovalHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    @InjectMocks
    private EditServiceAgreementApprovalHandler editServiceAgreementApprovalHandler;

    @Test
    public void testExecuteRequest() {
        String approvalId = "approvalId";
        String serviceAgreementId = "sa id";
        ServiceAgreementSave requestData = new ServiceAgreementSave();
        editServiceAgreementApprovalHandler
            .executeRequest(new ServiceAgreementIdApprovalIdParameterHolder(serviceAgreementId, approvalId),
                requestData);
        verify(persistenceServiceAgreementService)
            .updateServiceAgreementApproval(eq(requestData), eq(serviceAgreementId), eq(approvalId));
    }

    @Test
    public void testCreateSuccessEvent() {
        String approvalId = "approvalId";
        String serviceAgreementId = "sa id";
        ServiceAgreementIdApprovalIdParameterHolder parameterHolder = new ServiceAgreementIdApprovalIdParameterHolder(
            serviceAgreementId, approvalId);
        Event successEvent = editServiceAgreementApprovalHandler.createSuccessEvent(parameterHolder, null, null);
        assertNull(successEvent);
    }

    @Test
    public void testCreateFailureEvent() {
        String approvalId = "approvalId";
        String serviceAgreementId = "sa id";
        ServiceAgreementIdApprovalIdParameterHolder parameterHolder = new ServiceAgreementIdApprovalIdParameterHolder(
            serviceAgreementId, approvalId);
        Exception failure = new Exception("error msg");

        Event failureEvent = editServiceAgreementApprovalHandler.createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }
}