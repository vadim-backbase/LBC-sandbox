package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.functiongroup.IngestFunctionGroupHandler;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestFunctionGroupTest {

    @Mock
    private IngestFunctionGroupHandler ingestFunctionGroupHandler;
    @Mock
    private DateTimeService dateTimeService;
    @Spy
    private FunctionGroupMapper functionGroupMapper = Mappers.getMapper(FunctionGroupMapper.class);

    @InjectMocks
    private IngestFunctionGroup ingestFunctionGroup;

    @Test
    public void shouldIngestFunctionGroup() {
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";

        PresentationIngestFunctionGroupPostResponseBody data = new PresentationIngestFunctionGroupPostResponseBody()
            .withId(createdFagId);

        when(ingestFunctionGroupHandler.handleRequest(any(), any(FunctionGroupIngest.class)))
            .thenReturn(data);

        FunctionGroupIngest persistenceIngestFunctionGroup = new FunctionGroupIngest()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId);
        when(functionGroupMapper
            .presentationFunctionGroupBaseToFunctionGroupIngest(any(PresentationFunctionGroup.class)))
            .thenReturn(persistenceIngestFunctionGroup);

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId);
        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);

        InternalRequest<PresentationIngestFunctionGroupPostResponseBody> responseResult = ingestFunctionGroup
            .ingestFunctionGroup(postRequest);
        verify(ingestFunctionGroupHandler, times(1))
            .handleRequest(any(), eq(persistenceIngestFunctionGroup));
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenFromDateNullAndFromTimeNotNull() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";

        String fromDate = null;
        String fromTime = "07:48:23";
        String untilDate = null;
        String untilTime = null;
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate)
            .withValidFromTime(fromTime)
            .withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroup.ingestFunctionGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(ingestFunctionGroupHandler, times(0))
            .handleRequest(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenUntilDateNullAndUntilTimeNotNull() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";

        String fromDate = null;
        String fromTime = null;
        String untilDate = null;
        String untilTime = "07:48:23";
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate)
            .withValidFromTime(fromTime)
            .withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroup.ingestFunctionGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(ingestFunctionGroupHandler, times(0))
            .handleRequest(any(), any());
    }


    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidFormatFromDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";

        String fromDate = "2017-21-43";
        String fromTime = "07:48:23";
        String untilDate = null;
        String untilTime = null;
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate)
            .withValidFromTime(fromTime)
            .withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroup.ingestFunctionGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(ingestFunctionGroupHandler, times(0))
            .handleRequest(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidFormatUntilDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";

        String fromDate = null;
        String fromTime = null;
        String untilDate = "2017-14-44";
        String untilTime = "07:48:23";
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate)
            .withValidFromTime(fromTime)
            .withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroup.ingestFunctionGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(ingestFunctionGroupHandler, times(0))
            .handleRequest(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidPeriodOfDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";

        String fromDate = "2018-03-31";
        String fromTime = "07:48:23";
        String untilDate = "2017-01-31";
        String untilTime = "07:48:23";

        doThrow(getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        PresentationFunctionGroup postData = new PresentationFunctionGroup()
            .withName(name)
            .withExternalServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate)
            .withValidFromTime(fromTime)
            .withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<PresentationFunctionGroup> postRequest = getInternalRequest(postData);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroup.ingestFunctionGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));

        verify(ingestFunctionGroupHandler, times(0))
            .handleRequest(any(), any());
    }
}