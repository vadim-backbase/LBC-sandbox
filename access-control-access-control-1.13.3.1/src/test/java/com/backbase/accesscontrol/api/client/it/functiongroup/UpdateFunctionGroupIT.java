package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalTypeDto;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.api.client.v2.model.PutApprovalTypeAssignmentRequest;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link FunctionGroupsController#putFunctionGroupById}
 */
@TestPropertySource(properties = {
    "backbase.approval.level.enabled=true"
}
)
public class UpdateFunctionGroupIT extends TestDbWireMock {

    private static final String FUNCTION_GROUP_URL = "/accessgroups/function-groups/{id}";

    private static final String APPROVAL_TYPE_ASSIGNMENT = baseServiceUrl
        + "/approval-type-assignments/{id}";
    private static final String APPROVAL_TYPE_ASSIGNMENT_CREATE = baseServiceUrl
        + "/approval-type-assignments/bulk";

    private FunctionGroup functionGroup;
    private FunctionGroup functionGroupCsa;
    private ServiceAgreement csa;
    private UserContext userContext;
    private LegalEntity leInCsa;

    @Before
    public void setUp() {
        AssignablePermissionSet assignablePermissionSetRegularUpdate = createAssignablePermissionSet(
            "apsRegularUpdate1",
            AssignablePermissionType.CUSTOM,
            "apsRegularIngestUpdate1",
            businessFunctionCache.getByFunctionIdAndPrivilege("1020", "edit").getId(),
            businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view").getId()
        );
        assignablePermissionSetRegularUpdate = assignablePermissionSetJpaRepository
            .save(assignablePermissionSetRegularUpdate);

        rootMsa.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularUpdate));
        rootMsa = serviceAgreementJpaRepository.save(rootMsa);

        List<ApplicableFunctionPrivilege> afp = singletonList(
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view"));

        functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "fg-name", "fg-description", new HashSet<>(), FunctionGroupType.DEFAULT, rootMsa);

        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList = GroupedFunctionPrivilegeUtil
            .getGroupedFunctionPrivileges(
                afp.stream()
                    .map(applicableFunctionPrivilege -> getGroupedFunctionPrivilege(null, applicableFunctionPrivilege,
                        functionGroup))
                    .toArray(GroupedFunctionPrivilege[]::new)
            );

        functionGroup.setPermissions(groupedFunctionPrivilegeList);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        leInCsa = LegalEntityUtil
            .createLegalEntity(null, "leInSa", "leInSa", null, LegalEntityType.CUSTOMER);
        leInCsa = legalEntityJpaRepository.save(leInCsa);

        csa = ServiceAgreementUtil
            .createServiceAgreement("sa1", "sa1", "", leInCsa, leInCsa.getId(), leInCsa.getId());
        csa.setMaster(false);
        csa.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularUpdate));
        csa = serviceAgreementJpaRepository.save(csa);

        functionGroupCsa = FunctionGroupUtil
            .getFunctionGroup(null, "fg-name-csa", "fg-description-csa", new HashSet<>(), FunctionGroupType.DEFAULT,
                csa);
        functionGroupCsa = functionGroupJpaRepository.save(functionGroupCsa);

        userContext = userContextJpaRepository.save(new UserContext(getUuid(), csa.getId()));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void testSuccessfulUpdateFunctionGroupUnderMSAWithDeleteApprovalType() throws Exception {
        String description = "Test Description";
        String functionGroupId = functionGroup.getId();
        String functionGroupName = "Name";
        String newApprovalTypeId = null;
        String oldApprovalTypeId = getUuid();

        List<Permission> permissions = singletonList(new Permission().withFunctionId("1020")
            .withAssignedPrivileges(singletonList(new Privilege().withPrivilege("view"))));
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions)
            .withApprovalTypeId(newApprovalTypeId);

        addStubGet(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(),
            new GetApprovalTypeResponse().approvalType(new ApprovalTypeDto().id(oldApprovalTypeId)), 200);
        addStubDelete(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(), null, 200);

        String requestAsString = objectMapper.writeValueAsString(putData);

        executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId()).build(), HttpMethod.PUT,
            requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT);
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(UPDATE)
            .withId(functionGroup.getId())));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void testSuccessfulUpdateFunctionGroupUnderMSAWithCreateApprovalType() throws Exception {
        String description = "Test Description";
        String functionGroupId = functionGroup.getId();
        String serviceAgreementId = rootMsa.getId();
        String functionGroupName = "Name";
        String newApprovalTypeId = getUuid();
        String oldApprovalTypeId = null;

        List<Permission> permissions = singletonList(new Permission().withFunctionId("1020")
            .withAssignedPrivileges(singletonList(new Privilege().withPrivilege("view"))));
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(permissions)
            .withApprovalTypeId(newApprovalTypeId);

        addStubGet(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(),
            new GetApprovalTypeResponse().approvalType(new ApprovalTypeDto().id(oldApprovalTypeId)), 200);

        PresentationPostBulkApprovalTypeAssignmentRequest approval = new PresentationPostBulkApprovalTypeAssignmentRequest()
            .approvalTypeAssignments(singletonList(
                new PresentationApprovalTypeAssignmentDto()
                    .jobProfileId(functionGroupId)
                    .approvalTypeId(newApprovalTypeId)));

        addStubPostEqualToJson(APPROVAL_TYPE_ASSIGNMENT_CREATE, null, 200, approval);

        String requestAsString = objectMapper.writeValueAsString(putData);

        executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId()).build(), HttpMethod.PUT,
            requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT);
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(UPDATE)
            .withId(functionGroup.getId())));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void testSuccessfulCreateFunctionGroupUnderCSAWithUpdateApprovalType() throws Exception {
        String functionGroupName = "Name";
        String description = "Test Description";
        String functionGroupId = functionGroupCsa.getId();
        String serviceAgreementId = csa.getId();
        String oldApprovalTypeId = getUuid();
        String newApprovalTypeId = getUuid();

        List<Permission> permissions = singletonList(new Permission().withFunctionId("1020")
            .withAssignedPrivileges(singletonList(new Privilege().withPrivilege("view"))));
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(permissions)
            .withApprovalTypeId(newApprovalTypeId);

        addStubGet(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(),
            new GetApprovalTypeResponse().approvalType(new ApprovalTypeDto().id(oldApprovalTypeId)), 200);

        PutApprovalTypeAssignmentRequest approval = new PutApprovalTypeAssignmentRequest()
            .approvalTypeId(newApprovalTypeId);
        addStubPutEqualToJson(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(), null,
            200, approval);

        String requestAsString = objectMapper.writeValueAsString(putData);

        executeClientRequestWithContext(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            requestAsString, "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, leInCsa.getId());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(UPDATE)
            .withId(functionGroupId)));
    }
}
