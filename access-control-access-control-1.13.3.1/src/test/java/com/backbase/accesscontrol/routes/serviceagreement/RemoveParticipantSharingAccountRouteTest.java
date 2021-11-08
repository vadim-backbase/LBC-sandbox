package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RemoveParticipantSharingAccountRouteTest extends
    BaseTestRoutesBuilder<RemoveParticipantSharingAccountRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE);
        mockEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessAddUserInServiceAgreementCorrectlyReceivedByMock()
        throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}