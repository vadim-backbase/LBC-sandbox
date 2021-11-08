package com.backbase.accesscontrol.api.client.it.serviceagreements;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementsController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementsController#postUsersAdd}
 */
public class AddUserInServiceAgreementWithoutContextIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/service-agreements/{serviceAgreementId}/users/add";
    private static final String USERS_URL = "/service-api/v2/users/bulk";

    private ServiceAgreement serviceAgreementCustom;
    private LegalEntity legalEntity;

    @Before
    public void setUp() {
        legalEntity = createLegalEntity(null, "le-name2", "ex-id32", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreementCustom = createServiceAgreement("name.sa1", "exid.sa1", "desc.sa1", legalEntity,
            legalEntity.getId(), legalEntity.getId());
        serviceAgreementCustom.setMaster(false);
        serviceAgreementCustom = serviceAgreementJpaRepository.save(serviceAgreementCustom);
    }

    @Test
    public void testAddUserInServiceAgreement() throws Exception {
        String userId1 = "U-01";
        String userId2 = "U-02";

        GetUsersList list = new GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setId(userId1);
        user.setLegalEntityId(legalEntity.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new GetUser();
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
        String valueAsString = objectMapper.writeValueAsString(putData);

        Participant participant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity.getExternalId()).get();

        // Before add users there are 0 users in the participants
        assertEquals(0, participant.getParticipantUsers().size());

        executeClientRequest(new UrlBuilder(URL).addPathParameter(serviceAgreementCustom.getId()).build(),
            HttpMethod.POST, valueAsString, "user", ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_EDIT);

        participant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(serviceAgreementCustom.getExternalId(),
                legalEntity.getExternalId()).get();

        // After add users there are 2 users in the participants
        assertThat(participant.getParticipantUsers(), hasSize(2));
        assertThat(participant.getParticipantUsers(), containsInAnyOrder(
            hasProperty("userId", is(userId1)),
            hasProperty("userId", is(userId2))
        ));
    }
}