package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementController#getServiceAgreement}
 */
public class ListServiceAgreementIT extends TestDbWireMock {

    private static final String ENDPOINT_URL = "/accessgroups/serviceagreements";

    private ServiceAgreement serviceAgreement;

    @Autowired
    private DateTimeService dateTimeService;

    @Before
    public void setUp() {
        Map<String, String> additions = new HashMap<>();
        additions.put("externalId", "123456789");

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "id.external", "description", rootLegalEntity,
                null, null);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement.withStartDate(startDate);
        serviceAgreement.withEndDate(endDate);
        serviceAgreement.setAdditions(additions);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);
    }

    @Test
    public void testListAllServiceAgreements() throws Exception {

        String query = "";
        String from = "0";
        String cursor = "";
        String size = "10";

        String response = executeClientRequest(
            new UrlBuilder(ENDPOINT_URL)
                .addQueryParameter("creatorId", rootLegalEntity.getId())
                .addQueryParameter("query", query)
                .addQueryParameter("from", from)
                .addQueryParameter("cursor", cursor)
                .addQueryParameter("size", size)
                .build()
            , HttpMethod.GET, "user", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<ServiceAgreementGetResponseBody> returnedServiceAgreements = objectMapper
            .readValue(response, new TypeReference<List<ServiceAgreementGetResponseBody>>() {
            });

        assertEquals(1, returnedServiceAgreements.size());

        List<Entry<String, String>> entries = new ArrayList<>(serviceAgreement.getAdditions().entrySet());

        assertThat(returnedServiceAgreements, containsInAnyOrder(
            allOf(
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
                hasProperty("additions", hasEntry(is(entries.get(0).getKey()), is(entries.get(0).getValue())))
            )));
    }
}
