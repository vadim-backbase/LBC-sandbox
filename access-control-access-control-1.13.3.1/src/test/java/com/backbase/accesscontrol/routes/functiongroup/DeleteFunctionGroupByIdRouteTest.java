package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeleteFunctionGroupByIdRouteTest extends BaseTestRoutesBuilder<DeleteFunctionGroupByIdRoute> {

    private MockEndpoint mockDeleteFunctionGroupById;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID);
        mockDeleteFunctionGroupById = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockDeleteFunctionGroupById.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID, "payload");
        mockDeleteFunctionGroupById.assertIsSatisfied();
        context.stop();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }

}
