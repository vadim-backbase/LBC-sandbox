package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetUnexposedUsersForServiceAgreementRouteTest extends BaseTestRoutesBuilder<GetUnexposedUsersRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_UNEXPOSED_USERS);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_GET_UNEXPOSED_USERS);
    }

    @Test
    public void mockedEndpointShouldReceiveMessage() throws Exception {
        context.start();
        mockEndpoint.expectedMessageCount(1);
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_UNEXPOSED_USERS, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
