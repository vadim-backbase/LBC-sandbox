package com.backbase.accesscontrol.business.flows.approval;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.GetDataGroupApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataGroupApprovalDetailsByIdFlowTest {

    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @InjectMocks
    private GetDataGroupApprovalDetailsByIdFlow getDataGroupApprovalDetailsByIdFlow;

    @Test
    public void shouldExecuteGetDataGroupApprovalDetailsByIdFlow() {

        String appId = "appId";
        String saId = "saId";

        PresentationDataGroupApprovalDetailsItem persistenceApprovalDataGroups =
            new PresentationDataGroupApprovalDetailsItem()
                .withApprovalId(appId)
                .withServiceAgreementId(saId)
                .withDataGroupId("dgId")
                .withAddedDataItems(Sets.newHashSet("1", "2"));

        PresentationDataGroupApprovalDetailsItem presentationDataGroupApprovalDetailsItem =
            new PresentationDataGroupApprovalDetailsItem()
                .withApprovalId(appId)
                .withServiceAgreementId(saId)
                .withDataGroupId("dgId")
                .withAddedDataItems(Sets.newHashSet("1", "2"));
        when(accessControlApprovalService
            .getPersistenceApprovalDataGroups(any(), any(), any()))
            .thenReturn(persistenceApprovalDataGroups);

        GetDataGroupApprovalDetailsParametersFlow parameters = new GetDataGroupApprovalDetailsParametersFlow()
            .withUserId("user")
            .withServiceAgreementId(saId)
            .withApprovalId(appId);
        PresentationDataGroupApprovalDetailsItem response = getDataGroupApprovalDetailsByIdFlow.execute(parameters);
        assertEquals(presentationDataGroupApprovalDetailsItem, response);
    }
}