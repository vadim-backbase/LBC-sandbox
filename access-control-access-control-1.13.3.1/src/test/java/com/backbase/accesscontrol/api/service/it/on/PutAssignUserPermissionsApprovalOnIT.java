package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_061;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.UsersServiceApiController;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link UsersServiceApiController#putAssignUserPermissions}
 */
@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class PutAssignUserPermissionsApprovalOnIT extends TestDbWireMock {

    private static final String PUT_USER_PERMISSIONS_URL = "/accessgroups/users/permissions/user-permissions";
    private static final String POST_USERS_PERSISTENCE_URL = "/service-api/v2/users/bulk/externalids";

    private static final String EXTERNAL_USER_ID = "externalUserId";
    private static final String USER_ID = UUID.randomUUID().toString();

    private LegalEntity legalEntity;
    private ServiceAgreement serviceAgreement;
    private FunctionGroup functionGroup;
    private DataGroup dataGroup;

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        legalEntity = createLegalEntity("le-name", "Backbase", null);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = createServiceAgreement("sa-01", "SA-01", "desc", legalEntity, legalEntity.getId(),
            legalEntity.getId());
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(newHashSet(apsDefaultRegular));
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        functionGroup = getFunctionGroup(null, "name", "description", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        dataGroup = createDataGroup("dag01", "ARRANGEMENTS", "dag01", serviceAgreement);
        dataGroup = dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    public void shouldThrowBadRequestForPendingDeleteDataGroup() throws Exception {

        ApprovalDataGroup pendingDeleteDataGroup = new ApprovalDataGroup();
        pendingDeleteDataGroup.setApprovalId(UUID.randomUUID().toString());
        pendingDeleteDataGroup.setDataGroupId(dataGroup.getId());
        approvalDataGroupJpaRepository.save(pendingDeleteDataGroup);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(legalEntity.getId());
        user.setId(USER_ID);
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());


        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString,
            HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(1, responseItemsExtended.get(0).getErrors().size());
        assertEquals(ERR_ACQ_061.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));
    }
}
