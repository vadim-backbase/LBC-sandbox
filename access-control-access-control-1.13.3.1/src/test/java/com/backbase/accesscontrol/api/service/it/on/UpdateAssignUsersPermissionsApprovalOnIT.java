package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_072;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class UpdateAssignUsersPermissionsApprovalOnIT extends TestDbWireMock {

    private static final String SERVICE_AGREEMENT = "Service Agreement";
    private static final String ASSIGN_PERMISSIONS = "Assign Permissions";
    private static final String URL = "/accessgroups/service-agreements/{id}/users/{userId}/permissions";
    private static final String URL_USERS = "/service-api/v2/users/{id}";
    private static final String getApprovalUrl = "/service-api/v2/approvals";

    private final String userId = contextUserId;

    private DataGroup dataGroup01;
    private DataGroup dataGroup02;
    private DataGroup dataGroup03;
    private DataGroup dataGroup04;
    private DataGroup dataGroup05;

    private FunctionGroup functionGroup01;
    private FunctionGroup functionGroup02;
    private FunctionGroup functionGroup03;
    private FunctionGroup functionGroup04;
    private FunctionGroup functionGroup05;

    @Before
    public void setup(){
        dataGroup01 = createDataGroup("dag01", "ARRANGEMENTS", "dag01", rootMsa);
        dataGroup01 = dataGroupJpaRepository.save(dataGroup01);

        dataGroup02 = createDataGroup("dag02", "ARRANGEMENTS", "dag02", rootMsa);
        dataGroup02 = dataGroupJpaRepository.save(dataGroup02);

        dataGroup03 = createDataGroup("dag03", "ARRANGEMENTS", "dag03", rootMsa);
        dataGroup03 = dataGroupJpaRepository.save(dataGroup03);

        dataGroup04 = createDataGroup("dag04", "ARRANGEMENTS", "dag04", rootMsa);
        dataGroup04 = dataGroupJpaRepository.save(dataGroup04);

        dataGroup05 = createDataGroup("dag05", "ARRANGEMENTS", "dag05", rootMsa);
        dataGroup05 = dataGroupJpaRepository.save(dataGroup05);

        functionGroup01 = createFunctionGroup("fg01", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup02 = createFunctionGroup("fg02", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup03 = createFunctionGroup("fg03", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup04 = createFunctionGroup("fg04", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup05 = createFunctionGroup("fg05", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);

        UserContext userContext = new UserContext(userId, rootMsa.getId());

        UserAssignedFunctionGroup uaFg01 = new UserAssignedFunctionGroup(functionGroup01, userContext);
        uaFg01.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), uaFg01)));

        UserAssignedFunctionGroup uaFg03 = new UserAssignedFunctionGroup(functionGroup03, userContext);
        uaFg03.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), uaFg03)));

        UserAssignedFunctionGroup uaFg04 = new UserAssignedFunctionGroup(functionGroup04, userContext);
        uaFg04.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup04.getId()), uaFg04),
                new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup05.getId()), uaFg04)));

        UserAssignedFunctionGroup uaFg05 = new UserAssignedFunctionGroup(functionGroup05, userContext);
        uaFg05.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), uaFg05)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg01, uaFg03, uaFg04, uaFg05));
        userContextJpaRepository.save(userContext);
    }

    @Test
    public void shouldInvokePersistenceEndpointOverServiceOnPending() throws IOException, JSONException {
        ApprovalDto approval = new ApprovalDto()
            .id(UUID.randomUUID().toString())
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .itemId(UUID.randomUUID().toString())
            .userId(userId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.PENDING);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(rootLegalEntity.getId());
        user.setId(userId);
        user.setExternalId("username");
        user.setFullName("userFullName");

        PresentationFunctionDataGroup item11 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup01.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup01.getId())));

        PresentationFunctionDataGroup item123 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup01.getId())
            .withDataGroupIds(asList(new PresentationGenericObjectId().withId(dataGroup02.getId()), new PresentationGenericObjectId().withId(dataGroup03.getId())));

        PresentationFunctionDataGroup item24 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup02.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup04.getId())));

        PresentationFunctionDataGroup item25 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup02.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup05.getId())));

        PresentationFunctionDataGroup item32 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup03.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup02.getId())));

        PresentationFunctionDataGroup item44 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup04.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup04.getId())));

        PresentationFunctionDataGroupItems putData = new PresentationFunctionDataGroupItems()
            .withItems(asList(item11, item123, item24, item25, item32, item44));

        addStubGet(new UrlBuilder(URL_USERS).addPathParameter(userId)
            .addQueryParameter("skipHierarchyCheck","true").build(), user, 200);
        addStubPost(new UrlBuilder(getApprovalUrl).build(), approvalResponse, 200);

        String jsonResponse = executeServiceRequest(
            new UrlBuilder(URL).addPathParameter(rootMsa.getId()).addPathParameter(userId).build(), putData, userId,
            rootMsa.getId(), HttpMethod.PUT);

        PresentationApprovalStatus status = readValue(jsonResponse, PresentationApprovalStatus.class);

        assertEquals(ApprovalStatus.PENDING, status.getApprovalStatus());

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            List<ApprovalUserContextAssignFunctionGroup> pendingPermissions = approvalUserContextAssignFunctionGroupJpaRepository
                .findAll();

            assertThat(pendingPermissions, containsInAnyOrder(
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup01.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup02.getId(), dataGroup03.getId()))),
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup01.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup01.getId()))),
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup02.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup04.getId()))),
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup02.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup05.getId()))),
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup03.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup02.getId()))),
                allOf(
                    hasProperty("functionGroupId", equalTo(functionGroup04.getId())),
                    hasProperty("dataGroups", containsInAnyOrder(dataGroup04.getId())))
            ));

            return true;
        });
    }

    @Test
    public void shouldInvokePersistenceEndpointOverServiceOnApproved() throws IOException, JSONException {
        ApprovalDto approval = new ApprovalDto()
            .id(UUID.randomUUID().toString())
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .itemId(UUID.randomUUID().toString())
            .userId(userId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(rootLegalEntity.getId());
        user.setId(userId);
        user.setExternalId("username");
        user.setFullName("userFullName");

        FunctionGroup functionGroup = createFunctionGroup("fg", "desc", rootMsa, new ArrayList<>(),
            FunctionGroupType.DEFAULT);
        PresentationFunctionDataGroup item = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup.getId());
        PresentationFunctionDataGroupItems putData = new PresentationFunctionDataGroupItems()
            .withItems(Collections.singletonList(item));
        addStubGet(new UrlBuilder(URL_USERS).addPathParameter(userId)
            .addQueryParameter("skipHierarchyCheck","true").build(), user, 200);
        addStubPost(
            new UrlBuilder(getApprovalUrl)
                .build(), approvalResponse, 200);
        String jsonResponse = executeServiceRequest(
            new UrlBuilder(URL).addPathParameter(rootMsa.getId()).addPathParameter(userId).build(), putData, userId,
            rootMsa.getId(), HttpMethod.PUT);
        PresentationApprovalStatus status = readValue(
            jsonResponse, PresentationApprovalStatus.class);

        assertEquals(ApprovalStatus.APPROVED, status.getApprovalStatus());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(rootMsa.getId())
            .withUserId(userId)));
    }

    @Test
    public void shouldThrowBadRequestIfValidationFailsAndCancelApprovalRequest() {
        String approvalId = UUID.randomUUID().toString();
        String getApprovalStatusUrl = "/service-api/v2/approvals/" + approvalId + "/status";

        ApprovalUserContext approvalUserContext = new ApprovalUserContext().withUserId(userId)
            .withLegalEntityId(rootLegalEntity.getId())
            .withServiceAgreementId(rootMsa.getId())
            .withApprovalId(approvalId);
        approvalUserContextJpaRepository.save(approvalUserContext);
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .function(ASSIGN_PERMISSIONS)
            .resource(SERVICE_AGREEMENT)
            .itemId(UUID.randomUUID().toString())
            .userId(userId)
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.CANCELLED);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(rootLegalEntity.getId());
        user.setId(userId);
        user.setExternalId("username");
        user.setFullName("userFullName");

        FunctionGroup functionGroup = createFunctionGroup("fg", "desc", rootMsa, new ArrayList<>(),
            FunctionGroupType.DEFAULT);
        PresentationFunctionDataGroup item = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup.getId());
        PresentationFunctionDataGroupItems putData = new PresentationFunctionDataGroupItems()
            .withItems(Collections.singletonList(item));
        addStubGet(new UrlBuilder(URL_USERS).addPathParameter(userId)
            .addQueryParameter("skipHierarchyCheck","true").build(), user, 200);
        addStubPost(
            new UrlBuilder(getApprovalUrl)
                .build(), approvalResponse, 200);
        PutUpdateStatusRequest persistencePutUpdateStatusRequest = new PutUpdateStatusRequest()
                .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.CANCELLED);
        addStubPutContains(new UrlBuilder(getApprovalStatusUrl).build(), "", 200, persistencePutUpdateStatusRequest);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(
                new UrlBuilder(URL).addPathParameter(rootMsa.getId()).addPathParameter(userId).build(), putData, userId,
                rootMsa.getId(), HttpMethod.PUT));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_ACC_072.getErrorMessage(), ERR_ACC_072.getErrorCode())));
    }
}