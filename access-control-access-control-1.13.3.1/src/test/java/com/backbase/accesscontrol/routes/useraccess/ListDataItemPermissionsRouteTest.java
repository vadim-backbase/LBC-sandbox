package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ListPrivilegesRoute}
 */
public class ListDataItemPermissionsRouteTest extends BaseTestRoutesBuilder<ListDataItemPrivilegesRoute> {

    private MockEndpoint mockFunctions;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_LIST_DATA_ITEM_PRIVILEGES);
        mockFunctions = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_LIST_DATA_ITEM_PRIVILEGES);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockFunctions.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_LIST_DATA_ITEM_PRIVILEGES, "payload");
        mockFunctions.assertIsSatisfied();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
