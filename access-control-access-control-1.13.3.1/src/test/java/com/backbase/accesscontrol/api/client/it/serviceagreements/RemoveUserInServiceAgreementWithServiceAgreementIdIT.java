package com.backbase.accesscontrol.api.client.it.serviceagreements;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.google.common.collect.Lists;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link ServiceAgreementsController#postUsersRemove
 */
public class RemoveUserInServiceAgreementWithServiceAgreementIdIT extends TestDbWireMock {

    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";
    private static final String REMOVE_USERS_FROM_SERVICE_AGREEMENT_URL =
        "/accessgroups/service-agreements/{id}/users/remove";

    private ServiceAgreement serviceAgreementCustom;
    private LegalEntity legalEntity2;
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_ID2 = UUID.randomUUID().toString();
    private UserContext userContext;

    @Before
    public void setUp() {
        legalEntity2 = createLegalEntity(null, "le-name2", "ex-id32", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        legalEntity2 = legalEntityJpaRepository.save(legalEntity2);

        serviceAgreementCustom = createServiceAgreement("name.sa1", "exid.sa1", "desc.sa1",
            legalEntity2,
            null, null);
        serviceAgreementCustom.setMaster(false);
        serviceAgreementCustom = serviceAgreementJpaRepository.save(serviceAgreementCustom);

        Participant participant = new Participant()
            .withId(UUID.randomUUID().toString())
            .withLegalEntity(legalEntity2)
            .withServiceAgreement(serviceAgreementCustom)
            .withShareAccounts(true)
            .withShareUsers(true);
        participant.addParticipantUsers(asList(USER_ID, USER_ID2));
        participantJpaRepository.save(participant);

        ApplicableFunctionPrivilege viewServiceAgreement = businessFunctionCache.getApplicableFunctionPrivilegeById(
            businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList("view"))
                .stream().findFirst().get());

        GroupedFunctionPrivilege viewEntitlementsWithLimit =
            getGroupedFunctionPrivilege(null, viewServiceAgreement, null);

        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreementCustom)
        );

        userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(USER_ID, serviceAgreementCustom.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(contextUserId,
                serviceAgreementCustom.getId())));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup);
    }

    @Test
    public void testRemoveUserFromServiceAgreement() throws Exception {

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(USER_ID);
        user.setLegalEntityId(legalEntity2.getId());
        user.setFullName("userFullName2");

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId(USER_ID2);
        user2.setLegalEntityId(legalEntity2.getId());
        user2.setFullName("userFullName2");

        list.addUsersItem(user);
        list.addUsersItem(user2);
        addStubGet(new UrlBuilder(GET_USERS_URL).addQueryParameter("id", asList(USER_ID, USER_ID2)).build(),
            list, 200);
        PresentationUsersForServiceAgreementRequestBody usersRemovePostRequestBody
            = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(asList(USER_ID, USER_ID2));

        Participant participantBefore = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity2.getExternalId()).get();

        ResponseEntity<String> response = executeClientRequestEntityWithUserContext(
            new UrlBuilder(REMOVE_USERS_FROM_SERVICE_AGREEMENT_URL)
                .addPathParameter(serviceAgreementCustom.getId())
                .build()
            , HttpMethod.POST, usersRemovePostRequestBody,
            USER_ID, ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_EDIT, userContext, legalEntity2.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Participant participantAfter = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity2.getExternalId()).get();

        assertEquals(2, participantBefore.getParticipantUsers().size());
        assertEquals(0, participantAfter.getParticipantUsers().size());
    }

}
