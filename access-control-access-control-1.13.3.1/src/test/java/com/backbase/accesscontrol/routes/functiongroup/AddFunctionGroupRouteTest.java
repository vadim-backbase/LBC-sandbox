package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddFunctionGroupRouteTest extends BaseTestRoutesBuilder<AddFunctionGroupRoute> {

    private MockEndpoint mockAddFunctionGroupEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_ADD_FUNCTION_GROUP);
        mockAddFunctionGroupEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_ADD_FUNCTION_GROUP);
    }

    @Test
    public void shouldPassWhenMessageIsConsumedByMockEndpoint() throws Exception {
        mockAddFunctionGroupEndpoint.setExpectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_ADD_FUNCTION_GROUP, "functionGroup");
        mockAddFunctionGroupEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }

}
