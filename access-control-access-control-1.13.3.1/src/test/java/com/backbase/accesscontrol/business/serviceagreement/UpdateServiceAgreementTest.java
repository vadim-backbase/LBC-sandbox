package com.backbase.accesscontrol.business.serviceagreement;


import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_069;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_070;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.UpdateServiceAgreementHandler;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementTest {

    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private UpdateServiceAgreementHandler updateServiceAgreementHandler;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @InjectMocks
    private UpdateServiceAgreement updateServiceAgreement;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Test
    public void shouldSuccessfullyUpdateNameDescriptionAndExternalIdForServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withExternalId("ext-id")
            .withStatus(Status.DISABLED);
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockCheckIfServiceAgreementByExternalIdAlreadyExists(putBody, serviceAgreement, false);

        updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId);

        verify(updateServiceAgreementHandler).handleRequest(any(), eq(putBody));
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingState(eq(serviceAgreementId));
        verify(serviceAgreementBusinessRulesService).existsPendingServiceAgreementWithExternalId(eq("ext-id"));
    }

    @Test
    public void shouldReturnThatNameDescriptionAndExternalIdAreSuccessfullyUpdatedWhenNoNameDescriptionAndExternalIdAreProvided() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName(null)
            .withDescription(null)
            .withExternalId(null);
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockCheckIfServiceAgreementByExternalIdAlreadyExists(putBody, serviceAgreement, false);

        updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId);

        verify(updateServiceAgreementHandler).handleRequest(any(), eq(putBody));
    }

    @Test
    public void shouldReturnThatNameDescriptionAndExternalIdAreSuccessfullyUpdatedWhenNameDescriptionAndExternalIdAreTheSameAsPreviously() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        String name = "old name";
        String description = "old description";
        String externalId = "old-external-id";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName(name)
            .withDescription(description)
            .withExternalId(externalId);
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId)
            .withName(name)
            .withDescription(description)
            .withExternalId(externalId);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockCheckIfServiceAgreementByExternalIdAlreadyExists(putBody, serviceAgreement, false);

        updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId);

        verify(updateServiceAgreementHandler).handleRequest(any(), eq(putBody));
    }

    @Test
    public void shouldFailUpdateNameDescriptionAndExternalIdForServiceAgreementWhenServiceAgreementWithGivenExternalIdAlreadyExists() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withExternalId("ext-id");
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockCheckIfServiceAgreementByExternalIdAlreadyExists(putBody, serviceAgreement, true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_069.getErrorMessage(), ERR_AG_069.getErrorCode())));
    }

    @Test
    public void shouldFailUpdateNameDescriptionAndExternalIdForServiceAgreementWhenServiceAgreementWithGivenExternalIdAlreadyExistsInPending() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withExternalId("ext-id");
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        when(serviceAgreementBusinessRulesService.existsPendingServiceAgreementWithExternalId(anyString()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode())));
    }

    @Test
    public void shouldFailUpdateNameDescriptionAndExternalIdForServiceAgreementWhenServiceAgreementIsInPending() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withExternalId("ext-id");
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId);

        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        when(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(anyString()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode())));
    }

    @Test
    public void shouldFailUpdateServiceAgreementStatusWhenServiceAgreementRootMasterSA() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementPutRequestBody putBody = new ServiceAgreementPutRequestBody()
            .withStatus(Status.DISABLED);
        InternalRequest<ServiceAgreementPutRequestBody> request = getInternalRequest(putBody);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(creatorLegalEntityId)
            .withIsMaster(true);

        when(serviceAgreementBusinessRulesService.isPeriodValid(isNull(), isNull())).thenReturn(true);
        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockIsRootMasterServiceAgreement(serviceAgreement, true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateServiceAgreement.updateServiceAgreement(request, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_070.getErrorMessage(), ERR_AG_070.getErrorCode())));
    }

    private void mockIsRootMasterServiceAgreement(ServiceAgreementItem serviceAgreement, boolean isRoot) {
        when(serviceAgreementBusinessRulesService.isServiceAgreementRootMasterServiceAgreement(serviceAgreement))
            .thenReturn(isRoot);
    }

    private void mockCheckIfServiceAgreementByExternalIdAlreadyExists(ServiceAgreementPutRequestBody putBody,
        ServiceAgreementItem serviceAgreement, boolean exists) {
        when(serviceAgreementBusinessRulesService
            .serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNull(putBody, serviceAgreement))
            .thenReturn(exists);
    }

    private void mockGetServiceAgreement(String serviceAgreementId, ServiceAgreementItem serviceAgreement) {
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(serviceAgreement);
    }

}