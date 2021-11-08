package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.FunctionGroupsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link FunctionGroupsController#postFunctionGroups
 */
@TestPropertySource(properties = "backbase.approval.level.enabled=true")
public class CreateFunctionGroupIT extends TestDbWireMock {

    private String url = "/accessgroups/function-groups";
    private static final String postBulkApprovalTypeAssignments = baseServiceUrl
        + "/approval-type-assignments/bulk";
    private ServiceAgreement serviceAgreementWithAps;
    private LegalEntity legalEntityAps;
    private BusinessFunction bf1028;
    private ApplicableFunctionPrivilege apfBf1028View;
    private UserContext userContext;

    @Before
    public void setUp() {

        apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        bf1028 = apfBf1028View.getBusinessFunction();

        AssignablePermissionSet aps = createAssignablePermissionSet(
            "apsRegularCreate",
            AssignablePermissionType.CUSTOM,
            "apsRegularCreate",
            apfBf1028View.getId()
        );
        aps = assignablePermissionSetJpaRepository.save(aps);

        legalEntityAps = LegalEntityUtil
            .createLegalEntity(null, "le-nameaps", "ex-id1aps", null, LegalEntityType.CUSTOMER);
        legalEntityAps = legalEntityJpaRepository.save(legalEntityAps);

        serviceAgreementWithAps = ServiceAgreementUtil
            .createServiceAgreement("name.saaps", "exid.saaps", "desc.saaps", legalEntityAps, legalEntityAps.getId(),
                legalEntityAps.getId());
        serviceAgreementWithAps.setMaster(false);
        serviceAgreementWithAps.setPermissionSetsRegular(newHashSet(aps));
        serviceAgreementWithAps = serviceAgreementJpaRepository.save(serviceAgreementWithAps);

        rootMsa.setPermissionSetsRegular(newHashSet(aps));
        serviceAgreementJpaRepository.save(rootMsa);
        userContext = new UserContext("user", serviceAgreementWithAps.getId());
        userContext = userContextJpaRepository.save(userContext);
    }

    @Test
    public void testSuccessfulCreateFunctionGroupUnderMSA() throws Exception {

        String functionGroupName = "Name";
        String description = "Test Description";
        String serviceAgreementId = rootMsa.getId();
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(fg.get().getId())));
    }

    @Test
    public void testSuccessfulCreateFunctionGroupUnderMSAWithApprovalTypeId() throws Exception {

        String functionGroupName = "Name";
        String description = "Test Description";
        String serviceAgreementId = rootMsa.getId();
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(permissions)
            .withApprovalTypeId(newApprovalTypeId);

        addStubPost(postBulkApprovalTypeAssignments, null, 207);

        String responseAsString = executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(fg.get().getId())));
    }

    @Test
    public void testSuccessfulCreateFunctionGroupUnderCSA() throws Exception {

        String functionGroupName = "Name";
        String description = "Test Description";
        String serviceAgreementId = serviceAgreementWithAps.getId();
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));

        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription(description)
            .withName(functionGroupName)
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(permissions);

        String valueAsString = objectMapper.writeValueAsString(postData);
        String responseAsString = executeClientRequestWithContext(url, HttpMethod.POST, valueAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE,
            userContext, legalEntityAps.getId());
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(fg.get().getId())));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionsNotInAps() {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId("32131")
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc.fg")
            .withName("fg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    public void shouldThrowForbiddenExceptionIfServiceAgreementDoesNotExist() {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc.fg")
            .withName("fg-name")
            .withServiceAgreementId(getUuid())
            .withPermissions(permissions);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfFunctionGroupNameIsNotUnique() throws Exception {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));

        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc.fg")
            .withName("fg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions);

        executeClientRequest(url, HttpMethod.POST, postData, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionDoesNotExist() {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());

        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc.fg")
            .withName("fg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(singletonList(
                new Permission().withFunctionId("does-not-exist")
                    .withAssignedPrivileges(singletonList(view))
            ));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionHasStartDateBeforeServiceAgreementStartDate() {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());
        String validFromDate = "2020-07-07";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        rootMsa.setStartDate(startDate);
        serviceAgreementJpaRepository.save(rootMsa);
        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc")
            .withName("functionGroupName")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions)
            .withValidFromDate(validFromDate);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionDoesHasEndDateAfterServiceAgreementEndDate() {
        Privilege view = new Privilege().withPrivilege(apfBf1028View.getPrivilege().getName());
        String validUntilDate = "2200-01-01";

        LocalDateTime localDateTIme = java.time.LocalDateTime.now();
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        rootMsa.setEndDate(endDate);
        serviceAgreementJpaRepository.save(rootMsa);
        List<Permission> permissions = singletonList(new Permission().withFunctionId(bf1028.getId())
            .withAssignedPrivileges(singletonList(view)));
        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc")
            .withName("functionGroupName")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(permissions)
            .withValidUntilDate(validUntilDate);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfPrivilegeDoesNotExist() {
        Privilege invalid = new Privilege().withPrivilege("invalid");

        FunctionGroupBase postData = new FunctionGroupBase()
            .withDescription("desc.fg")
            .withName("fg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withPermissions(singletonList(
                new Permission().withFunctionId("1002")
                    .withAssignedPrivileges(singletonList(invalid))));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url, HttpMethod.POST, postData, "user",
                ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }
}
