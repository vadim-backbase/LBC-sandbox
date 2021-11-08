package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetDataGroupByIdRouteTest extends BaseTestRoutesBuilder<GetDataGroupByIdRoute> {

    private static final String EXCHANGE_TO_SEND = "{name: 'account-group'}";

    private MockEndpoint mockGetDataGroupByIdEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_DATA_GROUP_BY_ID);
        mockGetDataGroupByIdEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_GET_DATA_GROUP_BY_ID);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessGetDataGroupByIdEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockGetDataGroupByIdEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_GET_DATA_GROUP_BY_ID, EXCHANGE_TO_SEND);
        mockGetDataGroupByIdEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}