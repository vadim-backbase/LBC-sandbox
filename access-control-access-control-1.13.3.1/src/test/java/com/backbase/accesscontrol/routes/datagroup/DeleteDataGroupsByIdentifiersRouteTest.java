package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeleteDataGroupsByIdentifiersRouteTest extends BaseTestRoutesBuilder<DeleteDataGroupsByIdentifiersRoute> {

    private MockEndpoint mockDeleteDataGroupEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUPS_BY_IDENTIFIERS);
        mockDeleteDataGroupEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUPS_BY_IDENTIFIERS);
    }

    @Test
    public void shouldPassWhenMessageIsConsumedByMockEndpoint() throws Exception {
        mockDeleteDataGroupEndpoint.setExpectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_DELETE_DATA_GROUPS_BY_IDENTIFIERS, "dataGroup");
        mockDeleteDataGroupEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }

}
