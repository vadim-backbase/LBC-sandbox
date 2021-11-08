package com.backbase.accesscontrol.business.useraccess;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.batch.permission.UserPermissionBatchUpdate;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignUserPermissionsTest {

    @Mock
    private EventBus eventBus;
    @Mock
    private InternalRequestContext internalRequestContext;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private UserPermissionBatchUpdate userPermissionBatchUpdate;
    @Spy
    private final BatchResponseItemExtendedMapper batchResponseItemExtendedMapper = Mappers.getMapper(
        BatchResponseItemExtendedMapper.class);

    @InjectMocks
    private AssignUserPermissions assignUserPermissions;


    @Test
    public void shouldFilterInvalidRequestsAndFillTheEmptyPlacesOfResponse() {

        List<PresentationFunctionGroupDataGroup> incorrectList = new ArrayList<>();
        incorrectList.add(null);

        PresentationAssignUserPermissions requestNoUserPermissions = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa1")
            .withExternalUserId("user1")
            .withFunctionGroupDataGroups(incorrectList);
        PresentationAssignUserPermissions requestWithBothIdentifiersInFG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa3")
            .withExternalUserId("user3")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier("fgId1")
                    .withNameIdentifier(new NameIdentifier()
                        .withName("fgNme4")
                        .withExternalServiceAgreementId("sa3")))));
        PresentationAssignUserPermissions requestWithNoIdentifiersInFG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa4")
            .withExternalUserId("user4")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()));
        PresentationAssignUserPermissions requestWithNotExistingUser = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa5")
            .withExternalUserId("user5")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId16"))));
        PresentationAssignUserPermissions requestWithNotMatchingSAInDG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa6")
            .withExternalUserId("user6")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId6"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier()
                        .withExternalServiceAgreementId("sa61")
                        .withName("dgName6"))))));
        PresentationAssignUserPermissions requestWithBothIdentifiersInDG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa7")
            .withExternalUserId("user7")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId7"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()
                    .withIdIdentifier("dgId7")
                    .withNameIdentifier(new NameIdentifier()
                        .withExternalServiceAgreementId("sa7")
                        .withName("dgName7"))))));
        PresentationAssignUserPermissions requestWithNoIdentifiersInDG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa8")
            .withExternalUserId("user8")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId8"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()))));
        PresentationAssignUserPermissions requestValid1 = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa9")
            .withExternalUserId("user9")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId9"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier()
                        .withExternalServiceAgreementId("sa9")
                        .withName("dgName9"))))));
        PresentationAssignUserPermissions requestValid2 = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa10")
            .withExternalUserId("корисник10")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId10"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()
                    .withIdIdentifier("dgId10")))));
        PresentationAssignUserPermissions requestValid3 = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa11")
            .withExternalUserId("x æ a-12")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId11"))));
        PresentationAssignUserPermissions requestValid4 = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa12")
            .withExternalUserId("user12")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier()
                        .withExternalServiceAgreementId("sa12")
                        .withName("fgName12")))));
        PresentationAssignUserPermissions requestValid5 = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa13")
            .withExternalUserId("user13");

        com.backbase.dbs.user.api.client.v2.model.GetUser userValid1 =
            getUsers("USER9", "leid9", "uid9");

        com.backbase.dbs.user.api.client.v2.model.GetUser userValid2 =
            getUsers("КОРИСНИК10", "leid10", "uid10");

        com.backbase.dbs.user.api.client.v2.model.GetUser userValid3 =
            getUsers("X Æ A-12", "leid11", "uid11");

        com.backbase.dbs.user.api.client.v2.model.GetUser userValid4 =
            getUsers("USER12", "leid12", "uid12");

        com.backbase.dbs.user.api.client.v2.model.GetUser userValid5 =
            getUsers("USER13", "leid13", "uid13");

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> validUsers = asList(userValid1, userValid2, userValid3,
            userValid4,
            userValid5);

        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> externalIdToUserMap = new HashMap<>();
        validUsers.forEach(user -> externalIdToUserMap.put(user.getExternalId(), user));

        List<AssignUserPermissionsData> assignUserPermissionsData = asList(
            new AssignUserPermissionsData(copyUserPermissionWithUpperCase(requestValid1), externalIdToUserMap),
            new AssignUserPermissionsData(copyUserPermissionWithUpperCase(requestValid2), externalIdToUserMap),
            new AssignUserPermissionsData(copyUserPermissionWithUpperCase(requestValid3), externalIdToUserMap),
            new AssignUserPermissionsData(copyUserPermissionWithUpperCase(requestValid4), externalIdToUserMap),
            new AssignUserPermissionsData(copyUserPermissionWithUpperCase(requestValid5), externalIdToUserMap)
        );

        List<ResponseItemExtended> responseFromPandP = Lists.newArrayList(
            new ResponseItemExtended("uid9", "sa9", ItemStatusCode.HTTP_STATUS_OK, null, emptyList()),
            new ResponseItemExtended("uid10", "sa10", ItemStatusCode.HTTP_STATUS_OK, null, emptyList()),
            new ResponseItemExtended("uid11", "sa11", ItemStatusCode.HTTP_STATUS_OK, null, emptyList()),
            new ResponseItemExtended("uid12", "sa12", ItemStatusCode.HTTP_STATUS_OK, null, emptyList()),
            new ResponseItemExtended("uid13", "sa13", ItemStatusCode.HTTP_STATUS_OK, null, emptyList()));

        when(userManagementService
            .getUsersByExternalIds(
                refEq(Lists.newArrayList("USER5", "USER9", "КОРИСНИК10", "X Æ A-12", "USER12", "USER13"))))
            .thenAnswer(ans -> validUsers);
        when(userPermissionBatchUpdate.processBatchItems(eq(assignUserPermissionsData))).thenReturn(responseFromPandP);

        List<BatchResponseItemExtended> responses = assignUserPermissions
            .saveBulkUserPermissions(Lists.newArrayList(requestValid1,
                requestNoUserPermissions, requestValid2, requestValid3,
                requestWithBothIdentifiersInFG, requestWithNoIdentifiersInFG, requestWithNotExistingUser,
                requestWithNotMatchingSAInDG, requestWithBothIdentifiersInDG, requestWithNoIdentifiersInDG,
                requestValid4, requestValid5));

        assertEquals(12, responses.size());
        assertEquals(responses.get(0), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("user9")
            .withExternalServiceAgreementId("sa9"));
        assertEquals(responses.get(1), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user1")
            .withExternalServiceAgreementId("sa1"));
        assertEquals(responses.get(2), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("корисник10")
            .withExternalServiceAgreementId("sa10"));
        assertEquals(responses.get(3), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("x æ a-12")
            .withExternalServiceAgreementId("sa11"));
        assertEquals(responses.get(4), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user3")
            .withExternalServiceAgreementId("sa3"));
        assertEquals(responses.get(5), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user4")
            .withExternalServiceAgreementId("sa4"));
        assertEquals(responses.get(6), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_082.getErrorMessage()))
            .withResourceId("user5")
            .withExternalServiceAgreementId("sa5"));
        assertEquals(responses.get(7), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user6")
            .withExternalServiceAgreementId("sa6"));
        assertEquals(responses.get(8), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user7")
            .withExternalServiceAgreementId("sa7"));
        assertEquals(responses.get(9), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user8")
            .withExternalServiceAgreementId("sa8"));
        assertEquals(responses.get(10), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("user12")
            .withExternalServiceAgreementId("sa12"));
        assertEquals(responses.get(11), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("user13")
            .withExternalServiceAgreementId("sa13"));
    }

    @Test
    public void shouldNotCallPersistenceForAllInvalidUpdatePermissionItems() {
        PresentationAssignUserPermissions requestWithBothIdentifiersInFG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa1")
            .withExternalUserId("user1")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier("fgId1")
                    .withNameIdentifier(new NameIdentifier()
                        .withName("fgNme1")
                        .withExternalServiceAgreementId("sa1")))));

        List<BatchResponseItemExtended> response = assignUserPermissions
            .saveBulkUserPermissions(Lists.newArrayList(requestWithBothIdentifiersInFG));

        verify(userManagementService, never())
            .getUsersByExternalIds(any());
        verify(userPermissionBatchUpdate, never()).processBatchItems(any(List.class));

        assertEquals(response.get(0), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user1")
            .withExternalServiceAgreementId("sa1"));
    }

    @Test
    public void shouldNotCallPersistenceForNullUpdatePermissionItems() {
        PresentationAssignUserPermissions requestWithBothIdentifiersInFG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa1")
            .withExternalUserId("user1")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(null)
                    .withNameIdentifier(null))));

        List<BatchResponseItemExtended> response = assignUserPermissions
            .saveBulkUserPermissions(Lists.newArrayList(requestWithBothIdentifiersInFG));

        verify(userManagementService, never())
            .getUsersByExternalIds(any());
        verify(userPermissionBatchUpdate, never()).processBatchItems(any(List.class));

        assertEquals(response.get(0), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user1")
            .withExternalServiceAgreementId("sa1"));
    }

    @Test
    public void shouldNotCallPersistenceAndUsersForAllInvalidUpdatePermissionItems() {

        List<PresentationFunctionGroupDataGroup> incorrectList = new ArrayList<>();
        incorrectList.add(null);

        PresentationAssignUserPermissions requestNoUserPermissions = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa1")
            .withExternalUserId("user1")
            .withFunctionGroupDataGroups(incorrectList);
        PresentationAssignUserPermissions requestWithBothIdentifiersInFG = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa3")
            .withExternalUserId("user3")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier("fgId1")
                    .withNameIdentifier(new NameIdentifier()
                        .withName("fgNme4")
                        .withExternalServiceAgreementId("sa3")))));
        PresentationAssignUserPermissions missingUserIdRequest = new PresentationAssignUserPermissions()
            .withExternalServiceAgreementId("sa9")
            .withExternalUserId("null")
            .withFunctionGroupDataGroups(Lists.newArrayList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier("fgId9"))
                .withDataGroupIdentifiers(Lists.newArrayList(new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier()
                        .withExternalServiceAgreementId("sa9")
                        .withName("dgName9"))))));

        List<BatchResponseItemExtended> responses = assignUserPermissions
            .saveBulkUserPermissions(Lists.newArrayList(missingUserIdRequest,
                requestNoUserPermissions,
                requestWithBothIdentifiersInFG));

        assertEquals(3, responses.size());
        assertEquals(responses.get(0), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_082.getErrorMessage()))
            .withResourceId("null")
            .withExternalServiceAgreementId("sa9"));
        assertEquals(responses.get(1), new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
            .withResourceId("user1")
            .withExternalServiceAgreementId("sa1"));
    }

    private PresentationAssignUserPermissions copyUserPermissionWithUpperCase(
        PresentationAssignUserPermissions userPermission) {
        PresentationAssignUserPermissions presentationAssignUserPermissions = new PresentationAssignUserPermissions();
        presentationAssignUserPermissions.setExternalUserId(userPermission.getExternalUserId().toUpperCase());
        presentationAssignUserPermissions.setExternalServiceAgreementId(userPermission.getExternalServiceAgreementId());
        presentationAssignUserPermissions.setFunctionGroupDataGroups(userPermission.getFunctionGroupDataGroups());
        presentationAssignUserPermissions.setAdditions(userPermission.getAdditions());
        return presentationAssignUserPermissions;
    }

    private com.backbase.dbs.user.api.client.v2.model.GetUser getUsers(String externalId, String legalEntityId,
        String id) {
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(id);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(externalId);
        return user1;
    }
}
