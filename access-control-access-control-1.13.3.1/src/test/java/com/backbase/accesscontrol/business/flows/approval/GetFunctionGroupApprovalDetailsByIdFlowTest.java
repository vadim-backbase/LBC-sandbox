package com.backbase.accesscontrol.business.flows.approval;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.GetFunctionGroupApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetFunctionGroupApprovalDetailsByIdFlowTest {

    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @InjectMocks
    private GetFunctionGroupApprovalDetailsByIdFlow getFunctionGroupApprovalDetailsByIdFlow;

    @Test
    public void testShouldExecuteGetFunctionGroupApprovalDetailsByIdFlow() {

        String appId = "appId";
        String saId = "saId";

        PresentationFunctionGroupApprovalDetailsItem presentationFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem()
                .withFunctionGroupId("0955e686d31e4216b3dd5d66161d536d")
                .withApprovalId("606d4532-f8d9-4a5f-36kl-887baf88fa24")
                .withServiceAgreementId("0889e686d31e4216b3dd5d66163d2b14")
                .withServiceAgreementName("saName");

        PresentationFunctionGroupApprovalDetailsItem persistenceFunctionGroupApprovalDetailsItem =
            new PresentationFunctionGroupApprovalDetailsItem()
                .withFunctionGroupId("0955e686d31e4216b3dd5d66161d536d")
                .withApprovalId("606d4532-f8d9-4a5f-36kl-887baf88fa24")
                .withServiceAgreementId("0889e686d31e4216b3dd5d66163d2b14")
                .withServiceAgreementName("saName");

        GetFunctionGroupApprovalDetailsParametersFlow parameters = new GetFunctionGroupApprovalDetailsParametersFlow();
        parameters.setUserId("user");
        parameters.setServiceAgreementId(saId);
        parameters.setApprovalId(appId);

        when(accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(eq(parameters.getApprovalId()),
                eq(parameters.getServiceAgreementId()),
                eq(parameters.getUserId())))
            .thenReturn(persistenceFunctionGroupApprovalDetailsItem);

        PresentationFunctionGroupApprovalDetailsItem response =
            getFunctionGroupApprovalDetailsByIdFlow.execute(parameters);
        assertEquals(presentationFunctionGroupApprovalDetailsItem, response);
    }
}
