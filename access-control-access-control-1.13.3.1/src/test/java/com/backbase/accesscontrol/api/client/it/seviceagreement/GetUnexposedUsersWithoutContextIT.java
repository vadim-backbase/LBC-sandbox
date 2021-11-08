package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_062;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetUnexposedUsersWithoutContextIT extends TestDbWireMock {

    private static final String POST_USERS_URL = "/service-api/v2/users/legalentityids";

    private String fromQuery = "1";
    private String sizeQuery = "2";
    private String participant1 = "LE-1";
    private String exposedUser1 = "U-04";
    private String unexposedUser1 = "U-01";
    private UserContext userContext;
    private LegalEntity customLe;
    private ServiceAgreement customSa;

    @Before
    public void setUp() {
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
        customSa.addParticipant(new Participant()
            .withShareUsers(true)
            .withShareAccounts(false)
            .withLegalEntity(customLe)
        );
        customSa = serviceAgreementJpaRepository.save(customSa);
        String serviceAgreementId = customSa.getId();

        FunctionGroup functionGroup = createFunctionGroup(getUuid(), getUuid(),
            serviceAgreementJpaRepository.findById(customSa.getId()).get(),
            businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, null,
                    Lists.newArrayList(PRIVILEGE_VIEW)), FunctionGroupType.DEFAULT);

        userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(exposedUser1, serviceAgreementId)
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(exposedUser1, serviceAgreementId)));

        userAssignedFunctionGroupJpaRepository.save(new UserAssignedFunctionGroup()
            .withFunctionGroup(functionGroup)
            .withUserContext(userContext));

    }

    @Test
    public void testGetUnexposedUsersForCSA() throws Exception {
        String serviceAgreementId = customSa.getId();
        String URL = "/accessgroups/service-agreements/{saId}/users/unexposed";
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(participant1);
        user.setId(unexposedUser1);
        user.setExternalId(exposedUser1);
        list.setUsers(Collections.singletonList(user));
        list.setTotalElements(1L);
        addStubPost(POST_USERS_URL, list, 200);

        String contentAsString = executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addQueryParameter("size", sizeQuery).addPathParameter(serviceAgreementId).build(),
            HttpMethod.GET,
            exposedUser1, userContext, customLe.getId());

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(1, responseUnexposedUsers.size());
        assertEquals(unexposedUser1, list.getUsers().get(0).getId());
        assertEquals(participant1, list.getUsers().get(0).getLegalEntityId());

    }

    @Test
    public void testGetUnexposedUsersForMSA() throws Exception {
        String serviceAgreementId = rootMsa.getId();
        String URL = "/accessgroups/service-agreements/{saId}/users/unexposed";
        String contentAsString = executeClientRequest(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addQueryParameter("size", sizeQuery).addPathParameter(serviceAgreementId).build(),
            HttpMethod.GET,
            exposedUser1, ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(0, responseUnexposedUsers.size());


    }

    @Test
    public void shouldThrowBadRequestWhenGetUsersForServiceAgreementIsInvoked() {
        String query = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec.";
        String URL = "/accessgroups/service-agreements/{saId}/users/unexposed";

        Map<String, String> additions = new HashMap<>();
        String key = "address";
        String value = "123456789";
        String key1 = "mail";
        String value1 = "mail123";
        additions.put(key, value);
        additions.put(key1, value1);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("U-01");
        user1.setAdditions(additions);

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId("U-02");
        user2.setAdditions(additions);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addQueryParameter("size", sizeQuery)
                .addQueryParameter("query", query).addPathParameter(customSa.getId()).build(),
            HttpMethod.GET,
            exposedUser1, userContext, customLe.getId()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_062.getErrorMessage(), ERR_AG_062.getErrorCode()));
    }


    @Test
    public void testGetUsersForServiceAgreementWithQueryParameter() throws Exception {
        String URL = "/accessgroups/service-agreements/{saId}/users/unexposed";
        String query = "U";
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(participant1);
        user.setId(unexposedUser1);
        user.setExternalId(exposedUser1);
        list.setUsers(Collections.singletonList(user));
        list.setTotalElements(1L);
        addStubPost(POST_USERS_URL, list, 200);

        String contentAsString = executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("from", fromQuery)
                .addPathParameter(customSa.getId())
                .addQueryParameter("size", sizeQuery)
                .addQueryParameter("query", query).build(),
            HttpMethod.GET,
            exposedUser1, userContext, customLe.getId());

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(1, responseUnexposedUsers.size());
        assertEquals(unexposedUser1, list.getUsers().get(0).getId());
        assertEquals(participant1, list.getUsers().get(0).getLegalEntityId());
    }

    @Test
    public void testGetUsersForServiceAgreementWithDefaultPaginationValues() throws Exception {
        String query = "U";
        String URL = "/accessgroups/service-agreements/{saId}/users/unexposed";

        Map<String, String> additions = new HashMap<>();
        String key = "address";
        String value = "123456789";
        String key1 = "mail";
        String value1 = "mail123";
        additions.put(key, value);
        additions.put(key1, value1);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("U-01");
        user1.setAdditions(additions);

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId("U-02");
        user2.setAdditions(additions);

        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(participant1);
        user.setId(unexposedUser1);
        user.setExternalId(exposedUser1);
        list.setUsers(Collections.singletonList(user));
        list.setTotalElements(1L);
        addStubPost(POST_USERS_URL, list, 200);

        String contentAsString = executeClientRequestWithContext(
            new UrlBuilder(URL)
                .addPathParameter(customSa.getId())
                .addQueryParameter("query", query).build(),
            HttpMethod.GET,
            exposedUser1, userContext, customLe.getId());

        List<UnexposedUsersGetResponseBody> responseUnexposedUsers = objectMapper
            .readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(1, responseUnexposedUsers.size());
        assertEquals(unexposedUser1, list.getUsers().get(0).getId());
        assertEquals(participant1, list.getUsers().get(0).getLegalEntityId());
    }
}
