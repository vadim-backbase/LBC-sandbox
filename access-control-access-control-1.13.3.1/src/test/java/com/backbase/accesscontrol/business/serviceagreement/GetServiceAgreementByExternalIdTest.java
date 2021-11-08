package com.backbase.accesscontrol.business.serviceagreement;


import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_061;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.mappers.ServiceAgreementGetByExternalIdMapper;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetServiceAgreementByExternalIdTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock
    private ServiceAgreementGetByExternalIdMapper serviceAgreementMapper;

    @InjectMocks
    private GetServiceAgreementByExternalId getServiceAgreementByExternalId;

    @Test
    public void shouldPassIfGetServiceAgreementByExternalIdIsInvokedInServiceWithIdParameter() {
        String serviceAgreementId = "001";
        String serviceAgreementName = "name";
        String externalId = "ex-id";

        ServiceAgreement data = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withExternalId(externalId)
            .withState(ServiceAgreementState.DISABLED)
            .withName(serviceAgreementName);

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(eq(externalId)))
            .thenReturn(Optional.of(data));

        getServiceAgreementByExternalId.getServiceAgreementByExternalId(new InternalRequest(), externalId);

        verify(persistenceServiceAgreementService)
            .getServiceAgreementResponseBodyByExternalId(eq(externalId));

        verify(serviceAgreementMapper, times(1)).mapSingle(refEq(data));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEmptyReturnedFromDomainService() {
        String externalId = "ex-id";

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(eq(externalId)))
            .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
            () -> getServiceAgreementByExternalId.getServiceAgreementByExternalId(new InternalRequest(), externalId));

        assertThat(notFoundException,
            is(new NotFoundErrorMatcher(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode())));

        verify(persistenceServiceAgreementService)
            .getServiceAgreementResponseBodyByExternalId(eq(externalId));

        verify(serviceAgreementMapper, times(0)).mapSingle(any(ServiceAgreement.class));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenCalledWithNullExternalId() {
        String externalId = "ex-id";

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(eq(externalId)))
            .thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
            () -> getServiceAgreementByExternalId.getServiceAgreementByExternalId(new InternalRequest(), externalId));

        assertThat(notFoundException,
            is(new NotFoundErrorMatcher(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode())));

        verify(persistenceServiceAgreementService)
            .getServiceAgreementResponseBodyByExternalId(eq(externalId));

        verify(serviceAgreementMapper, times(0)).mapSingle(any(ServiceAgreement.class));
    }
}
