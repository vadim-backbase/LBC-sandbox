package com.backbase.accesscontrol.dto;

import static org.junit.Assert.assertEquals;

import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import org.junit.Test;

public class ServiceAgreementAwareTest {

    @Test
    public void shouldSetServiceAgreementId() {
        ServiceAgreementAware exposeFunctionGroupsWithConsumerPayload = new ServiceAgreementAware();
        String id = "id";
        exposeFunctionGroupsWithConsumerPayload.setServiceAgreementId(id);
        assertEquals(id, exposeFunctionGroupsWithConsumerPayload.getServiceAgreementId());
    }

    @Test
    public void shouldSetInternalContext() {
        DefaultInternalRequestContext internalRequestContext = new DefaultInternalRequestContext();
        ServiceAgreementAware exposeFunctionGroupsWithConsumerPayload = new ServiceAgreementAware();
        exposeFunctionGroupsWithConsumerPayload.setInternalRequestContext(internalRequestContext);
        assertEquals(internalRequestContext, exposeFunctionGroupsWithConsumerPayload.getInternalRequestContext());
    }

}