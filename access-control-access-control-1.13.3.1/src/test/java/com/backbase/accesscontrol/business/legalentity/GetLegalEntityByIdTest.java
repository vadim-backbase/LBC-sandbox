package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_003;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetLegalEntityByIdTest {

    @InjectMocks
    private GetLegalEntityById getLegalEntityById;
    @Mock
    private LegalEntityPAndPService legalEntityPAndPService;
    @Mock
    private AccessControlValidator accessControlValidator;

    @Spy
    private ObjectConverter converter = new ObjectConverter(spy(ObjectMapper.class));

    @Before
    public void setUp() {
        getLegalEntityById = spy(
            new GetLegalEntityById(legalEntityPAndPService, accessControlValidator,
                converter));
    }


    @Test
    public void shouldGetLegalEntityByIdWhenLegalEntityExists() {
        String legalEntityId = "001";

        String key = "externalId";
        String value = "ex id";

        LegalEntityGetResponseBody legalEntityByIdResponse = createLegalEntityByIdGetPandpBody("EX01", null,
            legalEntityId, true, key, value);

        doReturn(legalEntityByIdResponse).when(legalEntityPAndPService)
            .getLegalEntityByIdAsResponseBody(eq(legalEntityId));

        mockUserAccessToEntitlementsRecourse(legalEntityId, false);

        InternalRequest<LegalEntityByIdGetResponseBody> legalEntityById = getLegalEntityById
            .getLegalEntityById(new InternalRequest<>(), legalEntityId);

        assertEquals(legalEntityByIdResponse.getExternalId(), legalEntityById.getData().getExternalId());
        assertEquals(legalEntityByIdResponse.getParentId(), legalEntityById.getData().getParentId());
        assertEquals(legalEntityByIdResponse.getId(), legalEntityById.getData().getId());
        assertEquals(legalEntityByIdResponse.getName(), legalEntityById.getData().getName());
        assertEquals(legalEntityByIdResponse.getType(), legalEntityById.getData().getType());
        assertEquals(legalEntityByIdResponse.getIsParent(), legalEntityById.getData().getIsParent());
        assertEquals(legalEntityByIdResponse.getAdditions(), legalEntityById.getData().getAdditions());
        verify(legalEntityPAndPService, times(1)).getLegalEntityByIdAsResponseBody(eq(legalEntityId));
    }

    @Test
    public void shouldReturnFailedEventWhenUserHasNoAccessToEntitlementsRecourse() {
        String legalEntityId = "001";

        mockUserAccessToEntitlementsRecourse(legalEntityId, true);

        ForbiddenException badRequestException = assertThrows(ForbiddenException.class,
            () -> getLegalEntityById.getLegalEntityById(new InternalRequest<>(), legalEntityId));

        assertThat(badRequestException,
            is(new ForbiddenErrorMatcher(ERR_LE_003.getErrorMessage(), ERR_LE_003.getErrorCode())));
    }

    private void mockUserAccessToEntitlementsRecourse(String legalEntityId, boolean response) {
        when(accessControlValidator
            .userHasNoAccessToEntitlementResource(eq(legalEntityId),
                eq(AccessResourceType.USER_AND_ACCOUNT))).thenReturn(response);
    }

    public static LegalEntityGetResponseBody createLegalEntityByIdGetPandpBody(String externalEntityId,
        String parentEntityId, String entityId, Boolean isParent, String key, String value) {
        LegalEntityGetResponseBody legalEntityGetResponseBody = new LegalEntityGetResponseBody()
            .withExternalId(externalEntityId)
            .withParentId(parentEntityId)
            .withId(entityId)
            .withIsParent(isParent);
        legalEntityGetResponseBody.withAddition(key, value);
        return legalEntityGetResponseBody;
    }
}