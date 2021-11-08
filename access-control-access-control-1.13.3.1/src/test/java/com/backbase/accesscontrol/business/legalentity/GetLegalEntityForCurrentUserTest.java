package com.backbase.accesscontrol.business.legalentity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetLegalEntityForCurrentUserTest {

    private GetLegalEntityForCurrentUser getLegalEntityForCurrentUser;

    @Mock
    private InternalRequest<Void> presentationVoidRequest;
    @Mock
    private LegalEntityPAndPService legalEntityPAndPService;
    @Mock
    private UserContextUtil userContextUtil;
    @Spy
    private ObjectConverter objectConverter = new ObjectConverter(spy(ObjectMapper.class));

    @Before
    public void setUp() {
        presentationVoidRequest = new InternalRequest<>();
        getLegalEntityForCurrentUser = spy(new GetLegalEntityForCurrentUser(objectConverter,
            legalEntityPAndPService, userContextUtil));
    }

    @Test
    public void getLegalEntityForCurrentUser() {
        String leId = "internal-id";

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userid", leId));

        LegalEntityGetResponseBody legalEntityGetResponseBody = new LegalEntityGetResponseBody()
            .withId(leId)
            .withExternalId("external-id")
            .withName("name")
            .withParentId(null)
            .withIsParent(false);

        when(legalEntityPAndPService
            .getLegalEntityByIdAsResponseBody(eq(legalEntityGetResponseBody.getId())))
            .thenReturn(legalEntityGetResponseBody);

        InternalRequest<LegalEntityForUserGetResponseBody> legalEntityForCurrentUser =
            getLegalEntityForCurrentUser.getLegalEntityForCurrentUser(presentationVoidRequest);

        LegalEntityForUserGetResponseBody response = legalEntityForCurrentUser.getData();

        assertEquals(legalEntityGetResponseBody.getId(), response.getId());
        assertEquals(legalEntityGetResponseBody.getExternalId(), response.getExternalId());
        assertEquals(legalEntityGetResponseBody.getName(), response.getName());
        assertEquals(legalEntityGetResponseBody.getParentId(), response.getParentId());
        assertEquals(legalEntityGetResponseBody.getIsParent(), response.getIsParent());
    }
}