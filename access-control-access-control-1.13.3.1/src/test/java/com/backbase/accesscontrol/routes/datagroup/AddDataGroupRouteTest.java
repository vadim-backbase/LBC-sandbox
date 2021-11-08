package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.routes.BaseExtensibleTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddDataGroupRouteTest extends BaseExtensibleTestRoutesBuilder<AddDataGroupRoute> {

    private static final String EXCHANGE_TO_SEND = "{name: 'account-group'}";

    private MockEndpoint mockAddDataGroupEndpoint;
    private MockEndpoint mockValidateDataGroupEndpoint;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_ADD_DATA_GROUP_PERSIST);
        setupAdvice(EndpointConstants.DIRECT_ADD_DATA_GROUP_VALIDATE);
        mockAddDataGroupEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_ADD_DATA_GROUP_PERSIST);
        mockValidateDataGroupEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_ADD_DATA_GROUP_VALIDATE);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessAddDataGroupsEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockAddDataGroupEndpoint.expectedMessageCount(1);
        mockValidateDataGroupEndpoint.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_START_ADD_DATA_GROUP, EXCHANGE_TO_SEND);
        mockAddDataGroupEndpoint.assertIsSatisfied();
        mockValidateDataGroupEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}