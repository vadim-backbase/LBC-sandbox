package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateLegalEntityRouteTest extends BaseTestRoutesBuilder<CreateLegalEntityRoute> {

    @EndpointInject(value = "mock:endpoint")
    private MockEndpoint mockEndpoint;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_CREATE_LEGAL_ENTITY)
    private ProducerTemplate producer;

    @Before
    public void initialize() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_CREATE_LEGAL_ENTITY);
        mockEndpoint = getMockEndpoint("mock:" + EndpointConstants.DIRECT_DEFAULT_CREATE_LEGAL_ENTITY);
    }

    @Test
    public void mockedEndpointShouldReceiveMessage() throws Exception {
        context.start();
        mockEndpoint.expectedMessageCount(1);

        InternalRequest<LegalEntitiesPostResponseBody> inRequest = new InternalRequest<>();

        mockEndpoint.expectedBodiesReceived(inRequest);
        producer.sendBody(inRequest);
        mockEndpoint.assertIsSatisfied();
        context.stop();
    }

    @After
    public void clean() throws Exception {
        context.stop();
    }
}
