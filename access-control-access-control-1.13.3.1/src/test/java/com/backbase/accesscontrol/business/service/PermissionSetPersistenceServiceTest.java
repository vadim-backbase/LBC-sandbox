package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_102;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.aps.CreatePermissionSetHandler;
import com.backbase.accesscontrol.business.persistence.aps.DeletePermissionSetHandler;
import com.backbase.accesscontrol.business.persistence.aps.UpdatePermissionSetHandler;
import com.backbase.accesscontrol.dto.IdentifierPair;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.PermissionSetValidationUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSetPersistenceServiceTest {

    @Mock
    private CreatePermissionSetHandler createPermissionSetHandler;
    @Mock
    private DeletePermissionSetHandler deletePermissionSetHandler;
    @Mock
    private UpdatePermissionSetHandler updatePermissionSetHandler;
    @InjectMocks
    private PermissionSetPersistenceService persistenceService;
    @Spy
    private PermissionSetValidationUtil permissionSetValidationUtil;

    @Test
    public void shouldCallPersistence() {
        BigDecimal expectedInternalIdResponse = new BigDecimal(1234);

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        PresentationPermissionSet persistencePermissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        when(createPermissionSetHandler.handleRequest(isA(EmptyParameterHolder.class), eq(persistencePermissionSet)))
            .thenReturn(expectedInternalIdResponse);

        PresentationInternalIdResponse internalIdResponse = persistenceService
            .createPermissionSet(permissionSet);

        verify(createPermissionSetHandler, times(1))
            .handleRequest(isA(EmptyParameterHolder.class), eq(persistencePermissionSet));
        assertEquals(expectedInternalIdResponse, internalIdResponse.getId());
    }

    @Test
    public void shouldCallPersistenceClientOnDelete() {
        persistenceService.deletePermissionSet("id", "1");
        verify(deletePermissionSetHandler).handleRequest(eq(new IdentifierPair("id", "1")), eq(null));
    }

    @Test
    public void shouldCallPersistenceClientOnUpdate() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        PresentationPermissionSetItemPut persistencePermissionSetItemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        persistenceService.updatePermissionSet(itemPut);
        verify(updatePermissionSetHandler)
            .handleRequest(isA(EmptyParameterHolder.class), eq(persistencePermissionSetItemPut));
    }

    @Test
    public void shouldThrowBadRequestOnUpdateApsWHenBothIdentifiersPresent() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L)))
                .withNameIdentifiers(asSet("APS name")))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> persistenceService.updatePermissionSet(itemPut));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestOnUpdateApsWHenEmptyListOfIdentifiers() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Collections.emptySet()))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> persistenceService.updatePermissionSet(itemPut));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));
    }
}