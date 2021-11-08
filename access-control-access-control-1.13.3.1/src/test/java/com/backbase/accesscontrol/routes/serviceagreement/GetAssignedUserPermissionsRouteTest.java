package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetAssignedUserPermissionsRouteTest extends
    BaseTestRoutesBuilder<GetAssignedUserPermissionsRoute> {


    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_GET_ASSIGNED_USERS_PERMISSIONS_INTERNAL);
        mockEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_GET_ASSIGNED_USERS_PERMISSIONS_INTERNAL);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_ASSIGNED_USERS_PERMISSIONS, "payload");
        mockEndpoint.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
