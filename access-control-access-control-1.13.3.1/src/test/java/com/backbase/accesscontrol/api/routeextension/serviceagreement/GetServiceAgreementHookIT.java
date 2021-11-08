package com.backbase.accesscontrol.api.routeextension.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import org.apache.camel.Produce;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"live", "routes", "h2"})
public class GetServiceAgreementHookIT extends TestDbWireMock {

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_ID)
    private GetServiceAgreementRouteProxy getServiceAgreementRouteProxy;

    @Test
    public void testGetServiceAgreementHook() {

        ServiceAgreementItemGetResponseBody serviceAgreementResponse = getServiceAgreementRouteProxy
            .getServiceAgreementById(getVoidInternalRequest(), rootMsa.getId()).getData();
        assertEquals(rootMsa.getName(), serviceAgreementResponse.getName());
        assertEquals("Service agreement was in post hook", serviceAgreementResponse.getDescription());
    }
}
