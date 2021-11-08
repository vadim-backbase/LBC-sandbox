package com.backbase.accesscontrol.api.routeextension.legalentity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByIdRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.apache.camel.Produce;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles({"it", "routes"})
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "backbase.communication.outbound=HTTP",
    "backbase.communication.inbound=HTTP"
})
public class GetLegalEntityByIdHookIT {

    @MockBean
    private LegalEntityPAndPService legalEntityPAndPService;
    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_BY_ID)
    private GetLegalEntityByIdRouteProxy getLegalEntityByIdRouteProxy;
    @MockBean
    private ServiceAgreementIdProvider serviceAgreementIdProvider;

    @MockBean
    private AccessControlValidator accessControlValidator;

    @Test
    @WithMockUser("USER")
    public void testGetUser() {
        when(legalEntityPAndPService.getLegalEntityByIdAsResponseBody(anyString()))
            .thenReturn(new LegalEntityGetResponseBody());
        InternalRequest<LegalEntityByIdGetResponseBody> response =
            getLegalEntityByIdRouteProxy.getLegalEntity(new InternalRequest<>(), "id");
        assertEquals("idHook", response.getData().getId());
    }
}
