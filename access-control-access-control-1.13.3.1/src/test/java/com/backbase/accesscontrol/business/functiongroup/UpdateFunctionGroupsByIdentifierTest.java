package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_083;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupDataProvider.createPresentationFunctionGroupPutRequestBody;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.batch.functiongroup.UpdateBatchFunctionGroupByIdentifier;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupsByIdentifierTest {

    @Mock
    private UpdateBatchFunctionGroupByIdentifier updateBatchFunctionGroupByIdentifier;
    @Spy
    private BatchResponseItemExtendedMapper batchResponseItemMapper = Mappers
        .getMapper(BatchResponseItemExtendedMapper.class);
    @Mock
    private InternalRequestContext internalRequestContext;
    @Mock
    private DateTimeService dateTimeService;

    @InjectMocks
    private UpdateFunctionGroupByIdentifier updateFunctionGroupByIdentifier;

    @Test
    public void shouldUpdateFunctionGroupBatch() {
        List<PresentationFunctionGroupPutRequestBody> putData = asList(
            createPresentationFunctionGroupPutRequestBody("FG-01", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), null, null, null, null),
            createPresentationFunctionGroupPutRequestBody(null, null, "idSA2", "name2", "description2", "functionName2",
                asList("privileges21", "privileges22"), null, null, null, null),
            createPresentationFunctionGroupPutRequestBody("FG-03", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), null, "01:05:00", null, null),
            createPresentationFunctionGroupPutRequestBody("FG-04", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), null, null, null, "01:06:00"),
            createPresentationFunctionGroupPutRequestBody("FG-05", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), "2019-54-14", null, null, null),
            createPresentationFunctionGroupPutRequestBody("FG-06", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), null, null, "2019-15-22", null),
            createPresentationFunctionGroupPutRequestBody("FG-07", null, null, "name1", "description1", "functionName1",
                asList("privileges11", "privileges12"), "2019-03-01", "01:15:00", "2019-02-01", "01:15:00"),
            createPresentationFunctionGroupPutRequestBody(null, "nameId", "extSaid", "name1", "description1",
                "functionName1", asList("privileges11", "privileges12"), null, "01:46:00", null, "01:46:00"));

        ResponseItemExtended successfulResponseItemExtended = new ResponseItemExtended();
        successfulResponseItemExtended.setStatus(ItemStatusCode.HTTP_STATUS_OK);
        successfulResponseItemExtended.setResourceId("FG-01");
        BatchResponseItemExtended successfulResponseItem = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("FG-01");
        BatchResponseItemExtended errorInvalidIdentifier = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_083.getErrorMessage()));
        BatchResponseItemExtended errorInvalidFromDateTime = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_094.getErrorMessage()))
            .withResourceId("FG-03");
        BatchResponseItemExtended errorInvalidUntilDateTime = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_094.getErrorMessage()))
            .withResourceId("FG-04");
        BatchResponseItemExtended errorInvalidFormatFromDateTime = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_094.getErrorMessage()))
            .withResourceId("FG-05");
        BatchResponseItemExtended errorInvalidFormatUntilDateTime = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_094.getErrorMessage()))
            .withResourceId("FG-06");
        BatchResponseItemExtended errorInvalidPeriodDateTime = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_095.getErrorMessage()))
            .withResourceId("FG-07");
        BatchResponseItemExtended errorInvalidFormatDateTimeNameExtSaid = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERR_AG_094.getErrorMessage()))
            .withResourceId("nameId").withExternalServiceAgreementId("extSaid");

        List<ResponseItemExtended> mockSuccessfulResponseData = singletonList(successfulResponseItemExtended);
        List<BatchResponseItemExtended> mockInvalidResponseData = asList(errorInvalidIdentifier,
            errorInvalidFromDateTime, errorInvalidUntilDateTime, errorInvalidFormatFromDateTime,
            errorInvalidFormatUntilDateTime, errorInvalidPeriodDateTime, errorInvalidFormatDateTimeNameExtSaid);

        when(updateBatchFunctionGroupByIdentifier.processBatchItems(singletonList(putData.get(0))))
            .thenReturn(mockSuccessfulResponseData);

        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(null), eq("01:05:00"), eq(null), eq(null));
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(null), eq(null), eq(null), eq("01:06:00"));
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq("2019-54-14"), eq(null), eq(null), eq(null));
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(null), eq(null), eq("2019-15-22"), eq(null));
        doThrow(getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq("2019-03-01"), eq("01:15:00"), eq("2019-02-01"), eq("01:15:00"));
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(null), eq("01:46:00"), eq(null), eq("01:46:00"));

        InternalRequest<List<BatchResponseItemExtended>> response = updateFunctionGroupByIdentifier
            .updateFunctionGroup(getInternalRequest(putData, internalRequestContext));

        assertEquals(mockSuccessfulResponseData.size() + mockInvalidResponseData.size(), response.getData().size());
        assertTrue(containsSuccessfulResponseItem(response, successfulResponseItem));
        assertTrue(containsFailedResponseItem(response, errorInvalidIdentifier));
        assertTrue(containsFailedResponseItem(response, errorInvalidFromDateTime));
        assertTrue(containsFailedResponseItem(response, errorInvalidUntilDateTime));
        assertTrue(containsFailedResponseItem(response, errorInvalidFormatFromDateTime));
        assertTrue(containsFailedResponseItem(response, errorInvalidFormatUntilDateTime));
        assertTrue(containsFailedResponseItem(response, errorInvalidPeriodDateTime));
        assertTrue(containsFailedResponseItem(response, errorInvalidFormatDateTimeNameExtSaid));
    }
}
