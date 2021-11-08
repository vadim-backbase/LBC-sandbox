package com.backbase.accesscontrol.business.serviceagreement;


import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetServiceAgreementParticipantsTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @InjectMocks
    private GetServiceAgreementParticipants getServiceAgreementParticipants;
    @Spy
    private ObjectConverter converter = new ObjectConverter(spy(ObjectMapper.class));


    @Test
    public void shouldSuccessfullyReturnServiceAgreementParticipants() {
        String serviceAgreementId = "001";

        List<Participant> participants = Arrays.asList(
            new Participant()
                .withId("LE-1")
                .withName("Consumer1"),
            new Participant()
                .withId("LE-2")
                .withName("Provider1"));

        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participants);

        InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> businessProcessResult =
            getServiceAgreementParticipants.getServiceAgreementParticipants(new InternalRequest(), serviceAgreementId);

        verify(persistenceServiceAgreementService)
            .getServiceAgreementParticipants(eq(serviceAgreementId));

        List<ServiceAgreementParticipantsGetResponseBody> response = businessProcessResult.getData();
        assertEquals(participants.size(), response.size());
        assertEquals(participants.get(0).getName(), response.get(0).getName());
        assertEquals(participants.get(1).getName(), response.get(1).getName());
    }
}
