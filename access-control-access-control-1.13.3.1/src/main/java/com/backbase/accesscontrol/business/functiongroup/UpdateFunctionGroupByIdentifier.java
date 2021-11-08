package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.DataFunctionGroupUtil.isIdentifierValid;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_083;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.dto.ObjectPair;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.batch.functiongroup.UpdateBatchFunctionGroupByIdentifier;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for updating an existing Function Group. This class is a business process component of the access
 * group presentation service, communicating with the persistence services.
 */
@Service
@AllArgsConstructor
public class UpdateFunctionGroupByIdentifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFunctionGroupByIdentifier.class);

    private DateTimeService dateTimeService;
    private UpdateBatchFunctionGroupByIdentifier updateBatchFunctionGroupByIdentifier;
    private BatchResponseItemExtendedMapper batchResponseItemMapper;

    /**
     * Method that listens on the direct:direct:updateFunctionGroupBatchRequestedInternal endpoint and  forwards the
     * request to the persistence service.
     *
     * @param request Internal Request of list {@link PresentationFunctionGroupPutRequestBody} type to be send by the
     *                client
     * @return InternalRequest List of {@link BatchResponseItemExtended}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP)
    public InternalRequest<List<BatchResponseItemExtended>> updateFunctionGroup(
        @Body InternalRequest<List<PresentationFunctionGroupPutRequestBody>> request) {

        LOGGER.info("Trying to update function groups batch by Identifier");

         return getInternalRequest(processFunctionGroupRequest(request.getData()), request.getInternalRequestContext());
    }

    private List<BatchResponseItemExtended> processFunctionGroupRequest(
        List<PresentationFunctionGroupPutRequestBody> functionGroupPutRequestBodyList) {
        List<ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended>> list =
            functionGroupPutRequestBodyList
            .stream()
            .map(item -> new ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended>(item, null))
            .collect(Collectors.toList());

        List<ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended>> validItems =
            new ArrayList<>();
        validateItems(list, validItems);

        List<PresentationFunctionGroupPutRequestBody> functionGroupPutRequestBodies = validItems.stream()
            .map(ObjectPair::getRequest).collect(Collectors.toList());

        List<BatchResponseItemExtended> response = batchResponseItemMapper
            .mapList(updateBatchFunctionGroupByIdentifier.processBatchItems(functionGroupPutRequestBodies));

        validItems.forEach(item -> item.setResponse(response.get(validItems.indexOf(item))));

        return list.stream().map(ObjectPair::getResponse)
            .collect(Collectors.toList());
    }

    private void validateItems(
        List<ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended>> list,
        List<ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended>> validItems) {

        for (ObjectPair<PresentationFunctionGroupPutRequestBody, BatchResponseItemExtended> functionGroupPair : list) {
            try {
                if (isIdentifierValid(functionGroupPair.getRequest().getIdentifier())) {
                    dateTimeService
                        .validateTimebound(functionGroupPair.getRequest().getFunctionGroup().getValidFromDate(),
                            functionGroupPair.getRequest().getFunctionGroup().getValidFromTime(),
                            functionGroupPair.getRequest().getFunctionGroup().getValidUntilDate(),
                            functionGroupPair.getRequest().getFunctionGroup().getValidUntilTime());
                    validItems.add(functionGroupPair);
                } else {
                    functionGroupPair.setResponse(
                        getBatchResponseItemExtended(functionGroupPair.getRequest(), ERR_AG_083.getErrorMessage()));
                }
            } catch (BadRequestException ex) {
                functionGroupPair.setResponse(
                    getBatchResponseItemExtended(functionGroupPair.getRequest(), ex.getErrors().get(0).getMessage()));
            }
        }
    }

    private BatchResponseItemExtended getBatchResponseItemExtended(
        PresentationFunctionGroupPutRequestBody functionGroupRequest, String errorMessage) {
        BatchResponseItemExtended batchResponseItemExtended = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Arrays.asList(errorMessage));

        if (nonNull(functionGroupRequest.getIdentifier().getIdIdentifier())) {
            batchResponseItemExtended.withResourceId(functionGroupRequest.getIdentifier().getIdIdentifier());
        } else if (nonNull(functionGroupRequest.getIdentifier().getNameIdentifier())) {
            batchResponseItemExtended
                .withResourceId(functionGroupRequest.getIdentifier().getNameIdentifier().getName());
            batchResponseItemExtended.withExternalServiceAgreementId(
                functionGroupRequest.getIdentifier().getNameIdentifier().getExternalServiceAgreementId());
        }

        return batchResponseItemExtended;
    }
}
