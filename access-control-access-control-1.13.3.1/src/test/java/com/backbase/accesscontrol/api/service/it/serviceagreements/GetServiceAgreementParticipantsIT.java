package com.backbase.accesscontrol.api.service.it.serviceagreements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetServiceAgreementParticipantsIT extends TestDbWireMock {

    private static String URL = "/accessgroups/service-agreements/{id}/participants";

    @Test
    public void shouldGetServiceAgreementParticipantsFromSAid() throws IOException {

        String responseAsString = executeRequest(new UrlBuilder(URL).addPathParameter(rootMsa.getId()).build(),
            "", HttpMethod.GET);

        List<Participant> responseData =
            readValue(responseAsString,
                new TypeReference<List<Participant>>() {
                });

        assertEquals(responseData.get(0).getId(), rootLegalEntity.getId());
        assertTrue(responseData.size() == 1);
    }
}

