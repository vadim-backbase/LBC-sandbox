package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_073;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalTypeDto;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link FunctionGroupsController#deleteFunctionGroupById}
 */
@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
}
)
public class DeleteFunctionGroupWithApprovalIT extends TestDbWireMock {

    private static final String functionGroupURL = "/accessgroups/function-groups/{id}";

    private static final String postApprovalsUrl = baseServiceUrl
        + "/approvals";

    private static final String approvalTypeAssignment = baseServiceUrl
        + "/approval-type-assignments/{id}";

    private String serviceAgreementId;
    private String functionGroupId;
    private ApplicableFunctionPrivilege apfBf1002Create;

    @Before
    public void setup() {

        LegalEntity legalEntity = rootLegalEntity;

        apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "view");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDescription("Service Agreement");
        serviceAgreement.setName("SA Name");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1002Create, null));

        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.DEFAULT, serviceAgreement);

        functionGroup = functionGroupJpaRepository.saveAndFlush(functionGroup);

        functionGroupId = functionGroup.getId();
        serviceAgreementId = serviceAgreement.getId();
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldDeleteFunctionGroupWithApprovalOn() throws Exception {

        ApprovalDto approval = getApprovalDto(serviceAgreementId, ApprovalStatus.PENDING);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 200);

        executeClientRequest(
            new UrlBuilder(functionGroupURL).addPathParameter(functionGroupId).build()
            , HttpMethod.DELETE
            , "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE);
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldDeleteFunctionGroupWithZeroPolicy() throws Exception {

        ApprovalDto approval = getApprovalDto(serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 200);
        addStubGet(new UrlBuilder(approvalTypeAssignment).addPathParameter(functionGroupId).build(),
            new GetApprovalTypeResponse().approvalType(new ApprovalTypeDto().id("type A")),
            200);
        addStubDelete(new UrlBuilder(approvalTypeAssignment).addPathParameter(functionGroupId).build(), null, 200);

        executeClientRequest(
            new UrlBuilder(functionGroupURL).addPathParameter(functionGroupId).build()
            , HttpMethod.DELETE
            , "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE);
    }

    @Test
    public void shouldThrowBadRequestIfThereIsPendingAssignmentForFunctionGroup() {
        createPendingRequestForFunctionGroup();
        ApprovalDto approval = getApprovalDto(serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        addStubPost(postApprovalsUrl, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> {
                String build = new UrlBuilder(functionGroupURL)
                    .addPathParameter(functionGroupId)
                    .build();
                executeClientRequestEntity(build,
                    HttpMethod.DELETE, "", "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE);
            });

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_073.getErrorMessage(), ERR_ACC_073.getErrorCode()));
    }

    private void createPendingRequestForFunctionGroup() {

        ApprovalUserContext approvalUserContext = new ApprovalUserContext(UUID.randomUUID().toString(),
            rootMsa.getId(),
            rootLegalEntity.getId(), new HashSet<>());
        approvalUserContext.setApprovalId("1");
        approvalUserContext = approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup = new ApprovalUserContextAssignFunctionGroup(
            null, functionGroupId,
            approvalUserContext, new HashSet<>(), null);
        approvalUserContextAssignFunctionGroupJpaRepository.save(approvalUserContextAssignFunctionGroup);
    }


    private ApprovalDto getApprovalDto(String sa, ApprovalStatus status) {
        return new ApprovalDto()
            .id(getUuid())
            .userId("user")
            .serviceAgreementId(sa)
            .status(status)
            .itemId(getUuid())
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
    }

}
