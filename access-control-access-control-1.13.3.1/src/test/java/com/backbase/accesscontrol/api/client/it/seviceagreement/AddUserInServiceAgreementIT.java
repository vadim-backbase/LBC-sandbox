package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_039;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_067;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementController#postUsersAdd}
 */
public class AddUserInServiceAgreementIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements/context/users/add";
    private static final String USERS_URL = "/service-api/v2/users/bulk";;

    private ServiceAgreement serviceAgreement;
    private ServiceAgreement serviceAgreementCustom;
    private LegalEntity legalEntity2;
    private LegalEntity legalEntity;
    private UserContext userContext;
    private UserContext userContext2;

    private final String userId1 = "U-01";
    private final String userId2 = "U-02";

    @Before
    public void setUp() {
        legalEntity = createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = createServiceAgreement("name.sa", "exid.sa", "desc.sa", legalEntity, legalEntity.getId(),
            legalEntity.getId());
        serviceAgreement.setMaster(true);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId1, serviceAgreement.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(userId1, serviceAgreement.getId())));

        legalEntity2 = createLegalEntity(null, "le-name2", "ex-id32", legalEntity,
            LegalEntityType.CUSTOMER);
        legalEntity2 = legalEntityJpaRepository.save(legalEntity2);

        serviceAgreementCustom = createServiceAgreement("name.sa1", "exid.sa1", "desc.sa1", legalEntity2,
            legalEntity2.getId(), legalEntity2.getId());
        serviceAgreementCustom.setMaster(false);
        serviceAgreementCustom = serviceAgreementJpaRepository.save(serviceAgreementCustom);

        userContext2 = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId1, serviceAgreementCustom.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(userId1, serviceAgreementCustom.getId())));
    }

    @Test
    public void testAddUserInServiceAgreement() throws Exception {

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(userId1);
        user.setLegalEntityId(legalEntity2.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId(userId2);
        user2.setLegalEntityId(legalEntity2.getId());
        list.addUsersItem(user);
        list.addUsersItem(user2);

        addStubGet(
            new UrlBuilder(USERS_URL)
                .addQueryParameter("id", asList(userId1, userId2))
                .build(),
            list, 200);

        PresentationUsersForServiceAgreementRequestBody putData = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(asList(userId1, userId2));

        String valueAsString = objectMapper.writeValueAsString(putData);

        Participant participant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity2.getExternalId()).get();

        // Before add users there are 0 users in the participants
        assertEquals(0, participant.getParticipantUsers().size());

        executeClientRequestWithContext(URL, HttpMethod.POST, valueAsString, "user",
            ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_EDIT,
            userContext2, legalEntity2.getId());

        participant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity2.getExternalId()).get();

        // After add users there are 2 users in the participants
        assertThat(participant.getParticipantUsers(), hasSize(2));
        assertThat(participant.getParticipantUsers(), containsInAnyOrder(
            hasProperty("userId", is(userId1)),
            hasProperty("userId", is(userId2))
        ));
    }

    @Test
    public void shouldThrowBadRequestIfServiceAgreementIsMaster() throws Exception {

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(userId1);
        user.setLegalEntityId(legalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId(userId2);
        user2.setLegalEntityId(legalEntity.getId());
        list.addUsersItem(user);
        list.addUsersItem(user2);
        addStubGet(
            new UrlBuilder(USERS_URL)
                .addQueryParameter("id", asList(userId1, userId2))
                .build(),
            list, 200);

        PresentationUsersForServiceAgreementRequestBody putData = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(asList(userId1, userId2));
        String requestAsString = objectMapper.writeValueAsString(putData);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequestWithContext(URL, HttpMethod.POST, requestAsString, "user",
                ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_EDIT,
                userContext, legalEntity.getId()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfUserIsAlreadyExposedInServiceAgreement() throws Exception {
        String userId = "userId";

        LegalEntity legalEntity3 = createLegalEntity(null, "le-name3", "ex-id33", legalEntity,
            LegalEntityType.CUSTOMER);
        LegalEntity legalEntity3Saved = legalEntityJpaRepository.save(legalEntity3);

        UserContext userContext3 = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId, serviceAgreementCustom.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(userId, serviceAgreementCustom.getId())));

        Participant participant = new Participant()
            .withLegalEntity(legalEntity3Saved)
            .withServiceAgreement(serviceAgreementCustom)
            .withShareAccounts(true)
            .withShareUsers(true);
        participant.addParticipantUser(userId);
        participantJpaRepository.save(participant);

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(userId);
        user.setLegalEntityId(legalEntity3Saved.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId(userId2);
        user2.setLegalEntityId(legalEntity3Saved.getId());
        list.addUsersItem(user);
        list.addUsersItem(user2);
        addStubGet(
            new UrlBuilder(USERS_URL)
                .addQueryParameter("id", asList(userId, userId2))
                .build(),
            list, 200);

        PresentationUsersForServiceAgreementRequestBody putData = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(asList(userId, userId2));
        String requestAsString = objectMapper.writeValueAsString(putData);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequestWithContext(URL, HttpMethod.POST, requestAsString, "user",
                ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_EDIT,
                userContext3, legalEntity3Saved.getId()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_039.getErrorMessage(), ERR_ACC_039.getErrorCode()));
    }
}
