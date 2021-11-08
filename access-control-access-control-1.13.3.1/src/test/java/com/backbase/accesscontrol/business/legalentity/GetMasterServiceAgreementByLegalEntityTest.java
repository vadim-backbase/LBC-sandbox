package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.mappers.MasterServiceAgreementMapper;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetMasterServiceAgreementByLegalEntityTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @InjectMocks
    private GetMasterServiceAgreementByLegalEntity service;

    @Mock
    private MasterServiceAgreementMapper masterServiceAgreementMapper;
    @Mock
    private AccessControlValidator accessControlValidator;

    @Test
    public void shouldReturnMasterServiceAgreementByExternalId() {
        InternalRequest<Void> request = new InternalRequest<>();
        String externalLeId = "externalId";

        Date startDate = new Date();
        Date endDate = new Date();
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId("id")
            .withExternalId(externalLeId)
            .withState(ServiceAgreementState.ENABLED)
            .withMaster(true)
            .withCreatorLegalEntity(new LegalEntity().withId("leid"))
            .withStartDate(startDate)
            .withEndDate(endDate);

        when(persistenceLegalEntityService.getMasterServiceAgreementByExternalId(eq("externalId")))
            .thenReturn(serviceAgreement);

        service.getMasterServiceAgreementByExternalLegalEntityId(request, "externalId");

        verify(persistenceLegalEntityService, times(1))
            .getMasterServiceAgreementByExternalId(eq("externalId"));

        verify(masterServiceAgreementMapper, times(1))
            .convertToResponse(eq(serviceAgreement));
    }

    @Test
    public void shouldThrowForbiddenTryingToAccessMsaForParentLe() {
        String externalLeId = "externalId";
        String saId = "saId";

        InternalRequest<Void> request = new InternalRequest<>();

        Date startDate = new Date();
        Date endDate = new Date();
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(saId)
            .withExternalId(externalLeId)
            .withCreatorLegalEntity(new LegalEntity().withId("id"))
            .withState(ServiceAgreementState.ENABLED)
            .withMaster(true)
            .withStartDate(startDate)
            .withEndDate(endDate);

        when(persistenceLegalEntityService.getMasterServiceAgreementByExternalId(eq("externalId")))
            .thenReturn(serviceAgreement);

        when(accessControlValidator.userHasNoAccessToServiceAgreement(eq(saId), eq(AccessResourceType.NONE)))
            .thenReturn(Boolean.TRUE);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> service.getMasterServiceAgreementByExternalLegalEntityId(request, "externalId"));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode())));

        verify(persistenceLegalEntityService, times(1))
            .getMasterServiceAgreementByExternalId(eq("externalId"));

        verify(masterServiceAgreementMapper, times(0))
            .convertToResponse(any(ServiceAgreement.class));
    }

    @Test
    public void shouldPassIfGetMasterServiceAgreementByLegalEntityIdIsInvoked() {
        String legalEntityId = "001";

        ServiceAgreement serviceAgreementItem = new ServiceAgreement()
            .withId("sa-id")
            .withCreatorLegalEntity(new LegalEntity())
            .withStartDate(new Date())
            .withEndDate(new Date())
            .withState(ServiceAgreementState.ENABLED);

        when(accessControlValidator.userHasNoAccessToEntitlementResource(
            eq(legalEntityId), eq(AccessResourceType.NONE))).thenReturn(false);

        when(persistenceLegalEntityService
            .getMasterServiceAgreement(anyString()))
            .thenReturn(serviceAgreementItem);

        InternalRequest<Void> request = getInternalRequest(null);
        service.getMasterServiceAgreementByLegalEntityId(request, legalEntityId);

        verify(persistenceLegalEntityService, times(1))
            .getMasterServiceAgreement(eq(legalEntityId));
        verify(masterServiceAgreementMapper, times(1))
            .convertToResponse(eq(serviceAgreementItem));
    }

    @Test
    public void shouldThrowForbiddenWhenAccessControlValidationFails() {
        String legalEntityId = "001";

        when(accessControlValidator.userHasNoAccessToEntitlementResource(
            eq(legalEntityId), eq(AccessResourceType.NONE))).thenReturn(true);

        InternalRequest<Void> request = getInternalRequest(null);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> service.getMasterServiceAgreementByLegalEntityId(request, legalEntityId));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode())));

        verify(persistenceLegalEntityService, times(0))
            .getMasterServiceAgreement(anyString());
        verify(masterServiceAgreementMapper, times(0))
            .convertToResponse(any(ServiceAgreement.class));
    }
}