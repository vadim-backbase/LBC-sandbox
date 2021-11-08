package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GetServiceAgreementParticipantsRoute}
 */
public class GetServiceAgreementParticipantsRouteTest extends
    BaseTestRoutesBuilder<GetServiceAgreementParticipantsRoute> {

    private MockEndpoint mockGetServiceAgreementParticipants;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_PARTICIPANTS);
        mockGetServiceAgreementParticipants = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_PARTICIPANTS);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockGetServiceAgreementParticipants.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_PARTICIPANTS, "payload");
        mockGetServiceAgreementParticipants.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
