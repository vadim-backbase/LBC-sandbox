package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateUsersInServiceAgreementRouteTest extends BaseTestRoutesBuilder<UpdateUsersInServiceAgreementRoute> {

    private MockEndpoint mockUpdateUsersBatch;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_UPDATE_USERS_IN_SA);
        mockUpdateUsersBatch = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_UPDATE_USERS_IN_SA);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessUpdateAdminsBatchEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockUpdateUsersBatch.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_UPDATE_USERS_IN_SA, "payload");
        mockUpdateUsersBatch.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
