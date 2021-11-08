package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateAssignUsersPermissionsRouteTest extends BaseTestRoutesBuilder<UpdateAssignUsersPermissionsRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessAddUserInServiceAgreementCorrectlyReceivedByMock()
        throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}