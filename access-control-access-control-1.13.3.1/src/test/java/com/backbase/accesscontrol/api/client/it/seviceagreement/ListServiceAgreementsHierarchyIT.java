package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class ListServiceAgreementsHierarchyIT extends TestDbWireMock {

    @Test
    public void testListAllServiceAgreements() throws Exception {
        String userId = contextUserId;
        String legalEntityId = rootLegalEntity.getId();
        String query = "";
        String from = "0";
        String cursor = "";
        String size = "10";
        String getUserUrl = "/service-api/v2/users/{id}";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setId(userId);
        user.setLegalEntityId(legalEntityId);

        addStubGet(new UrlBuilder(getUserUrl)
                .addPathParameter(userId)
                .addQueryParameter("skipHierarchyCheck","true")
                .build(),
            user, 200);

        String URL = "/accessgroups/service-agreements/hierarchy";
        String response = executeClientRequest(
            new UrlBuilder(URL)
                .addQueryParameter("creatorId", rootLegalEntity.getId())
                .addQueryParameter("userId", userId)
                .addQueryParameter("query", query)
                .addQueryParameter("from", from)
                .addQueryParameter("cursor", cursor)
                .addQueryParameter("size", size)
                .build(), HttpMethod.GET, "admin",
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);
        List<PresentationServiceAgreement> returnedServiceAgreements = objectMapper
            .readValue(response,
                new TypeReference<>() {
                });

        assertEquals(returnedServiceAgreements.get(0).getId(), rootMsa.getId());
    }

    @Test
    public void testGetServiceAgreementWhenCreatorIdNotProvided() throws Exception {
        String userId = contextUserId;
        String legalEntityId = rootLegalEntity.getId();
        String query = "";
        String from = "0";
        String cursor = "";
        String size = "10";
        String getUserUrl = "/service-api/v2/users/{id}";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setId(userId);
        user.setLegalEntityId(legalEntityId);

        addStubGet(new UrlBuilder(getUserUrl)
                .addPathParameter(userId)
                .addQueryParameter("skipHierarchyCheck","true")
                .build(),
            user, 200);

        String URL = "/accessgroups/service-agreements/hierarchy";
        String response = executeClientRequest(
            new UrlBuilder(URL)
                .addQueryParameter("userId", userId)
                .addQueryParameter("query", query)
                .addQueryParameter("from", from)
                .addQueryParameter("cursor", cursor)
                .addQueryParameter("size", size)
                .build(), HttpMethod.GET, "admin",
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);
        List<PresentationServiceAgreement> returnedServiceAgreements = objectMapper
            .readValue(response,
                new TypeReference<>() {
                });

        assertEquals(returnedServiceAgreements.get(0).getId(), rootMsa.getId());
    }

    @Test
    public void testGetServiceAgreementWhenUserNotProvided() throws Exception {

        String query = "";
        String from = "0";
        String cursor = "";
        String size = "10";

        String URL = "/accessgroups/service-agreements/hierarchy";
        String response = executeClientRequest(
            new UrlBuilder(URL)
                .addQueryParameter("query", query)
                .addQueryParameter("from", from)
                .addQueryParameter("cursor", cursor)
                .addQueryParameter("size", size)
                .build(), HttpMethod.GET, "admin",
            MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);
        List<PresentationServiceAgreement> returnedServiceAgreements = objectMapper
            .readValue(response,
                new TypeReference<>() {
                });

        assertEquals(returnedServiceAgreements.get(0).getId(), rootMsa.getId());
    }
}
