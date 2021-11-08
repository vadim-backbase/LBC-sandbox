package com.backbase.accesscontrol.api.service.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_052;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.FunctionGroupServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.PresentationPermissionFunctionGroupUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link FunctionGroupServiceApiController#putFunctionGroupsUpdate} method.
 */
public class UpdateFunctionGroupByIdentifierIT extends TestDbWireMock {

    private static final String FUNCTION_GROUP_BATCH_UPDATE_URL = "/accessgroups/function-groups/batch/update";

    private ServiceAgreement serviceAgreement;
    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;
    private AssignablePermissionSet assignablePermissionSetRegularUpdate;
    private List<ApplicableFunctionPrivilege> permissions;

    @Before
    public void setUp() {
        assignablePermissionSetRegularUpdate = createAssignablePermissionSet(
            "apsRegularUpdateBatch",
            AssignablePermissionType.CUSTOM,
            "apsRegularIngestUpdateBatch",
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view").getId()
        );
        assignablePermissionSetRegularUpdate = assignablePermissionSetJpaRepository
            .save(assignablePermissionSetRegularUpdate);

        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(newHashSet(assignablePermissionSetRegularUpdate));
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        permissions = singletonList(businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view"));

        functionGroup1 = functionGroupJpaRepository.save(
            FunctionGroupUtil
                .getFunctionGroup(null, "fg-name", "fg-description", new HashSet<>(), FunctionGroupType.DEFAULT,
                    serviceAgreement)
        );

        functionGroup2 = functionGroupJpaRepository.save(
            FunctionGroupUtil
                .getFunctionGroup(null, "fg-name-2", "fg-description-2", new HashSet<>(), FunctionGroupType.DEFAULT,
                    serviceAgreement)
        );
    }

    @Test
    public void testUpdateSuccessfullyFunctionGroupBatch() throws Exception {
        PresentationFunctionGroupPutRequestBody putItem1 = getBasePutBody(functionGroup1);
        putItem1.getFunctionGroup().setName(putItem1.getFunctionGroup().getName() + "-updated");
        putItem1.setIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup1.getId()));
        putItem1.getFunctionGroup().setValidFromDate("2018-01-01");
        putItem1.getFunctionGroup().setValidFromTime("01:00:00");

        PresentationFunctionGroupPutRequestBody putItem2 = getBasePutBody(functionGroup2);
        putItem2.setIdentifier(new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName(functionGroup2.getName())
                .withExternalServiceAgreementId(serviceAgreement.getExternalId())));
        putItem2.getFunctionGroup().setName(putItem2.getFunctionGroup().getName() + "-updated");

        List<PresentationFunctionGroupPutRequestBody> putData = asList(putItem1, putItem2);

        String jsonResponse = executeRequest(FUNCTION_GROUP_BATCH_UPDATE_URL, putData, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemExtendedList = asList(
            readValue(jsonResponse, BatchResponseItemExtended[].class));

        assertEquals(2, responseItemExtendedList.size());
        assertEquals(functionGroup1.getId(), responseItemExtendedList.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemExtendedList.get(0).getStatus());

        assertEquals(functionGroup2.getName(), responseItemExtendedList.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(),
            responseItemExtendedList.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemExtendedList.get(1).getStatus());

        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
                .withAction(UPDATE)
                .withId(functionGroup1.getId()),
            new FunctionGroupEvent()
                .withAction(UPDATE)
                .withId(functionGroup2.getId())));
    }

    @Test
    public void shouldReturnErrorsOnInvalidIdentifiers() throws Exception {
        PresentationFunctionGroupPutRequestBody putItem1 = getBasePutBody(functionGroup1);
        putItem1.getFunctionGroup().setName(putItem1.getFunctionGroup().getName() + "-updated");
        String randomId = getUuid();
        putItem1.setIdentifier(new PresentationIdentifier().withIdIdentifier(randomId));

        PresentationFunctionGroupPutRequestBody putItem2 = getBasePutBody(functionGroup2);
        putItem2.setIdentifier(new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName("invalid-name")
                .withExternalServiceAgreementId("invalid-service-agreement-external-id")));
        putItem2.getFunctionGroup().setName(putItem2.getFunctionGroup().getName() + "-updated");

        String requestAsString = objectMapper.writeValueAsString(asList(putItem1, putItem2));

        String jsonResponse = executeRequest(FUNCTION_GROUP_BATCH_UPDATE_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemExtendedList = asList(
            readValue(jsonResponse, BatchResponseItemExtended[].class));

        assertEquals(2, responseItemExtendedList.size());
        assertEquals(randomId, responseItemExtendedList.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseItemExtendedList.get(0).getStatus());
        assertEquals(ERR_ACQ_003.getErrorMessage(), responseItemExtendedList.get(0).getErrors().get(0));

        assertEquals("invalid-name", responseItemExtendedList.get(1).getResourceId());
        assertEquals("invalid-service-agreement-external-id",
            responseItemExtendedList.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtendedList.get(1).getStatus());
        assertEquals(ERR_ACC_052.getErrorMessage(), responseItemExtendedList.get(1).getErrors().get(0));
    }

    @Test
    public void shouldThrowBadRequestIfThereIsOtherFunctionGroupWithSameName() throws Exception {
        createFunctionGroup("duplicate-name", "fg-description", serviceAgreement,
            singletonList(businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view").getId()),
            FunctionGroupType.DEFAULT);

        PresentationFunctionGroupPutRequestBody putItem1 = getBasePutBody(functionGroup1);
        putItem1.getFunctionGroup().setName("duplicate-name");
        putItem1.setIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup1.getId()));

        PresentationFunctionGroupPutRequestBody putItem2 = getBasePutBody(functionGroup2);
        putItem2.setIdentifier(new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName(functionGroup2.getName())
                .withExternalServiceAgreementId(serviceAgreement.getExternalId())));
        putItem2.getFunctionGroup().setName("duplicate-name");

        String requestAsString = objectMapper.writeValueAsString(asList(putItem1, putItem2));

        String jsonResponse = executeRequest(FUNCTION_GROUP_BATCH_UPDATE_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemExtendedList = asList(
            readValue(jsonResponse, BatchResponseItemExtended[].class));

        assertEquals(2, responseItemExtendedList.size());
        assertEquals(functionGroup1.getId(), responseItemExtendedList.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtendedList.get(0).getStatus());
        assertEquals(ERR_ACQ_023.getErrorMessage(), responseItemExtendedList.get(0).getErrors().get(0));

        assertEquals(functionGroup2.getName(), responseItemExtendedList.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemExtendedList.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtendedList.get(1).getStatus());
        assertEquals(ERR_ACQ_023.getErrorMessage(), responseItemExtendedList.get(1).getErrors().get(0));
    }

    @Test
    public void shouldReturnErrorsIfPermissionsAreInvalid() throws Exception {
        PresentationFunctionGroupPutRequestBody putItem1 = getBasePutBody(functionGroup1);
        putItem1.getFunctionGroup().setPermissions(
            singletonList(
                new PresentationPermissionFunctionGroupUpdate()
                    .withFunctionName("does-not-exist")
                    .withPrivileges(singletonList("view"))
            )
        );
        putItem1.setIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup1.getId()));

        PresentationFunctionGroupPutRequestBody putItem2 = getBasePutBody(functionGroup2);
        putItem2.setIdentifier(new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName(functionGroup2.getName())
                .withExternalServiceAgreementId(serviceAgreement.getExternalId())));
        putItem2.getFunctionGroup().setPermissions(
            singletonList(
                new PresentationPermissionFunctionGroupUpdate()
                    .withFunctionName(
                        businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view").getBusinessFunctionName())
                    .withPrivileges(singletonList("does-not-exist"))
            )
        );

        String requestAsString = objectMapper.writeValueAsString(asList(putItem1, putItem2));

        String jsonResponse = executeRequest(FUNCTION_GROUP_BATCH_UPDATE_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemExtendedList = asList(
            readValue(jsonResponse, BatchResponseItemExtended[].class));

        assertEquals(2, responseItemExtendedList.size());
        assertEquals(functionGroup1.getId(), responseItemExtendedList.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtendedList.get(0).getStatus());
        assertEquals(ERR_ACQ_024.getErrorMessage(), responseItemExtendedList.get(0).getErrors().get(0));

        assertEquals(functionGroup2.getName(), responseItemExtendedList.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemExtendedList.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtendedList.get(1).getStatus());
        assertEquals(ERR_ACQ_011.getErrorMessage(), responseItemExtendedList.get(1).getErrors().get(0));
    }

    @Test
    public void shouldSuccessfullyUpdateFunctionGroupsOfTypeTemplate() throws Exception {
        FunctionGroup functionGroup3 = FunctionGroupUtil
            .getFunctionGroup(null, "fg-name-3", "fg-description-3", new HashSet<>(), FunctionGroupType.TEMPLATE,
                serviceAgreement);

        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList = GroupedFunctionPrivilegeUtil
            .getGroupedFunctionPrivileges(
                permissions.stream()
                    .map(applicableFunctionPrivilege -> getGroupedFunctionPrivilege(null, applicableFunctionPrivilege,
                        functionGroup3))
                    .toArray(GroupedFunctionPrivilege[]::new)
            );
        functionGroup3.setPermissions(groupedFunctionPrivilegeList);
        functionGroup3.setAssignablePermissionSet(assignablePermissionSetRegularUpdate);

        functionGroupJpaRepository.save(functionGroup3);

        PresentationFunctionGroupPutRequestBody putItem3 = getBasePutBody(functionGroup3);
        putItem3.getFunctionGroup().setName(putItem3.getFunctionGroup().getName() + "-updated");
        putItem3.setIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup3.getId()));

        PresentationFunctionGroupPutRequestBody putItem2 = getBasePutBody(functionGroup2);
        putItem2.setIdentifier(new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName(functionGroup2.getName())
                .withExternalServiceAgreementId(serviceAgreement.getExternalId())));
        putItem2.getFunctionGroup().setName(putItem2.getFunctionGroup().getName() + "-updated");

        String requestAsString = objectMapper.writeValueAsString(asList(putItem3, putItem2));

        String jsonResponse = executeRequest(FUNCTION_GROUP_BATCH_UPDATE_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemExtendedList = asList(
            readValue(jsonResponse, BatchResponseItemExtended[].class));

        assertEquals(2, responseItemExtendedList.size());
        assertEquals(functionGroup3.getId(), responseItemExtendedList.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemExtendedList.get(0).getStatus());

        assertEquals(functionGroup2.getName(), responseItemExtendedList.get(1).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemExtendedList.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemExtendedList.get(1).getStatus());

        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(UPDATE)
            .withId(functionGroup2.getId())));
    }

    private PresentationFunctionGroupPutRequestBody getBasePutBody(FunctionGroup functionGroup) {
        ApplicableFunctionPrivilege afp1002View = businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view");
        return new PresentationFunctionGroupPutRequestBody()
            .withFunctionGroup(
                new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroup()
                    .withName(functionGroup.getName())
                    .withDescription(functionGroup.getDescription())
                    .withPermissions(singletonList(
                        new PresentationPermissionFunctionGroupUpdate()
                            .withFunctionName(afp1002View.getBusinessFunctionName())
                            .withPrivileges(singletonList(afp1002View.getPrivilegeName()))
                    ))
            );
    }

}

