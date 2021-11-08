package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ListFunctionGroupsRoute}
 */
public class ListFunctionGroupsRouteTest extends BaseTestRoutesBuilder<ListFunctionGroupsRoute> {

    private MockEndpoint getFunctionalGroupsMock;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_LIST_FUNCTION_GROUPS);
        getFunctionalGroupsMock = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_LIST_FUNCTION_GROUPS);
    }

    @Test
    public void shouldPassWhenMessageSentToBossinessListFunctionGroupsIsConsumedByMockEndpoint() throws Exception {
        getFunctionalGroupsMock.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_LIST_FUNCTION_GROUPS, "payload");
        getFunctionalGroupsMock.assertIsSatisfied();
        context.stop();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
