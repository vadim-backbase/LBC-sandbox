package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidatePermissionsRouteTest extends BaseTestRoutesBuilder<ValidatePermissionsRoute> {

    private MockEndpoint endpoint;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS);
        endpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        endpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_CHECK_PERMISSIONS, "payload");
        endpoint.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}