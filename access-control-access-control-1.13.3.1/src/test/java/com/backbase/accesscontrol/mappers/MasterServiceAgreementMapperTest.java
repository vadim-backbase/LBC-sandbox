package com.backbase.accesscontrol.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MasterServiceAgreementMapperTest {

    @InjectMocks
    private com.backbase.accesscontrol.mappers.MasterServiceAgreementMapperImpl masterServiceAgreementMapper;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Test
    public void shouldConvertServiceAgreement() {

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId("id")
            .withName("name")
            .withDescription("description")
            .withExternalId("externalId")
            .withState(ServiceAgreementState.DISABLED)
            .withStartDate(dateTimeService.getStartDateFromDateAndTime("2001-01-01", "10:10:10"))
            .withEndDate(dateTimeService.getStartDateFromDateAndTime("2011-11-11", "08:08:08"))
            .withCreatorLegalEntity(new LegalEntity().withId("leid"));
        serviceAgreement.setAddition("key", "value");
        MasterServiceAgreementGetResponseBody response = masterServiceAgreementMapper
            .convertToResponse(serviceAgreement);

        assertEquals(serviceAgreement.getId(), response.getId());
        assertEquals(serviceAgreement.getName(), response.getName());
        assertEquals(serviceAgreement.getDescription(), response.getDescription());
        assertEquals(serviceAgreement.getExternalId(), response.getExternalId());
        assertEquals(serviceAgreement.getState().toString(), response.getStatus().toString());
        assertEquals(serviceAgreement.getCreatorLegalEntity().getId(), response.getCreatorLegalEntity());
        assertEquals("2001-01-01", response.getValidFromDate());
        assertEquals("10:10:10", response.getValidFromTime());
        assertEquals("2011-11-11", response.getValidUntilDate());
        assertEquals("08:08:08", response.getValidUntilTime());
    }
}