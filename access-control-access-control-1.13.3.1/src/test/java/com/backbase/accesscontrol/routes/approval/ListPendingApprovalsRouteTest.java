package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListPendingApprovalsRouteTest extends BaseTestRoutesBuilder<ListPendingApprovalsRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_LIST_PENDING_APPROVALS);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_LIST_PENDING_APPROVALS);
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }

    @Test
    public void shouldPassWhenMessageIsConsumedByMockEndpoint() throws Exception {
        mockEndpoint.setExpectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_LIST_PENDING_APPROVALS, "payload");
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

}