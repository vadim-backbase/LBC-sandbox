package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_100;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.TestCase.assertEquals;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link FunctionGroupsController#postFunctionGroups
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true"}
)
public class CreateFunctionGroupWithApprovalIT extends TestDbWireMock {

    private String url = "/accessgroups/function-groups";
    private static final String postApprovalsUrl = baseServiceUrl
        + "/approvals";
    private static final String getApprovalStatusUrl = "/service-api/v2/approvals/{approvalId}/status";
    private BusinessFunction bf1028;
    private ApplicableFunctionPrivilege apfBf1028View;

    @Before
    public void setUp() throws Exception {
        apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        bf1028 = apfBf1028View.getBusinessFunction();

        AssignablePermissionSet aps = createAssignablePermissionSet(
            "apsRegularCreate",
            AssignablePermissionType.CUSTOM,
            "apsRegularCreate",
            apfBf1028View.getId()
        );
        aps = assignablePermissionSetJpaRepository.save(aps);

        rootMsa.setPermissionSetsRegular(newHashSet(aps));
        rootMsa = serviceAgreementJpaRepository.save(rootMsa);
    }

    @Test
    public void shouldCreateApprovalFunctionGroup() throws Exception {

        String functionGroupName = "Name";
        String description = "Test Description";
        String approvalId = "1235e686d31e4216b3dd5d66161d0001";
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = asList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(asList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions);
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("CREATE");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 201);

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        Optional<List<ApprovalFunctionGroup>> pending = approvalFunctionGroupJpaRepository
            .findByServiceAgreementId(rootMsa.getId());
        assertFalse(fg.isPresent());
        assertTrue(pending.isPresent());
        assertEquals(responseId, pending.get().get(0).getApprovalId());

    }

    @Test
    public void shouldCreateFunctionGroupWithApprovalOnAndZeroPolicy() throws Exception {
        ApprovalFunctionGroup functionGroupApprovalBase = new ApprovalFunctionGroup();
        functionGroupApprovalBase.setApprovalId("1235e686d31e4216b3dd5d66161d526d");
        functionGroupApprovalBase.setPrivileges(Sets.newHashSet("create"));
        functionGroupApprovalBase.setDescription("desc.fg1");
        functionGroupApprovalBase.setName("fg-name1");
        functionGroupApprovalBase.setServiceAgreementId(rootMsa.getId());
        approvalFunctionGroupJpaRepository.save(functionGroupApprovalBase);

        String functionGroupName = "Name";
        String description = "Test Description";
        String approvalId = "1235e686d31e4216b3dd5d66161d0001";
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = asList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(asList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions);
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.APPROVED)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("CREATE");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 201);

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());
    }


    @Test
    public void shouldThrowBadRequestIfFunctionGroupNameIsNotUniqueInTempTables() throws Exception {

        String functionGroupName = "Name";
        String description = "Test Description";
        String approvalId = "1235e686d31e4216b3dd5d66161d0001";
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = asList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(asList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions);
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(rootMsa.getId())
            .status(ApprovalStatus.PENDING)
            .itemId(UUID.randomUUID().toString())
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("CREATE");
        PresentationPostApprovalResponse presentationPostApprovalResponse =
            new PresentationPostApprovalResponse().approval(approval);
        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 201);
        addStubPut(new UrlBuilder(getApprovalStatusUrl).addPathParameter(approvalId).build(),
            presentationPostApprovalResponse, 200);

        executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_100.getErrorMessage(), ERR_ACC_100.getErrorCode()));
    }
}