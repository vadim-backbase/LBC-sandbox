package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;

public class GetServiceAgreementByContextIT extends TestDbWireMock {

    public static final String USER = "USER";

    private static final String url = "/accessgroups/serviceagreements/context";
    private UserContext userContext;

    @Test
    @WithMockUser(username = USER)
    public void testGetServiceAgreementById() throws Exception {

        userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId("user", rootMsa.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext("user", rootMsa.getId())));

        String responseAsString = executeClientRequestWithContext(url, HttpMethod.GET, "", "user",
            ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME, ResourceAndFunctionNameConstants.PRIVILEGE_VIEW,
            userContext, rootLegalEntity.getId());

        ServiceAgreementItem returnedServiceAgreement = objectMapper
            .readValue(responseAsString, ServiceAgreementItem.class);

        assertEquals(rootMsa.getId(), returnedServiceAgreement.getId());
        assertEquals(rootMsa.getName(), returnedServiceAgreement.getName());
        assertEquals(Status.ENABLED, returnedServiceAgreement.getStatus());
    }
}
