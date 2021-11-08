package com.backbase.accesscontrol.api.client.it.user;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CheckUserPermissionsIT extends TestDbWireMock {

    public static final String URL = "/accessgroups/users/user-permissions";

    @Test
    @SuppressWarnings("squid:S2699")
    public void testUserPermissionsCheckWithStatusOk() throws Exception {

        executeClientRequest(new UrlBuilder(URL)
            .addQueryParameter("functionName", SERVICE_AGREEMENT_FUNCTION_NAME)
            .addQueryParameter("resourceName", SERVICE_AGREEMENT_RESOURCE_NAME)
            .addQueryParameter("privileges", PRIVILEGE_VIEW).build(),
            HttpMethod.GET, "user", SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_VIEW);
    }

    @Test(expected = ForbiddenException.class)
    @SuppressWarnings("squid:S2699")
    public void testUserPermissionsCheckWithStatusForbidden() throws Exception {

        executeClientRequest(new UrlBuilder(URL)
                .addQueryParameter("functionName", SERVICE_AGREEMENT_FUNCTION_NAME)
                .addQueryParameter("resourceName", SERVICE_AGREEMENT_RESOURCE_NAME)
                .addQueryParameter("privileges", PRIVILEGE_VIEW).build(),
            HttpMethod.GET, "user", SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_CREATE);
    }
}
