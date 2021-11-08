package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateFunctionGroupByIdRouteTest extends BaseTestRoutesBuilder<UpdateFunctionGroupByIdRoute> {

    private MockEndpoint mockUpdateFunctionGroupEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP_BY_ID);
        mockUpdateFunctionGroupEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP_BY_ID);
    }

    @Test
    public void shouldPassWhenMessageIsConsumedByMockEndpoint() throws Exception {
        mockUpdateFunctionGroupEndpoint.setExpectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP_BY_ID, "functionGroup");
        mockUpdateFunctionGroupEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }

}
