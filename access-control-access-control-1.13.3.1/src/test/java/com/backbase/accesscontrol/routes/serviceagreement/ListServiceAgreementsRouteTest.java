package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListServiceAgreementsRouteTest extends BaseTestRoutesBuilder<ListServiceAgreementsRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessListServiceAgreementsEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_LIST_SERVICE_AGREEMENTS, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
