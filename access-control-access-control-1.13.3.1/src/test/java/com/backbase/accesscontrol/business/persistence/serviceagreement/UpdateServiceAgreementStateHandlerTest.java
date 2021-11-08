package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_070;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementStateHandlerTest {

    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private UpdateServiceAgreementStateHandler updateServiceAgreementStateHandler;

    @Test
    public void testExecuteRequest() {
        String serviceAgreementId = "sa id";
        ServiceAgreementStatePutRequestBody requestData = new ServiceAgreementStatePutRequestBody()
            .withState(Status.ENABLED);
        updateServiceAgreementStateHandler.executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).updateServiceAgreementState(serviceAgreementId,
            ServiceAgreementState.ENABLED);

    }

    @Test
    public void testHandleRequest() {
        String serviceAgreementId = "sa id";
        ServiceAgreementStatePutRequestBody requestData = new ServiceAgreementStatePutRequestBody()
            .withState(Status.ENABLED);
        updateServiceAgreementStateHandler.handleRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).updateServiceAgreementState(serviceAgreementId,
            ServiceAgreementState.ENABLED);
        verify(eventBus, times(1)).emitEvent(any());
    }

    @Test
    public void testCreateSuccessEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        ServiceAgreementEvent successEvent = updateServiceAgreementStateHandler
            .createSuccessEvent(parameterHolder, null, null);

        assertNotNull(successEvent);
        Assert.assertEquals(Action.UPDATE, successEvent.getAction());
        Assert.assertEquals(serviceAgreementId, successEvent.getId());
    }

    @Test
    public void testCreateFailureEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Exception failure = new Exception("error msg");
        Event failureEvent = updateServiceAgreementStateHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }

    @Test
    public void shouldFailUpdateMSAWithStatusDisabled() {
        String serviceAgreementId = "SA-01";
        String creatorLegalEntityId = "LE-01";
        ServiceAgreementStatePutRequestBody putBody = new ServiceAgreementStatePutRequestBody()
            .withState(Status.DISABLED);
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withIsMaster(true)
            .withCreatorLegalEntity(creatorLegalEntityId);

        mockGetServiceAgreement(serviceAgreementId, serviceAgreement);
        mockIsRootMasterServiceAgreement(serviceAgreement, true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateServiceAgreementStateHandler
                .executeRequest(new SingleParameterHolder<>(serviceAgreementId), putBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_070.getErrorMessage(), ERR_AG_070.getErrorCode())));
    }

    private void mockIsRootMasterServiceAgreement(ServiceAgreementItem serviceAgreement, boolean isRoot) {
        when(serviceAgreementBusinessRulesService.isServiceAgreementRootMasterServiceAgreement(serviceAgreement))
            .thenReturn(isRoot);
    }

    private void mockGetServiceAgreement(String serviceAgreementId, ServiceAgreementItem serviceAgreement) {
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(serviceAgreement);
    }
}