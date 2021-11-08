package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeleteDataGroupRouteTest extends BaseTestRoutesBuilder<DeleteDataGroupRoute> {

    private MockEndpoint mockDeleteDataGroupById;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUP);
        mockDeleteDataGroupById = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUP);
    }

    @Test
    public void shouldPassWhenMessageCorrectlySentAndConsumedByMockEndpoint() throws Exception {
        mockDeleteDataGroupById.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUP, "payload");
        mockDeleteDataGroupById.assertIsSatisfied();
        context.stop();
    }


    @After
    public void clean() throws Exception {
        context.stop();
    }
}
