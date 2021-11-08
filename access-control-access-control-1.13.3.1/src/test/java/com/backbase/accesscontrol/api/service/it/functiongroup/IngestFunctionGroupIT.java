package com.backbase.accesscontrol.api.service.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.FunctionGroupServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationPermission;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link FunctionGroupServiceApiController#postPresentationIngestFunctionGroup (PresentationFunctionGroup
 * presentationFunctionGroup, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) method}.
 */
public class IngestFunctionGroupIT extends TestDbWireMock {

    private static final String INGEST_FUNCTION_GROUP_URL = "/accessgroups/function-groups/ingest";

    private ServiceAgreement serviceAgreementWithAps;
    private LegalEntity legalEntityAps;
    private BusinessFunction bf1028;
    private ApplicableFunctionPrivilege apfBf1028View;
    private AssignablePermissionSet assignablePermissionSetRegularIngest;

    @Before
    public void setUp() {

        apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        bf1028 = apfBf1028View.getBusinessFunction();
        assignablePermissionSetRegularIngest = createAssignablePermissionSet(
            "apsRegularIngest",
            AssignablePermissionType.CUSTOM,
            "apsRegularIngestDesc",
            apfBf1028View.getId()
        );
        assignablePermissionSetRegularIngest = assignablePermissionSetJpaRepository
            .save(assignablePermissionSetRegularIngest);
        legalEntityAps = LegalEntityUtil
            .createLegalEntity(null, "le-nameaps", "ex-id1aps", null, LegalEntityType.CUSTOMER);
        legalEntityAps = legalEntityJpaRepository.save(legalEntityAps);

        serviceAgreementWithAps = ServiceAgreementUtil
            .createServiceAgreement("name.saaps", "exid.saaps", "desc.saaps", legalEntityAps, null, null);
        serviceAgreementWithAps.setMaster(true);
        serviceAgreementWithAps.setStartDate(new Date(1111));
        serviceAgreementWithAps.setEndDate(new Date(3));
        serviceAgreementWithAps.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularIngest));
        serviceAgreementWithAps = serviceAgreementJpaRepository.save(serviceAgreementWithAps);

        rootMsa.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularIngest));
        serviceAgreementJpaRepository.save(rootMsa);
    }

    @Test
    public void testSuccessfulCreateFunctionGroupUnderMSA() throws Exception {
        String functionGroupName = "Name";
        String description = "Test Description";

        List<PresentationPermission> permissions = singletonList(new PresentationPermission()
            .withFunctionId(bf1028.getId())
            .withPrivileges(singletonList("view")));
        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withDescription(description)
            .withName(functionGroupName)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        String responseAsString = executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());

        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(responseId)));
    }

    @Test
    public void shouldSuccessfullySaveFunctionGroupOfTypeTemplate() throws Exception {
        String functionGroupName = "Name";
        String description = "Test Description";

        List<PresentationPermission> permissions = singletonList(new PresentationPermission()
            .withFunctionId(bf1028.getId())
            .withPrivileges(singletonList("view")));
        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withDescription(description)
            .withName(functionGroupName)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withPermissions(permissions)
            .withType(Type.TEMPLATE)
            .withApsId(new BigDecimal(assignablePermissionSetRegularIngest.getId()));

        String responseAsString = executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST);
        String responseId = readValue(
            responseAsString,
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody.class)
            .getId();
        Optional<FunctionGroup> fg = functionGroupJpaRepository.findById(responseId);
        assertNotNull(fg.get());

        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(ADD)
            .withId(responseId)));
    }

    @Test
    public void shouldThrowBadRequestIfFunctionGroupNameIsNotUnique() {
        String functionGroupName = "Name";
        String description = "Test Description";

        List<PresentationPermission> permissions = singletonList(new PresentationPermission()
            .withFunctionId(bf1028.getId())
            .withPrivileges(singletonList("view")));
        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withDescription(description)
            .withName(functionGroupName)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfPrivilegeDoesNotExist() {
        String functionGroupName = "Name";
        String description = "Test Description";

        List<PresentationPermission> permissions = singletonList(new PresentationPermission()
            .withFunctionId(bf1028.getId())
            .withPrivileges(singletonList("random")));
        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withDescription(description)
            .withName(functionGroupName)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfBusinessFunctionDoesNotExist() {
        String functionGroupName = "Name";
        String description = "Test Description";

        List<PresentationPermission> permissions = singletonList(new PresentationPermission()
            .withFunctionId("321321")
            .withPrivileges(singletonList("view")));
        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withDescription(description)
            .withName(functionGroupName)
            .withExternalServiceAgreementId(rootMsa.getExternalId())
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(INGEST_FUNCTION_GROUP_URL, postData, HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }
}
