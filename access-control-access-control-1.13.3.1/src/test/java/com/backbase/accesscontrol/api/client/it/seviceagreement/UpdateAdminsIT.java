package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_028;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_080;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link ServiceAgreementController#putAdmins}
 */
public class UpdateAdminsIT extends TestDbWireMock {

    private static final String url =
        "/accessgroups/serviceagreements/{id}/admins";
    private static final String USER_EXT_ID_ADMIN = "admin";
    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";

    @Test
    public void testSuccessfulUpdatedAdmins() throws IOException, JSONException {

        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "newLe", "newLe", rootLegalEntity,
                LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        String serviceAgreementId = rootMsa.getId();

        LegalEntityAdmins participant1 = createLegalEntityAdmin(rootLegalEntity.getId(),
            new HashSet<>(singletonList(legalEntity.getId())));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1= new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(legalEntity.getId());
        user1.setLegalEntityId(rootLegalEntity.getId());
        user1.setFullName("userFullName1");

        com.backbase.dbs.user.api.client.v2.model.GetUsersList response1 = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        response1.setUsers(Collections.singletonList(user1));

        addStubGet(new UrlBuilder(GET_USERS_URL)
                .addQueryParameter("id", legalEntity.getId()).build(),
            response1, 200);
        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(participant1);
        ResponseEntity<String> response = executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementId)
                .build()
            , HttpMethod.PUT, adminPutRequestBody,
            USER_EXT_ID_ADMIN, MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testShouldThrowBadRequestExceptionAdminMustBelongInParticipant() {
        String serviceAgreementId = rootMsa.getId();
        LegalEntityAdmins participant1 = createLegalEntityAdmin(getUuid(), new HashSet<>(singletonList("1")));
        LegalEntityAdmins participant2 = createLegalEntityAdmin(getUuid(), new HashSet<>(singletonList("2")));

        com.backbase.dbs.user.api.client.v2.model.GetUser user= new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId("1");
        user.setLegalEntityId(rootLegalEntity.getId());
        user.setFullName("userFullName1");

        com.backbase.dbs.user.api.client.v2.model.GetUser user1= new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("2");
        user1.setLegalEntityId(rootLegalEntity.getId());
        user1.setFullName("userFullName2");

        com.backbase.dbs.user.api.client.v2.model.GetUsersList response1 = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        response1.setUsers(Collections.singletonList(user));

        addStubGet(new UrlBuilder(GET_USERS_URL).addQueryParameter("id", "1%2C2").build(),
            response1, 200);

        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(participant1, participant2);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementId)
                .build()
            , HttpMethod.PUT, adminPutRequestBody,
            USER_EXT_ID_ADMIN, MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_028.getErrorMessage(), ERR_AG_028.getErrorCode()));
    }

    @Test
    public void testUpdateAdminsWithDuplicateAdminsForProvider() {

        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "newLe", "newLe", rootLegalEntity,
                LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        String serviceAgreementId = rootMsa.getId();

        LegalEntityAdmins participant1 = createLegalEntityAdmin(rootLegalEntity.getId(),
            new HashSet<>(asList(legalEntity.getId(), legalEntity.getId())));
        LegalEntityAdmins participant2 = createLegalEntityAdmin(rootLegalEntity.getId(),
            new HashSet<>(asList(legalEntity.getId(), legalEntity.getId())));

        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(participant1, participant2);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementId)
                .build()
            , HttpMethod.PUT, adminPutRequestBody,
            USER_EXT_ID_ADMIN, MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_080.getErrorMessage(), ERR_AG_080.getErrorCode()));
    }

    @Test
    public void testInvalidServiceAgreement() {
        LegalEntityAdmins participant1 = createLegalEntityAdmin(getUuid(), new HashSet<>(singletonList("1")));
        AdminsPutRequestBody adminPutRequestBody = createAdminPutRequestBody(participant1);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter("invalidSa")
                .build()
            , HttpMethod.PUT, adminPutRequestBody,
            USER_EXT_ID_ADMIN, MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()));
    }

    private AdminsPutRequestBody createAdminPutRequestBody(LegalEntityAdmins... participants) {
        return new AdminsPutRequestBody()
            .withParticipants(asList(participants));
    }

    private LegalEntityAdmins createLegalEntityAdmin(String entityId, Set<String> admins) {
        return new LegalEntityAdmins().withId(entityId).withAdmins(admins);
    }
}
