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
public class UpdateBatchAdminsRouteTest extends BaseTestRoutesBuilder<UpdateBatchAdminsRoute> {

    private MockEndpoint mockUpdateAdminsBatch;

    @Before
    public void setup() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_INGEST_ADMINS_UPDATE);
        mockUpdateAdminsBatch = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_INGEST_ADMINS_UPDATE);
    }

    @Test
    public void shouldPassWhenConsumedMessageOnBusinessUpdateAdminsBatchEndpointIsCorrectlyReceivedByMock()
        throws Exception {
        mockUpdateAdminsBatch.expectedMessageCount(1);
        context.start();
        template.sendBody(EndpointConstants.DIRECT_BUSINESS_INGEST_ADMINS_UPDATE, "payload");
        mockUpdateAdminsBatch.assertIsSatisfied();
        context.stop();
    }

    @After
    public void cleanup() throws Exception {
        context.stop();
    }
}
