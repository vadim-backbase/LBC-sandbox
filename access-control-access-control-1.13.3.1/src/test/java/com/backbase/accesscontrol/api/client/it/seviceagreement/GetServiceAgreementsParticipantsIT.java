package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementsServiceApiController;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementsServiceApiController#getServiceAgreementParticipants(String)}
 */
public class GetServiceAgreementsParticipantsIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/service-agreements/{id}/participants";

    @Test
    public void testGetServiceAgreementParticipants() throws Exception {

        String contentAsString = executeServiceRequest(
            new UrlBuilder(URL)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.GET, "USER", rootMsa.getId(), HttpMethod.GET);

        List<LinkedHashMap<String, Object>> returnedListOfData = objectMapper.readValue(contentAsString, List.class);

        List<ServiceAgreementParticipantsGetResponseBody> participants = returnedListOfData
            .stream()
            .map(serviceAgreement -> objectMapper.convertValue(serviceAgreement,
                ServiceAgreementParticipantsGetResponseBody.class))
            .collect(Collectors.toList());

        assertEquals(1, participants.size());
        assertEquals(rootLegalEntity.getId(), participants.get(0).getId());
    }
}
