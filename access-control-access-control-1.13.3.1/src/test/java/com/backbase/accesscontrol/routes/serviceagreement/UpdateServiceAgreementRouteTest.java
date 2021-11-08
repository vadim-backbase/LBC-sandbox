package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateServiceAgreementRouteTest extends BaseTestRoutesBuilder<UpdateServiceAgreementRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_UPDATE_SERVICE_AGREEMENT);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_UPDATE_SERVICE_AGREEMENT);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessUpdateServiceAgreementEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_UPDATE_SERVICE_AGREEMENT, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
