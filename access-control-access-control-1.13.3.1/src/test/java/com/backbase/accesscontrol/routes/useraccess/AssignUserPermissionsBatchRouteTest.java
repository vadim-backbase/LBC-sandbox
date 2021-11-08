package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AssignUserPermissionsBatchRouteTest extends BaseTestRoutesBuilder<AssignUserPermissionsBatchRoute> {

    private MockEndpoint mockFunctions;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_BUSINESS_ASSIGN_USER_PERMISSIONS);
        mockFunctions = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_BUSINESS_ASSIGN_USER_PERMISSIONS);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockFunctions.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_ASSIGN_USER_PERMISSIONS_BATCH, "payload");
        mockFunctions.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }

}