package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_MANAGE_USERS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.RESOURCE_MANAGE_USERS;
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
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.support.TransactionTemplate;

public class ListUsersForServiceAgreementIT extends TestDbWireMock {

    private static String URL = "/accessgroups/serviceagreements/context/users";
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
            return true;
        });
    }

    @Test
    public void testGetUsersForServiceAgreement() throws Exception {

        String GET_USERS_URL = "/service-api/v2/users/bulk";
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(exposedUser1);
        user.setLegalEntityId(customLe.getId());
        user.setExternalId("ExternalId");
        user.setFullName("FullName");

        com.backbase.dbs.user.api.client.v2.model.GetUsersList response = new GetUsersList();
        response.setUsers(Collections.singletonList(user));
        response.setTotalElements(1L);

        addStubGet(new UrlBuilder(GET_USERS_URL).addQueryParameter("id", exposedUser1)
            .build(), response, 200);

        String responseAsString = executeClientRequestWithContext(URL, HttpMethod.GET, "", exposedUser1,
            ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, ResourceAndFunctionNameConstants.PRIVILEGE_VIEW,
            userContext, customLe.getId());
        List<ServiceAgreementUsersGetResponseBody> serviceAgreementUsers = this.objectMapper
            .readValue(responseAsString, new TypeReference<>() {
            });
        assertEquals(1, serviceAgreementUsers.size());
        assertEquals(exposedUser1, serviceAgreementUsers.get(0).getId());
        assertEquals(customLe.getName(), serviceAgreementUsers.get(0).getLegalEntityName());
    }

    @Test
    public void shouldThrowBadRequestWhenGetUsersForServiceAgreementIsInvoked() {
        String query = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec.";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("query", query).build(),
            HttpMethod.GET, "", exposedUser1,
            ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, ResourceAndFunctionNameConstants.PRIVILEGE_VIEW,
            userContext, customLe.getId()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_062.getErrorMessage(), ERR_AG_062.getErrorCode()));
    }

    @Test
    public void testGetUsersForServiceAgreementWithQueryParameter() throws Exception {
        String query = "U";

        String GET_USERS_URL = "/service-api/v2/users/bulk";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(exposedUser1);
        user.setLegalEntityId(customLe.getId());
        user.setExternalId("ExternalId");
        user.setFullName("FullName");

        com.backbase.dbs.user.api.client.v2.model.GetUsersList response = new GetUsersList();
        response.setUsers(Collections.singletonList(user));

        addStubGet(
            new UrlBuilder(GET_USERS_URL).addQueryParameter("id", exposedUser1).addQueryParameter("query", query)
                .build(),
            response, 200);

        String responseAsString = executeClientRequestWithContext(
            new UrlBuilder(URL).addQueryParameter("query", query).build(), HttpMethod.GET, "", exposedUser1,
            ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, ResourceAndFunctionNameConstants.PRIVILEGE_VIEW,
            userContext, customLe.getId());
        List<ServiceAgreementUsersGetResponseBody> serviceAgreementUsers = this.objectMapper
            .readValue(responseAsString, new TypeReference<>() {
            });
        assertEquals(1, serviceAgreementUsers.size());
        assertEquals(exposedUser1, serviceAgreementUsers.get(0).getId());
        assertEquals(customLe.getName(), serviceAgreementUsers.get(0).getLegalEntityName());
    }
}
