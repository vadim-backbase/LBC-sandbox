package com.backbase.accesscontrol.business.serviceagreement;


import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.ServiceAgreementApprovalService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.util.Objects;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EditServiceAgreementTest {

    @Spy
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ServiceAgreementApprovalService serviceAgreementApprovalService;
    @Mock
    private ApprovalsService approvalsService;
    @InjectMocks
    private EditServiceAgreement editServiceAgreement;

    @Test
    public void shouldSuccessfullyUpdateServiceAgreement() {
        String serviceAgreementId = "SA-01";

        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-02")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(ENABLED)
            .withParticipants(newHashSet(participant1, participant2));

        mockApprovalValidation(applicationProperties, false);

        doNothing().when(serviceAgreementApprovalService)
            .updateServiceAgreement(any(ServiceAgreementSave.class), eq(serviceAgreementId));

        editServiceAgreement.editServiceAgreement(getInternalRequest(serviceAgreementSaveBody), serviceAgreementId);

        ArgumentCaptor<ServiceAgreementSave> captor = ArgumentCaptor.forClass(ServiceAgreementSave.class);
        verify(serviceAgreementApprovalService).updateServiceAgreement(captor.capture(), eq(serviceAgreementId));

        assertThat(captor.getValue(), serviceAgreementSaveMatcher(serviceAgreementSaveBody));
        assertFalse(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldSuccessfullyUpdateServiceAgreementWithApprovalOnAndZeroPolicy() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";
        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(DISABLED)
            .withParticipants(newHashSet(participant1, participant2));

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        mockApprovalService(userId, serviceAgreementId, ApprovalStatus.APPROVED);

        doNothing().when(serviceAgreementApprovalService)
            .updateServiceAgreement(any(ServiceAgreementSave.class), eq(serviceAgreementId));

        editServiceAgreement.editServiceAgreement(getInternalRequest(serviceAgreementSaveBody), serviceAgreementId);

        ArgumentCaptor<ServiceAgreementSave> captor = ArgumentCaptor.forClass(ServiceAgreementSave.class);
        verify(serviceAgreementApprovalService).updateServiceAgreement(captor.capture(), eq(serviceAgreementId));

        assertThat(captor.getValue(), serviceAgreementSaveMatcher(serviceAgreementSaveBody));
        assertFalse(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldSuccessfullyUpdateServiceAgreementWithApprovalOn() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";

        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(ENABLED)
            .withParticipants(newHashSet(participant1, participant2));

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        ApprovalDto approvalDto = mockApprovalService(userId, serviceAgreementId, ApprovalStatus.PENDING);

        doNothing().when(serviceAgreementApprovalService)
            .updateServiceAgreementWithApproval(any(ServiceAgreementSave.class), eq(serviceAgreementId),
                eq(approvalDto.getId()));

        editServiceAgreement.editServiceAgreement(getInternalRequest(serviceAgreementSaveBody), serviceAgreementId);

        ArgumentCaptor<ServiceAgreementSave> captor = ArgumentCaptor.forClass(ServiceAgreementSave.class);
        verify(serviceAgreementApprovalService)
            .updateServiceAgreementWithApproval(captor.capture(), eq(serviceAgreementId), eq(approvalDto.getId()));

        assertThat(captor.getValue(), serviceAgreementSaveMatcher(serviceAgreementSaveBody));
        assertTrue(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldCancelApprovalRequestWhenExceptionThrownOnAddServiceAgreementWithApprovalOn() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";

        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(ENABLED)
            .withParticipants(newHashSet(participant1, participant2));

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        ApprovalDto approvalDto = mockApprovalService(userId, serviceAgreementId, ApprovalStatus.PENDING);

        doThrow(new BadRequestException("Exception thrown")).when(serviceAgreementApprovalService)
            .updateServiceAgreementWithApproval(any(ServiceAgreementSave.class), eq(serviceAgreementId),
                eq(approvalDto.getId()));

        assertThrows(BadRequestException.class,
            () -> editServiceAgreement
                .editServiceAgreement(getInternalRequest(serviceAgreementSaveBody), serviceAgreementId));

        ArgumentCaptor<ServiceAgreementSave> captor = ArgumentCaptor.forClass(ServiceAgreementSave.class);
        verify(serviceAgreementApprovalService)
            .updateServiceAgreementWithApproval(captor.capture(), eq(serviceAgreementId), eq(approvalDto.getId()));

        assertThat(captor.getValue(), serviceAgreementSaveMatcher(serviceAgreementSaveBody));
        verify(approvalsService).cancelApprovalRequest(eq(approvalDto.getId()));
        assertFalse(approvalOnRequestScope.isApproval());
    }

    private ApprovalDto mockApprovalService(String userId, String saId, ApprovalStatus status) {
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId(userId)
            .serviceAgreementId(saId)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("EDIT")
            .status(status);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalsService
            .getApprovalResponse(eq(userId), eq(saId), eq("Entitlements"), eq("Manage Service Agreements"),
                eq("EDIT")))
            .thenReturn(approvalResponse);
        return approval;
    }

    private Matcher<ServiceAgreementSave> serviceAgreementSaveMatcher(ServiceAgreementSave serviceAgreementSaveBody) {
        return allOf(
            hasProperty("name", equalTo(serviceAgreementSaveBody.getName())),
            hasProperty("description", equalTo(serviceAgreementSaveBody.getDescription())),
            hasProperty("externalId", equalTo(serviceAgreementSaveBody.getExternalId())),
            hasProperty("status", equalTo(serviceAgreementSaveBody.getStatus())),
            hasProperty("isMaster", equalTo(
                Objects.nonNull(serviceAgreementSaveBody.getIsMaster()) ? serviceAgreementSaveBody.getIsMaster()
                    : false)),
            hasProperty("participants", containsInAnyOrder(serviceAgreementSaveBody.getParticipants().toArray()))
        );
    }
}
