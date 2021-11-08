package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_025;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalTypeDto;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
    "backbase.approval.validation.enabled=true",
    "backbase.approval.level.enabled=true"
}
)
public class UpdateFunctionGroupWithApprovalIT extends TestDbWireMock {

    private static final String FUNCTION_GROUP_URL = "/accessgroups/function-groups/{id}";

    private static final String POST_APPROVAL_URL = baseServiceUrl + "/approvals";

    private static final String APPROVAL_TYPE_ASSIGNMENT = baseServiceUrl
        + "/approval-type-assignments/{id}";

    private FunctionGroup functionGroup;

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

        functionGroup = createFunctionGroup("fg-name", "fg-description", rootMsa,
            Collections.singletonList(businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view")));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldUpdateFunctionGroupWithApprovalOn() throws Exception {

        String serviceAgreementId = rootMsa.getId();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        ApprovalDto approval = getApprovalDto(getUuid(), serviceAgreementId, ApprovalStatus.PENDING);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId()).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT);
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldUpdateFunctionGroupWithApprovalOnZeroPolicy() throws Exception {

        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);
        addStubGet(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(),
            new GetApprovalTypeResponse().approvalType(new ApprovalTypeDto().id(approvalId)),
            200);
        addStubDelete(new UrlBuilder(APPROVAL_TYPE_ASSIGNMENT).addPathParameter(functionGroupId).build(), null, 200);

        executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT);
    }

    @Test
    public void shouldThrowBadRequestIfThereIsOtherFunctionGroupWithSameName() {
        createFunctionGroup("conflicting-name", "fg-description", rootMsa,
            Collections.singletonList(businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view")));

        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        functionGroupByIdPutRequestBody.setName("conflicting-name");

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundIfFunctionGroupDoesNotExist() {
        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = getUuid();
        String approvalId = getUuid();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfServiceAgreementIdIsNotTheSame() {
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", rootLegalEntity, null, null);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreement.getId());

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreement.getId(), ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_025.getErrorMessage(), ERR_ACQ_025.getErrorCode()));
    }


    @Test
    public void shouldThrowBadRequestIfStartDateIsBeforeServiceAgreementStartDate() {
        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        rootMsa.setStartDate(startDate);
        serviceAgreementJpaRepository.save(rootMsa);

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        functionGroupByIdPutRequestBody.setValidFromDate("2018-01-01");
        functionGroupByIdPutRequestBody.setValidFromTime("01:00:00");

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfEndDateIsAfterServiceAgreementEndDate() {
        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        rootMsa.setEndDate(endDate);
        serviceAgreementJpaRepository.save(rootMsa);

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        functionGroupByIdPutRequestBody.setValidUntilDate("2200-01-01");
        functionGroupByIdPutRequestBody.setValidUntilTime("01:00:00");

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionDoesNotExist() {
        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        functionGroupByIdPutRequestBody.setPermissions(Collections.singletonList(
            new Permission().withFunctionId("invalid-BF-id")
                .withAssignedPrivileges(Collections.singletonList(new Privilege().withPrivilege("create")))));

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfPrivilegeDoesNotExist() {
        String serviceAgreementId = rootMsa.getId();
        String functionGroupId = functionGroup.getId();
        String approvalId = getUuid();

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody =
            getFunctionGroupByIdPutRequestBody(serviceAgreementId);
        functionGroupByIdPutRequestBody.setPermissions(Collections.singletonList(
            new Permission().withFunctionId("1020")
                .withAssignedPrivileges(Collections.singletonList(new Privilege().withPrivilege("invalid")))));

        ApprovalDto approval = getApprovalDto(approvalId, serviceAgreementId, ApprovalStatus.APPROVED);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        addStubPost(POST_APPROVAL_URL, presentationPostApprovalResponse, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroupId).build(), HttpMethod.PUT,
            functionGroupByIdPutRequestBody, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    private FunctionGroupByIdPutRequestBody getFunctionGroupByIdPutRequestBody(String saId) {
        String functionGroupName = "Name";
        String description = "Test Description";
        String functionId = "1020";

        List<Permission> permissions = Collections.singletonList(
            new Permission().withFunctionId(functionId)
                .withAssignedPrivileges(
                    Arrays.asList(new Privilege().withPrivilege("view"),
                        new Privilege().withPrivilege("edit"))));

        return new FunctionGroupByIdPutRequestBody()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(saId)
            .withPermissions(permissions);
    }

    private ApprovalDto getApprovalDto(String approvalId, String sa, ApprovalStatus status) {
        return new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId(sa)
            .status(status)
            .itemId(getUuid())
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
    }

    public FunctionGroup createFunctionGroup(String name, String description,
        ServiceAgreement serviceAgreement, List<ApplicableFunctionPrivilege> applicableFunctionPrivileges) {
        final FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, name, description, new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList = GroupedFunctionPrivilegeUtil
            .getGroupedFunctionPrivileges(
                applicableFunctionPrivileges.stream()
                    .map(applicableFunctionPrivilege -> getGroupedFunctionPrivilege(null, applicableFunctionPrivilege,
                        functionGroup))
                    .toArray(GroupedFunctionPrivilege[]::new)
            );

        functionGroup.setPermissions(groupedFunctionPrivilegeList);

        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.flush();

        return savedFunctionGroup;
    }

}
