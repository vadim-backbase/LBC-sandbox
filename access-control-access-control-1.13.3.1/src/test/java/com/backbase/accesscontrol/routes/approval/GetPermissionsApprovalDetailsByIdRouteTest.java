package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetPermissionsApprovalDetailsByIdRouteTest extends
    BaseTestRoutesBuilder<GetPermissionsApprovalDetailsByIdRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_PERMISSIONS_APPROVAL_BY_ID);
        mockEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_GET_PERMISSIONS_APPROVAL_BY_ID);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_PERMISSIONS_APPROVAL_BY_ID, "payload");
        mockEndpoint.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }

}