package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementAdminsHandlerTest {

    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private ServiceAgreementAdminService serviceAgreementAdminService;

    @InjectMocks
    private UpdateServiceAgreementAdminsHandler updateServiceAgreementAdminsHandler;

    @Test
    public void testExecuteRequest() {
        AdminsPutRequestBody requestData = new AdminsPutRequestBody();
        String serviceAgreementId = "sa id";
        when(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId))
            .thenReturn(false);
        updateServiceAgreementAdminsHandler
            .executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(serviceAgreementAdminService).updateAdmins(serviceAgreementId, requestData);
    }

    @Test
    public void testExecuteRequestShouldThrowBadRequestWhenServiceAgreementInPending() {
        AdminsPutRequestBody requestData = new AdminsPutRequestBody();
        String serviceAgreementId = "sa id";
        when(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId))
            .thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> updateServiceAgreementAdminsHandler
                .executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode()));
    }

    @Test
    public void testCreateSuccessEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Event successEvent = updateServiceAgreementAdminsHandler
            .createSuccessEvent(parameterHolder, null, null);
        assertNull(successEvent);
    }

    @Test
    public void testCreateFailureEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Exception failure = new Exception("error msg");
        Event failureEvent = updateServiceAgreementAdminsHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }
}