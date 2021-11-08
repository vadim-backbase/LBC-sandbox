package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.routes.BaseTestRoutesBuilder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetMasterServiceAgreementByLegalEntityIdRouteTest extends
    BaseTestRoutesBuilder<GetMasterServiceAgreementByLegalEntityIdRoute> {

    @EndpointInject(value = "mock:endpoint")
    private MockEndpoint mockEndpoint;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID)
    private ProducerTemplate producer;

    @Before
    public void initialize() throws Exception {
        setupAdvice(EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID);
        mockEndpoint = getMockEndpoint(
            "mock:" + EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID);
    }

    @Test
    public void mockedEndpointShouldReceiveMessage() throws Exception {
        InternalRequest<ServiceAgreementItem> inRequest = new InternalRequest<>();

        context.start();

        mockEndpoint.expectedMessageCount(1);
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
