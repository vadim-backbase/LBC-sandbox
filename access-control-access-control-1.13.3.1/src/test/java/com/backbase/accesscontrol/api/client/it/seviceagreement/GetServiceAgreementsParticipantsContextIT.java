package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class GetServiceAgreementsParticipantsContextIT extends TestDbWireMock {

    String URL = "/accessgroups/service-agreements/usercontext/participants";

    @Test
    public void testGetServiceAgreementParticipantsContext() throws Exception {

        ResponseEntity<String> contentAsString = executeClientRequestEntity(URL, HttpMethod.GET,
            "",
            "admin", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<ServiceAgreementParticipantsGetResponseBody> responseData =
            readValue(
                contentAsString.getBody(),
                new TypeReference<List<ServiceAgreementParticipantsGetResponseBody>>() {
                });

        assertEquals(1, responseData.size());
        assertEquals(rootLegalEntity.getId(), responseData.get(0).getId());
    }
}
