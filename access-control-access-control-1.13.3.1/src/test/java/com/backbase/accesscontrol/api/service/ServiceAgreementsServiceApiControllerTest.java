package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.BatchResponseItemToBatchResponseItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ListOfFunctionGroupsWithDataGroupsToPresentationFunctionDataGroupItemsMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.PresentationApprovalStatusToPresentationApprovalStatusMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServiceAgreementBatchDeleteToPresentationDeleteServiceAgreementsMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServiceAgreementParticipantsGetResponseBodyToServiceAgreementParticipantsGetResponseBodyMapper;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.model.ListOfFunctionGroupsWithDataGroups;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementBatchDelete;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementsServiceApiControllerTest {

    @Mock
    private ServiceAgreementService serviceAgreementService;
    @Mock
    private UserContextUtil userContextUtil;
    @InjectMocks
    private ServiceAgreementsServiceApiController serviceAgreementsServiceApiController;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(
                ServiceAgreementParticipantsGetResponseBodyToServiceAgreementParticipantsGetResponseBodyMapper.class)),
            spy(Mappers.getMapper(PresentationApprovalStatusToPresentationApprovalStatusMapper.class)),
            spy(Mappers.getMapper(BatchResponseItemToBatchResponseItemMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementBatchDeleteToPresentationDeleteServiceAgreementsMapper.class)),
            spy(Mappers.getMapper(ListOfFunctionGroupsWithDataGroupsToPresentationFunctionDataGroupItemsMapper.class))
        ));

    @Test
    public void shouldGetServiceAgreementParticipants() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementParticipantsGetResponseBody body = new ServiceAgreementParticipantsGetResponseBody()
            .withExternalId(serviceAgreementId)
            .withName("name")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withId("id");
        List<ServiceAgreementParticipantsGetResponseBody> serviceAgreementParticipantsGetResponseBodyList = Collections
            .singletonList(body);

        when(serviceAgreementService.getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(serviceAgreementParticipantsGetResponseBodyList);

        List<com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody>
            serviceAgreementParticipants = serviceAgreementsServiceApiController
            .getServiceAgreementParticipants(serviceAgreementId).getBody();

        assertNotNull(serviceAgreementParticipants);
        assertEquals(serviceAgreementParticipantsGetResponseBodyList.size(), serviceAgreementParticipants.size());
        assertEquals(serviceAgreementParticipantsGetResponseBodyList.get(0).getExternalId(),
            serviceAgreementParticipants.get(0).getExternalId());
    }

    @Test
    public void shouldGetContextServiceAgreementParticipants() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementParticipantsGetResponseBody body = new ServiceAgreementParticipantsGetResponseBody()
            .withExternalId(serviceAgreementId)
            .withName("name")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withId("id");
        List<ServiceAgreementParticipantsGetResponseBody> serviceAgreementParticipantsGetResponseBodyList = Collections
            .singletonList(body);

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        when(serviceAgreementService.getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(serviceAgreementParticipantsGetResponseBodyList);

        List<com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody>
            serviceAgreementParticipants = serviceAgreementsServiceApiController
            .getContextServiceAgreementParticipants().getBody();

        assertNotNull(serviceAgreementParticipants);
        assertEquals(serviceAgreementParticipantsGetResponseBodyList.size(), serviceAgreementParticipants.size());
        assertEquals(serviceAgreementParticipantsGetResponseBodyList.get(0).getExternalId(),
            serviceAgreementParticipants.get(0).getExternalId());
    }

    @Test
    public void shouldDeleteServiceAgreementsByIdentifiers() {
        BatchResponseItem item1 = new BatchResponseItem()
            .withResourceId("saName")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);

        List<BatchResponseItem> responses = singletonList(item1);

        PresentationServiceAgreementIdentifier identifier = new PresentationServiceAgreementIdentifier()
            .nameIdentifier("saName");
        List<PresentationServiceAgreementIdentifier>
            identifiers = singletonList(identifier);

        ServiceAgreementBatchDelete serviceAgreements = new ServiceAgreementBatchDelete()
            .accessToken("123")
            .serviceAgreementIdentifiers(identifiers);

        when(serviceAgreementService.batchDeleteServiceAgreement(any())).thenReturn(responses);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem> batchResponseItems =
            serviceAgreementsServiceApiController
                .postBatchdelete(serviceAgreements).getBody();

        assertNotNull(batchResponseItems);
        assertEquals(responses.size(), batchResponseItems.size());
        assertEquals(responses.get(0).getResourceId(), batchResponseItems.get(0).getResourceId());
    }

    @Test
    public void shouldInvokeService() {
        String saId = "sa-id";
        String userId = "us-id";
        PresentationApprovalStatus approvalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.PENDING);

        ListOfFunctionGroupsWithDataGroups functionDataGroupItems = new ListOfFunctionGroupsWithDataGroups();

        when(serviceAgreementService.putAssignUsersPermissions(any(), eq(saId), eq(userId)))
            .thenReturn(approvalStatus);

        com.backbase.accesscontrol.service.rest.spec.model.PresentationApprovalStatus presentationApprovalStatus =
            serviceAgreementsServiceApiController.putAssignUsersPermissions(saId, userId, functionDataGroupItems)
                .getBody();

        assertEquals(approvalStatus.getApprovalStatus().toString(),
            presentationApprovalStatus.getApprovalStatus().toString());
    }
}
