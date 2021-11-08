package com.backbase.accesscontrol.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementGetByExternalIdMapperTest {

    @InjectMocks
    private com.backbase.accesscontrol.mappers.ServiceAgreementGetByExternalIdMapperImpl serviceAgreementGetByExternalIdMapper;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Test
    public void shouldConvertListOfSaToServiceAgreementGetResponseBody() {
        String creatorId = "LE-01";
        String serviceAgreementId = "SA-01";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ServiceAgreement firstSa = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withExternalId("extSa01")
            .withName("sa01")
            .withDescription("sa01-description")
            .withState(ServiceAgreementState.DISABLED)
            .withCreatorLegalEntity(new LegalEntity().withId(creatorId))
            .withStartDate(startDate)
            .withEndDate(endDate)
            .withMaster(true)
            .withAdditions(new HashMap<String, String>() {{
                put("add-01", "add-01-01");
                put("add-012", "add-01-02");
            }});
        ServiceAgreement secondSa = new ServiceAgreement()
            .withId("SA-02")
            .withExternalId("extSa02")
            .withName("sa02")
            .withDescription("sa02-description")
            .withState(ServiceAgreementState.ENABLED)
            .withCreatorLegalEntity(new LegalEntity().withId(creatorId))
            .withStartDate(startDate)
            .withEndDate(endDate)
            .withMaster(false)
            .withAdditions(new HashMap<String, String>() {{
                put("add-02", "add-02-01");
                put("add-022", "add-02-02");
            }});
        List<ServiceAgreement> serviceAgreements = Arrays.asList(firstSa, secondSa);

        List<ServiceAgreementExternalIdGetResponseBody> response = serviceAgreementGetByExternalIdMapper
            .mapList(serviceAgreements);

        assertThat(response, containsInAnyOrder(
            getServiceAgreementExternalIdGetResponseBodyMatcher(firstSa),
            getServiceAgreementExternalIdGetResponseBodyMatcher(secondSa)
        ));
    }

    private Matcher<ServiceAgreementExternalIdGetResponseBody> getServiceAgreementExternalIdGetResponseBodyMatcher(
        ServiceAgreement serviceAgreement) {
        List<Entry<String, String>> entries = new ArrayList<>(serviceAgreement.getAdditions().entrySet());
        return allOf(
            hasProperty("id", is(serviceAgreement.getId())),
            hasProperty("externalId", is(serviceAgreement.getExternalId())),
            hasProperty("name", is(serviceAgreement.getName())),
            hasProperty("description", is(serviceAgreement.getDescription())),
            hasProperty("isMaster", is(serviceAgreement.isMaster())),
            hasProperty("status", is(Status.fromValue(serviceAgreement.getState().toString()))),
            hasProperty("creatorLegalEntity", is(serviceAgreement.getCreatorLegalEntity().getId())),
            hasProperty("validFromDate", is(dateTimeService.getStringDateFromDate(serviceAgreement.getStartDate()))),
            hasProperty("validFromTime", is(dateTimeService.getStringTimeFromDate(serviceAgreement.getStartDate()))),
            hasProperty("validUntilDate", is(dateTimeService.getStringDateFromDate(serviceAgreement.getEndDate()))),
            hasProperty("validUntilTime", is(dateTimeService.getStringTimeFromDate(serviceAgreement.getEndDate()))),
            hasProperty("additions", hasEntry(is(entries.get(0).getKey()), is(entries.get(0).getValue()))),
            hasProperty("additions", hasEntry(is(entries.get(1).getKey()), is(entries.get(1).getValue())))
        );
    }
}