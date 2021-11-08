package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GetOrCheckArrangementPrivilegesRouteTest extends BaseTestRoutesBuilder<GetArrangementPrivilegesRoute> {

    private MockEndpoint mockEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_LIST_ARRANGEMENT_PRIVILEGES);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_LIST_ARRANGEMENT_PRIVILEGES);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_LIST_ARRANGEMENT_PRIVILEGES, "payload");
        mockEndpoint.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}