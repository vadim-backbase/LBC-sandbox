package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GetServiceAgreementByExternalIdRoute}
 */
public class GetServiceAgreementByExternalIdRouteTest extends
    BaseTestRoutesBuilder<GetServiceAgreementByExternalIdRoute> {

    private MockEndpoint mockGetServiceAgreementByExternalId;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID);
        mockGetServiceAgreementByExternalId = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockGetServiceAgreementByExternalId.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID, "payload");
        mockGetServiceAgreementByExternalId.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
