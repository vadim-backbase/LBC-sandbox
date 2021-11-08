package com.backbase.accesscontrol.api.client.it.usercontext;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UserContextController;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link UserContextController#getUserContextServiceAgreements
 * (String, Integer, String, Integer, HttpServletRequest, HttpServletResponse)
 */
public class UserContextsGetIT extends TestDbWireMock {

    private final String TEST_URL = "/accessgroups/usercontext/serviceagreements";
    private final String USER = "USER";

    @Test
    public void shouldGetUserContextForRootMSA() throws Exception {

        String response = executeClientRequest(new UrlBuilder(TEST_URL)
            .build(), HttpMethod.GET, "USER"
        );

        List<UserContextServiceAgreementsGetResponseBody> list = readValue(response,
            new TypeReference<List<UserContextServiceAgreementsGetResponseBody>>() {
            });
        assertEquals(1, list.size());
        assertEquals(rootMsa.getExternalId(), list.get(0).getExternalId());
    }

    @Test
    public void shouldReturnNothingWhenOutOfRange() throws Exception {

        String response = executeClientRequest(new UrlBuilder(TEST_URL)
            .addQueryParameter("from", "5")
            .addQueryParameter("size", "10")
            .build(), HttpMethod.GET, "USER"
        );

        List<UserContextServiceAgreementsGetResponseBody> list = readValue(response,
            new TypeReference<List<UserContextServiceAgreementsGetResponseBody>>() {
            });
        assertEquals(0, list.size());
    }
}