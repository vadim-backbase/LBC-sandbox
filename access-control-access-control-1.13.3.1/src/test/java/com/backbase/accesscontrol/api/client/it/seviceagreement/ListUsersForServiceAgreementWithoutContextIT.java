package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_MANAGE_USERS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.RESOURCE_MANAGE_USERS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.support.TransactionTemplate;

public class ListUsersForServiceAgreementWithoutContextIT extends TestDbWireMock {

    private String fromQuery = "1";
    private String sizeQuery = "2";
    private String exposedUser1 = UUID.randomUUID().toString();
    private UserContext userContext;
    private LegalEntity customLe;
    private ServiceAgreement customSa;

    @Before
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(transactionStatus -> {
            customLe = legalEntityJpaRepository.save(new LegalEntity()
                .withExternalId("CUSTOM")
                .withName("CUSTOMLE")
                .withType(LegalEntityType.CUSTOMER));

            customSa = new ServiceAgreement()
                .withName("customSa")
                .withDescription("customSaDesc")
                .withExternalId("externalCustomSa")
                .withCreatorLegalEntity(customLe)
                .withMaster(false);

            customSa = serviceAgreementJpaRepository.save(customSa);
            Participant participant = new Participant()
                .withShareUsers(true)
                .withServiceAgreement(customSa)
                .withShareAccounts(false)
                .withLegalEntity(customLe);
            participant.addParticipantUser(exposedUser1);
            participantJpaRepository.save(participant);
            String serviceAgreementId = customSa.getId();

            FunctionGroup functionGroup = createFunctionGroup(getUuid(), getUuid(),
                serviceAgreementJpaRepository.findById(customSa.getId()).get(),
                businessFunctionCache
                    .getByFunctionNameOrResourceNameOrPrivilegesOptional(FUNCTION_MANAGE_USERS, RESOURCE_MANAGE_USERS,
                        Lists.newArrayList(PRIVILEGE_VIEW)), FunctionGroupType.DEFAULT);

            userContext = userContextJpaRepository
                .findByUserIdAndServiceAgreementId(exposedUser1, serviceAgreementId)
                .orElseGet(() -> userContextJpaRepository.save(new UserContext(exposedUser1, serviceAgreementId)));

            userAssignedFunctionGroupJpaRepository.save(new UserAssignedFunctionGroup()
                .withFunctionGroup(functionGroup)
                .withUserContext(userContext));

            ServiceAgreementItem serviceAgreementItem = new ServiceAgreementItem()
                .withIsMaster(false)
                .withCreatorLegalEntity("CLE")
                .withId(serviceAgreementId)
                .withName("sa-name");
            return true;
        });
    }

    @Test
    public void testGetUsersForCustomServiceAgreement() throws Exception {
        String serviceAgreementId = customSa.getId();
        String URL = "/accessgroups/service-agreements/" + serviceAgreementId + "/users";
        String GET_USERS_URL = "/service-api/v2/users/bulk?id=" + exposedUser1 + "&from=1&cursor=&size=2";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(exposedUser1);
        user.setFullName("FullName");
        user.setExternalId("ExternalId");
        user.setLegalEntityId(customLe.getId());

        com.backbase.dbs.user.api.client.v2.model.GetUsersList response = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        response.setUsers(Collections.singletonList(user));

        addStubGet(GET_USERS_URL, response, 200);

        String contentAsString = executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addQueryParameter("size", sizeQuery).build(),
            HttpMethod.GET,
            exposedUser1, userContext, customLe.getId());

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(1, responseUnexposedUsers.size());
        assertEquals(exposedUser1, response.getUsers().get(0).getId());
        assertEquals(customLe.getId(), response.getUsers().get(0).getLegalEntityId());
    }

    @Test
    public void testGetUsersForMasterServiceAgreement() throws Exception {
        String serviceAgreementId = rootMsa.getId();
        String URL = "/accessgroups/service-agreements/" + serviceAgreementId + "/users";
        String GET_USERS_MASTER_URL =
            "/service-api/v2/users/legalentityids";

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(exposedUser1);
        user.setLegalEntityId(rootLegalEntity.getId());
        user.setFullName("userFullName1");
        user.setExternalId("ExternalId");
        list.setTotalElements(1L);
        list.setUsers(Lists.newArrayList(user));

        addStubPost(GET_USERS_MASTER_URL, list, 200);

        String contentAsString = executeClientRequest(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addQueryParameter("size", sizeQuery).build(),
            HttpMethod.GET,
            exposedUser1, FUNCTION_MANAGE_USERS, PRIVILEGE_VIEW);

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(1, responseUnexposedUsers.size());
        assertEquals(exposedUser1, responseUnexposedUsers.get(0).getId());
    }
}
