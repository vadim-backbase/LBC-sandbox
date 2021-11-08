package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GetServiceAgreementRoute}
 */
public class GetServiceAgreementRouteTest extends BaseTestRoutesBuilder<GetServiceAgreementRoute> {

    private MockEndpoint mockGetServiceAgreementById;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_ID);
        mockGetServiceAgreementById = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_ID);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockGetServiceAgreementById.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_ID, "payload");
        mockGetServiceAgreementById.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
