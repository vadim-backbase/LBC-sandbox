package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GetFunctionGroupByIdRoute}
 */
public class GetFunctionGroupRouteTest extends BaseTestRoutesBuilder<GetFunctionGroupByIdRoute> {

    private MockEndpoint mockGetFunctionGroupById;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_FUNCTION_GROUP_BY_ID);
        mockGetFunctionGroupById = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_GET_FUNCTION_GROUP_BY_ID);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockGetFunctionGroupById.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_FUNCTION_GROUP_BY_ID, "payload");
        mockGetFunctionGroupById.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
