package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_061;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import java.io.IOException;
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
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementServiceApiController#getServiceAgreementExternalId}
 */
public class GetServiceAgreementByExternalIdIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements/external/{externalId}";

    private ServiceAgreement serviceAgreement;

    @Before
    public void setup(){
        String key = "externalId";
        String value = "123456789";
        Map<String, String> additions = new HashMap<>();
        additions.put(key, value);

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
    public void testGetServiceAgreementByExternalId() throws IOException {

        String responseAsString = executeRequest(
            new UrlBuilder(URL)
                .addPathParameter(serviceAgreement.getExternalId())
                .build(),
            null,
            HttpMethod.GET);

        ServiceAgreementExternalIdGetResponseBody returnedServiceAgreement = readValue(
            responseAsString,
            ServiceAgreementExternalIdGetResponseBody.class);

        assertEquals(serviceAgreement.getId(), returnedServiceAgreement.getId());
        assertEquals(serviceAgreement.getName(), returnedServiceAgreement.getName());
        assertEquals(serviceAgreement.getExternalId(), returnedServiceAgreement.getExternalId());
        assertEquals(serviceAgreement.getState().toString(), returnedServiceAgreement.getStatus().toString());

        assertEquals(DateFormatterUtil.utcFormatDateOnly(serviceAgreement.getStartDate()), returnedServiceAgreement.getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(serviceAgreement.getStartDate()), returnedServiceAgreement.getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(serviceAgreement.getEndDate()), returnedServiceAgreement.getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(serviceAgreement.getEndDate()), returnedServiceAgreement.getValidUntilTime());

        List<Entry<String, String>> additions = new ArrayList<>(serviceAgreement.getAdditions().entrySet());
        assertEquals(additions.size(), returnedServiceAgreement.getAdditions().size());
        assertTrue(returnedServiceAgreement.getAdditions().containsKey(additions.get(0).getKey()));
        assertTrue(returnedServiceAgreement.getAdditions().containsValue(additions.get(0).getValue()));
    }

    @Test
    public void shouldThrowAnExceptionWhenNoSaByExternalIdFound() {

        NotFoundException exception = assertThrows(NotFoundException.class, () -> executeRequest(
            new UrlBuilder(URL)
                .addPathParameter("INVALID_SA_EXTERNAL_ID")
                .build(),
            null,
            HttpMethod.GET));

        assertThat(exception, new NotFoundErrorMatcher(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode()));
    }
}
