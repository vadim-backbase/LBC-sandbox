package com.backbase.accesscontrol.api.service.it.serviceagreements;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetContextServiceAgreementParticipantsIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/service-agreements/usercontext/participants";

    @Test
    public void shouldGetServiceAgreementParticipantsFromSAidFromContext() throws Exception {

        String responseAsString = executeServiceRequest(URL, "",
            "", rootMsa.getId(),
            HttpMethod.GET);

        List<ServiceAgreementParticipantsGetResponseBody> responseData =
            readValue(
                responseAsString,
                new TypeReference<List<ServiceAgreementParticipantsGetResponseBody>>() {
                });

        assertEquals(1, responseData.size());
        assertEquals(rootLegalEntity.getId(), responseData.get(0).getId());
    }
}
