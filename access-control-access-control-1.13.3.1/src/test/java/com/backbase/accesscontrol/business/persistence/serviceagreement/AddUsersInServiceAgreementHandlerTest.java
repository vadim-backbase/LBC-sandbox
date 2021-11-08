package com.backbase.accesscontrol.business.persistence.serviceagreement;


import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddUsersInServiceAgreementHandlerTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @InjectMocks
    private AddUsersInServiceAgreementHandler addUsersInServiceAgreementHandler;

    @Test
    public void testExecuteRequest() {
        String serviceAgreementId = "sa id";
        List<UsersDto> requestData = singletonList(new UsersDto());
        when(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId))
            .thenReturn(false);
        addUsersInServiceAgreementHandler.executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData);
        verify(persistenceServiceAgreementService).addUsersInServiceAgreement(eq(serviceAgreementId), eq(requestData));
    }

    @Test
    public void testExecuteRequestShouldThrowBadRequestWhenServiceAgreementInPending() {
        String serviceAgreementId = "sa id";
        List<UsersDto> requestData = singletonList(new UsersDto());
        when(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId))
            .thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addUsersInServiceAgreementHandler
                .executeRequest(new SingleParameterHolder<>(serviceAgreementId), requestData));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode()));
    }

    @Test
    public void testCreateSuccessEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Event successEvent = addUsersInServiceAgreementHandler
            .createSuccessEvent(parameterHolder, null, null);
        assertNull(successEvent);
    }

    @Test
    public void testCreateFailureEvent() {
        String serviceAgreementId = "sa id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(serviceAgreementId);
        Exception failure = new Exception("error msg");
        Event failureEvent = addUsersInServiceAgreementHandler
            .createFailureEvent(parameterHolder, null, failure);
        assertNull(failureEvent);
    }

}
