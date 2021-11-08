package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link UpdateServiceAgreementAdminsRoute}
 */
public class UpdateServiceAgreementAdminsRouteTest extends BaseTestRoutesBuilder<UpdateServiceAgreementAdminsRoute> {

    private MockEndpoint mockUpdateAdmins;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_UPDATE_ADMINS);
        mockUpdateAdmins = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_UPDATE_ADMINS);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessUpdateAdminsEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockUpdateAdmins.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_UPDATE_ADMINS, "payload");
        mockUpdateAdmins.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
