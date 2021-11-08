package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_003;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.mappers.RetriveLegalEntityMapper;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetLegalEntityByExternalIdTest {

    @Spy
    private RetriveLegalEntityMapper retriveLegalEntityMapper = Mappers.getMapper(RetriveLegalEntityMapper.class);
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private AccessControlValidator accessControlValidator;
    @InjectMocks
    private GetLegalEntityByExternalId getLegalEntityByExternalIdService;


    @Test
    public void shouldReturnLegalEntityByExternalId() {
        String id = "1";
        String externalId = "123";
        String name = "Name";
        InternalRequest<Void> internalRequest = new InternalRequest<>();

        mockPandpResponse(id, externalId, name);
        mockUserAccessToEntitlementsRecourse(id, false);
        InternalRequest<LegalEntityByExternalIdGetResponseBody> entityByExternalIdGetResponseBodyInternalRequest
            = getLegalEntityByExternalIdService.getLegalEntityByExternalId(internalRequest, externalId);

        assertEquals(id, entityByExternalIdGetResponseBodyInternalRequest.getData().getId());
        assertEquals(externalId, entityByExternalIdGetResponseBodyInternalRequest.getData().getExternalId());
        assertEquals(name, entityByExternalIdGetResponseBodyInternalRequest.getData().getName());

        verify(persistenceLegalEntityService, times(1)).getLegalEntityByExternalId(eq(externalId), eq(true));
    }

    @Test
    public void shouldReturnFailedEventWhenUserHasNoAccessToEntitlementsRecourse() {
        String id = "1";
        String externalId = "123";
        String name = "Name";
        InternalRequest<Void> internalRequest = new InternalRequest<>();

        mockPandpResponse(id, externalId, name);
        mockUserAccessToEntitlementsRecourse(id, true);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> getLegalEntityByExternalIdService.getLegalEntityByExternalId(internalRequest, externalId));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_LE_003.getErrorMessage(), ERR_LE_003.getErrorCode())));
    }

    private void mockPandpResponse(String id, String externalId, String name) {
        LegalEntity result = new LegalEntity().withId(id).withExternalId(externalId).withName(name);

        when(persistenceLegalEntityService.getLegalEntityByExternalId(eq(externalId), eq(true)))
            .thenReturn(result);
    }

    private void mockUserAccessToEntitlementsRecourse(String legalEntityId, boolean response) {
        when(accessControlValidator
            .userHasNoAccessToEntitlementResource(eq(legalEntityId), eq(AccessResourceType.USER_AND_ACCOUNT)))
            .thenReturn(response);
    }
}
