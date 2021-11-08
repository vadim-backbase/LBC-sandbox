package com.backbase.accesscontrol.business.persistence.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.IngestFunctionGroupTransformService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestFunctionGroupHandlerTest {

    @InjectMocks
    private IngestFunctionGroupHandler ingestFunctionGroupHandler;
    @Mock
    private IngestFunctionGroupTransformService ingestFunctionGroupTransformService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    @Test
    public void shouldReturnIdOfTheCreatedFunctionGroup() {
        FunctionGroupIngest requestData = new FunctionGroupIngest();
        String id = "fg-id";
        when(ingestFunctionGroupTransformService.addFunctionGroup(eq(requestData)))
            .thenReturn(id);

        PresentationIngestFunctionGroupPostResponseBody functionGroupResponse
            = ingestFunctionGroupHandler.executeRequest(new EmptyParameterHolder(), requestData);
        assertThat(functionGroupResponse,
            allOf(
                is(notNullValue()),
                hasProperty("id", is(id))
            )
        );
    }

    @Test
    public void shouldThrowBadRequestWhenReturnIdOfTheCreatedFunctionGroup() {
        FunctionGroupIngest requestData = new FunctionGroupIngest();
        requestData.setExternalServiceAgreementId("exSa");
        String id = "fg-id";
        when(serviceAgreementBusinessRulesService
            .isServiceAgreementInPendingStateByExternalId(requestData.getExternalServiceAgreementId()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroupHandler.executeRequest(new EmptyParameterHolder(), requestData));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode())));
    }


    @Test
    public void testCreateSuccessEvent() {

        FunctionGroupEvent successEvent = ingestFunctionGroupHandler
            .createSuccessEvent(new EmptyParameterHolder(), new FunctionGroupIngest(),
                new PresentationIngestFunctionGroupPostResponseBody().withId("id"));

        assertNotNull(successEvent);
        assertEquals(Action.ADD, successEvent.getAction());
        assertEquals("id", successEvent.getId());

    }

    @Test
    public void testCreateFailureEvent() {
        String errorMessage = "message";

        Event failureEvent = ingestFunctionGroupHandler
            .createFailureEvent(new EmptyParameterHolder(), new FunctionGroupIngest(),
                new Exception(errorMessage));

        assertNull(failureEvent);
    }

}