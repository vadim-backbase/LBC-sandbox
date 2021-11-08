package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.service.facades.PermissionsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.properties.ApprovalLevel;
import com.backbase.accesscontrol.util.properties.ApprovalProperty;
import com.backbase.accesscontrol.util.properties.ApprovalValidation;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAssignUsersPermissionsTest {

    private static final String SERVICE_AGREEMENT = "Service Agreement";
    private static final String ASSIGN_PERMISSIONS = "Assign Permissions";
    private static final String ASSIGN_PERMISSIONS_ACTION = "EDIT";

    @InjectMocks
    private UpdateAssignUsersPermissions updateAssignUsersPermissions;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private PermissionsService permissionsService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;


    @Test
    public void putAssignUsersPermissionsZero() {
        List<PresentationFunctionDataGroup> fgDgItems = new ArrayList<>();
        PresentationGenericObjectId dg = new PresentationGenericObjectId()
            .withId("dg1");
        PresentationFunctionDataGroup fgDgItem = new PresentationFunctionDataGroup()
            .withFunctionGroupId("fgId")
            .withDataGroupIds(Collections.singletonList(dg));
        fgDgItems.add(fgDgItem);
        PresentationFunctionDataGroupItems items = new PresentationFunctionDataGroupItems()
            .withItems(fgDgItems);
        InternalRequest<PresentationFunctionDataGroupItems> internalRequest = getInternalRequest(items);
        String serviceAgreementId = "saId";
        String userId = "userId";
        ApprovalProperty approval = new ApprovalProperty();
        ApprovalLevel level = new ApprovalLevel();
        level.setEnabled(true);
        approval.setLevel(level);
        ApprovalValidation validation = new ApprovalValidation();
        validation.setEnabled(true);
        approval.setValidation(validation);
        when(applicationProperties.getApproval())
            .thenReturn(approval);
        UserContextDetailsDto details = new UserContextDetailsDto(userId, "legalEntity");

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setLegalEntityId(details.getLegalEntityId());

        when(userManagementService
            .getUserByInternalId(userId)).thenReturn(user1);
        when(userContextUtil.getUserContextDetails()).thenReturn(details);
        when(approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)).thenReturn(false);
        ApprovalDto app = new ApprovalDto()
            .serviceAgreementId(serviceAgreementId)
            .status(ApprovalStatus.APPROVED)
            .userId(userId)
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .action(ASSIGN_PERMISSIONS_ACTION);
        PresentationPostApprovalResponse response = new PresentationPostApprovalResponse()
            .approval(app);
        when(approvalsService
            .getApprovalResponse(details.getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                SERVICE_AGREEMENT, ASSIGN_PERMISSIONS, ASSIGN_PERMISSIONS_ACTION))
            .thenReturn(response);
        InternalRequest<PresentationApprovalStatus> presentationApprovalStatusInternalRequest = updateAssignUsersPermissions
            .putAssignUsersPermissions(internalRequest, serviceAgreementId, userId);
        assertEquals(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus.APPROVED,
            presentationApprovalStatusInternalRequest.getData().getApprovalStatus());
    }

    @Test
    public void putAssignUsersPermissionsWithNoApproval() {
        List<PresentationFunctionDataGroup> fgDgItems = new ArrayList<>();
        PresentationGenericObjectId dg = new PresentationGenericObjectId()
            .withId("dg1");
        PresentationFunctionDataGroup fgDgItem = new PresentationFunctionDataGroup()
            .withFunctionGroupId("fgId")
            .withDataGroupIds(Collections.singletonList(dg));
        fgDgItems.add(fgDgItem);
        PresentationFunctionDataGroupItems items = new PresentationFunctionDataGroupItems()
            .withItems(fgDgItems);
        InternalRequest<PresentationFunctionDataGroupItems> internalRequest = getInternalRequest(items);
        String serviceAgreementId = "saId";
        String userId = "userId";
        ApprovalProperty approval = new ApprovalProperty();
        ApprovalLevel level = new ApprovalLevel();
        level.setEnabled(false);
        approval.setLevel(level);
        ApprovalValidation validation = new ApprovalValidation();
        validation.setEnabled(false);
        approval.setValidation(validation);
        when(applicationProperties.getApproval())
            .thenReturn(approval);
        InternalRequest<PresentationApprovalStatus> presentationApprovalStatusInternalRequest = updateAssignUsersPermissions
            .putAssignUsersPermissions(internalRequest, serviceAgreementId, userId);
        assertNull(presentationApprovalStatusInternalRequest.getData().getApprovalStatus());
    }

    @Test
    public void putAssignUsersPermissionsPending() {
        List<PresentationFunctionDataGroup> fgDgItems = new ArrayList<>();
        PresentationGenericObjectId dg = new PresentationGenericObjectId()
            .withId("dg1");
        PresentationFunctionDataGroup fgDgItem = new PresentationFunctionDataGroup()
            .withFunctionGroupId("fgId")
            .withDataGroupIds(Collections.singletonList(dg));
        fgDgItems.add(fgDgItem);
        PresentationFunctionDataGroupItems items = new PresentationFunctionDataGroupItems()
            .withItems(fgDgItems);
        InternalRequest<PresentationFunctionDataGroupItems> internalRequest = getInternalRequest(items);
        String serviceAgreementId = "saId";
        String userId = "userId";
        ApprovalProperty approval = new ApprovalProperty();
        ApprovalLevel level = new ApprovalLevel();
        level.setEnabled(true);
        approval.setLevel(level);
        ApprovalValidation validation = new ApprovalValidation();
        validation.setEnabled(true);
        approval.setValidation(validation);
        when(applicationProperties.getApproval())
            .thenReturn(approval);
        UserContextDetailsDto details = new UserContextDetailsDto(userId, "legalEntity");
        when(userContextUtil.getUserContextDetails()).thenReturn(details);
        when(approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)).thenReturn(false);
        ApprovalDto app = new ApprovalDto()
            .id("appId")
            .serviceAgreementId(serviceAgreementId)
            .status(ApprovalStatus.PENDING)
            .userId(userId)
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .action(ASSIGN_PERMISSIONS_ACTION);

        PresentationPostApprovalResponse response = new PresentationPostApprovalResponse()
            .approval(app);
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setLegalEntityId(details.getLegalEntityId());
        when(userManagementService
            .getUserByInternalId(userId)).thenReturn(user1);
        when(approvalsService
            .getApprovalResponse(details.getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                SERVICE_AGREEMENT, ASSIGN_PERMISSIONS, ASSIGN_PERMISSIONS_ACTION))
            .thenReturn(response);
        doNothing().when(permissionsService)
            .savePermissionsToApproval(items, serviceAgreementId,
                userId, details.getLegalEntityId(), response.getApproval().getId());
        InternalRequest<PresentationApprovalStatus> presentationApprovalStatusInternalRequest = updateAssignUsersPermissions
            .putAssignUsersPermissions(internalRequest, serviceAgreementId, userId);
        assertEquals(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus.PENDING,
            presentationApprovalStatusInternalRequest.getData().getApprovalStatus());
    }

    @Test
    public void putAssignUsersPermissionsPendingBadRequest() {
        List<PresentationFunctionDataGroup> fgDgItems = new ArrayList<>();
        PresentationGenericObjectId dg = new PresentationGenericObjectId()
            .withId("dg1");
        PresentationFunctionDataGroup fgDgItem = new PresentationFunctionDataGroup()
            .withFunctionGroupId("fgId")
            .withDataGroupIds(Collections.singletonList(dg));
        fgDgItems.add(fgDgItem);
        PresentationFunctionDataGroupItems items = new PresentationFunctionDataGroupItems()
            .withItems(fgDgItems);
        InternalRequest<PresentationFunctionDataGroupItems> internalRequest = getInternalRequest(items);
        String serviceAgreementId = "saId";
        String userId = "userId";
        ApprovalProperty approval = new ApprovalProperty();
        ApprovalLevel level = new ApprovalLevel();
        level.setEnabled(true);
        approval.setLevel(level);
        ApprovalValidation validation = new ApprovalValidation();
        validation.setEnabled(true);
        approval.setValidation(validation);
        when(applicationProperties.getApproval())
            .thenReturn(approval);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setLegalEntityId("legalEntity");
        when(userManagementService.getUserByInternalId(userId)).thenReturn(user1);
        when(approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)).thenReturn(true);
        when(approvalUserContextJpaRepository.countByServiceAgreementIdAndLegalEntityId(
            serviceAgreementId, "legalEntity")).thenReturn(0L);
        assertThrows(BadRequestException.class,
            () -> updateAssignUsersPermissions
                .putAssignUsersPermissions(internalRequest, serviceAgreementId, userId));

    }

    @Test
    public void putAssignUsersPermissionsRejected() {

        List<PresentationFunctionDataGroup> fgDgItems = new ArrayList<>();
        PresentationGenericObjectId dg = new PresentationGenericObjectId()
            .withId("dg1");
        PresentationFunctionDataGroup fgDgItem = new PresentationFunctionDataGroup()
            .withFunctionGroupId("fgId")
            .withDataGroupIds(Collections.singletonList(dg));
        fgDgItems.add(fgDgItem);
        PresentationFunctionDataGroupItems items = new PresentationFunctionDataGroupItems()
            .withItems(fgDgItems);
        InternalRequest<PresentationFunctionDataGroupItems> internalRequest = getInternalRequest(items);
        String serviceAgreementId = "saId";
        String userId = "userId";
        ApprovalProperty approval = new ApprovalProperty();
        ApprovalLevel level = new ApprovalLevel();
        level.setEnabled(true);
        approval.setLevel(level);
        ApprovalValidation validation = new ApprovalValidation();
        validation.setEnabled(true);
        approval.setValidation(validation);
        when(applicationProperties.getApproval())
            .thenReturn(approval);
        when(approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)).thenReturn(false);
        UserContextDetailsDto details = new UserContextDetailsDto(userId, "legalEntity");
        when(userContextUtil.getUserContextDetails()).thenReturn(details);
        ApprovalDto app = new ApprovalDto()
            .id("appId")
            .serviceAgreementId(serviceAgreementId)
            .status(ApprovalStatus.PENDING)
            .userId(userId)
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .action(ASSIGN_PERMISSIONS_ACTION);
        PresentationPostApprovalResponse response = new PresentationPostApprovalResponse()
            .approval(app);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setLegalEntityId(details.getLegalEntityId());
        when(userManagementService
            .getUserByInternalId(userId)).thenReturn(user1);
        when(approvalsService
            .getApprovalResponse(details.getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                SERVICE_AGREEMENT, ASSIGN_PERMISSIONS, ASSIGN_PERMISSIONS_ACTION))
            .thenReturn(response);
        Error error = new Error();
        error.setMessage("error");
        doThrow(new BadRequestException().withErrors(Collections.singletonList(error))).when(permissionsService)
            .savePermissionsToApproval(items, serviceAgreementId,
                userId, details.getLegalEntityId(), response.getApproval().getId());

        assertThrows(BadRequestException.class,
            () -> updateAssignUsersPermissions
                .putAssignUsersPermissions(internalRequest, serviceAgreementId, userId));

    }
}