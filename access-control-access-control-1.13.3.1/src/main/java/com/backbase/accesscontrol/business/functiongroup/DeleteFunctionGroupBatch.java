package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.DataFunctionGroupUtil.separateValidFromInvalidRequests;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_083;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for deleting batch Function Groups. This class is a business process component of the access group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class DeleteFunctionGroupBatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFunctionGroupBatch.class);

    private FunctionGroupPAndPService functionGroupPAndPService;

    /**
     * Method that listens on the direct:direct:deleteFunctionGroupRequestedInternal endpoint
     *
     * @param request Internal Request of {@link PresentationIdentifier} type to be send by the client
     * @return InternalRequest List of batchResponseItemExtended
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP)
    public InternalRequest<List<BatchResponseItemExtended>> deleteFunctionGroup(
        @Body InternalRequest<List<PresentationIdentifier>> request) {

        LOGGER.info("Trying to delete function groups");

        return getInternalRequest(getBusinessProcessResult(request.getData()), request.getInternalRequestContext());
    }

    private List<BatchResponseItemExtended> getBusinessProcessResult(
        List<PresentationIdentifier> presentationIdentifiers) {

        List<PresentationIdentifier> badRequest = new ArrayList<>();
        List<PresentationIdentifier> validRequest = new ArrayList<>();
        List<Boolean> mapOrderOfValidRequest = new ArrayList<>();

        separateValidFromInvalidRequests(presentationIdentifiers, mapOrderOfValidRequest, badRequest, validRequest);
        List<BatchResponseItemExtended> invalidIdentifier = createResponseForInvalidIdentifiers(badRequest);

        List<BatchResponseItemExtended> response = functionGroupPAndPService.deleteFunctionGroup(validRequest);

        response = mergeResponses(mapOrderOfValidRequest, response, invalidIdentifier);
        return response;
    }


    private List<BatchResponseItemExtended> createResponseForInvalidIdentifiers(
        List<PresentationIdentifier> functionGroupsDeletePostRequestBodies) {
        return functionGroupsDeletePostRequestBodies.stream()
            .map(this::buildBatchResponseItemExtended)
            .collect(Collectors.toList());
    }

    private BatchResponseItemExtended buildBatchResponseItemExtended(PresentationIdentifier functionGroupRequest) {
        return new BatchResponseItemExtended()
            .withResourceId(functionGroupRequest.getIdIdentifier())
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList(ERR_AG_083.getErrorMessage()));
    }

    private List<BatchResponseItemExtended> mergeResponses(List<Boolean> mapOrderOfRequests,
        List<BatchResponseItemExtended> validData, List<BatchResponseItemExtended> invalidData) {
        Iterator<BatchResponseItemExtended> validDataIterator = validData.iterator();
        Iterator<BatchResponseItemExtended> invalidDataIterator = invalidData.iterator();

        return mapOrderOfRequests.stream()
            .map(isValid -> isValid ? validDataIterator.next() : invalidDataIterator.next())
            .collect(Collectors.toList());
    }
}
