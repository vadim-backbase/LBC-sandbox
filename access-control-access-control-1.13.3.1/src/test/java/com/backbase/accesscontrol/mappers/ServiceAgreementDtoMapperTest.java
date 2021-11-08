package com.backbase.accesscontrol.mappers;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.dto.ServiceAgreementDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementDtoMapperTest {

    private ServiceAgreementDtoMapper mapper = Mappers.getMapper(ServiceAgreementDtoMapper.class);

    @Test
    public void shouldMapServiceAgreementPostRequestBodyToServiceAgreementDto() {

        List<Participant> participants = asList(
            new Participant()
                .withId("le-01")
                .withSharingUsers(true)
                .withSharingAccounts(false)
                .withAdmins(newHashSet("admin1", "admin2")),
            new Participant()
                .withId("le-02")
                .withSharingUsers(false)
                .withSharingAccounts(true)
                .withAdmins(newHashSet("admin3", "admin4"))
        );

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody();
        serviceAgreementPostRequestBody.setValidFromDate("2020-01-01");
        serviceAgreementPostRequestBody.setValidFromTime("12:05:10");
        serviceAgreementPostRequestBody.setValidUntilDate("2021-01-01");
        serviceAgreementPostRequestBody.setValidUntilTime("11:55:50");
        serviceAgreementPostRequestBody.setParticipants(participants);

        ServiceAgreementDto serviceAgreementDto = mapper
            .fromServiceAgreementPostRequestBody(serviceAgreementPostRequestBody);

        assertEquals(serviceAgreementPostRequestBody.getValidFromDate(), serviceAgreementDto.getValidFromDate());
        assertEquals(serviceAgreementPostRequestBody.getValidFromTime(), serviceAgreementDto.getValidFromTime());
        assertEquals(serviceAgreementPostRequestBody.getValidUntilDate(), serviceAgreementDto.getValidUntilDate());
        assertEquals(serviceAgreementPostRequestBody.getValidUntilTime(), serviceAgreementDto.getValidUntilTime());

        assertEquals(participants.get(0).getId(), serviceAgreementDto.getParticipants().get(0).getId());
        assertEquals(participants.get(0).getSharingUsers(),
            serviceAgreementDto.getParticipants().get(0).getSharingUsers());
        assertEquals(participants.get(0).getSharingAccounts(),
            serviceAgreementDto.getParticipants().get(0).getSharingAccounts());
        assertTrue(
            serviceAgreementDto.getParticipants().get(0).getAdmins().containsAll(participants.get(0).getAdmins()));

        assertEquals(participants.get(1).getId(), serviceAgreementDto.getParticipants().get(1).getId());
        assertEquals(participants.get(1).getSharingUsers(),
            serviceAgreementDto.getParticipants().get(1).getSharingUsers());
        assertEquals(participants.get(1).getSharingAccounts(),
            serviceAgreementDto.getParticipants().get(1).getSharingAccounts());
        assertTrue(
            serviceAgreementDto.getParticipants().get(1).getAdmins().containsAll(participants.get(1).getAdmins()));
    }

    @Test
    public void shouldMapServiceAgreementSaveToServiceAgreementDto() {

        List<Participant> participants = asList(
            new Participant()
                .withId("le-01")
                .withSharingUsers(true)
                .withSharingAccounts(false)
                .withAdmins(newHashSet("admin1", "admin2")),
            new Participant()
                .withId("le-02")
                .withSharingUsers(false)
                .withSharingAccounts(true)
                .withAdmins(newHashSet("admin3", "admin4"))
        );

        ServiceAgreementSave serviceAgreementSave = new ServiceAgreementSave();
        serviceAgreementSave.setValidFromDate("2020-01-01");
        serviceAgreementSave.setValidFromTime("12:05:10");
        serviceAgreementSave.setValidUntilDate("2021-01-01");
        serviceAgreementSave.setValidUntilTime("11:55:50");
        serviceAgreementSave.setParticipants(newHashSet(participants));

        ServiceAgreementDto serviceAgreementDto = mapper.fromServiceAgreementSave(serviceAgreementSave);

        assertEquals(serviceAgreementSave.getValidFromDate(), serviceAgreementDto.getValidFromDate());
        assertEquals(serviceAgreementSave.getValidFromTime(), serviceAgreementDto.getValidFromTime());
        assertEquals(serviceAgreementSave.getValidUntilDate(), serviceAgreementDto.getValidUntilDate());
        assertEquals(serviceAgreementSave.getValidUntilTime(), serviceAgreementDto.getValidUntilTime());

        assertEquals(participants.get(0).getId(), serviceAgreementDto.getParticipants().get(0).getId());
        assertEquals(participants.get(0).getSharingUsers(),
            serviceAgreementDto.getParticipants().get(0).getSharingUsers());
        assertEquals(participants.get(0).getSharingAccounts(),
            serviceAgreementDto.getParticipants().get(0).getSharingAccounts());
        assertTrue(
            serviceAgreementDto.getParticipants().get(0).getAdmins().containsAll(participants.get(0).getAdmins()));

        assertEquals(participants.get(1).getId(), serviceAgreementDto.getParticipants().get(1).getId());
        assertEquals(participants.get(1).getSharingUsers(),
            serviceAgreementDto.getParticipants().get(1).getSharingUsers());
        assertEquals(participants.get(1).getSharingAccounts(),
            serviceAgreementDto.getParticipants().get(1).getSharingAccounts());
        assertTrue(
            serviceAgreementDto.getParticipants().get(1).getAdmins().containsAll(participants.get(1).getAdmins()));
    }
}