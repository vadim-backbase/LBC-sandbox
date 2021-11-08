package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IngestFunctionGroupRouteTest extends BaseTestRoutesBuilder<IngestFunctionGroupRoute> {

    private MockEndpoint mockIngestFunctionGroupEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_INGEST_FUNCTION_GROUP);
        mockIngestFunctionGroupEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_INGEST_FUNCTION_GROUP);
    }

    @Test
    public void shouldPassWhenMessageIsConsumedByMockEndpoint() throws Exception {
        mockIngestFunctionGroupEndpoint.setExpectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_INGEST_FUNCTION_GROUP, "functionGroup");
        mockIngestFunctionGroupEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }

}
