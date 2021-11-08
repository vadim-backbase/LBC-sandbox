package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.DataFunctionGroupUtil.mergeResponses;
import static com.backbase.accesscontrol.util.DataFunctionGroupUtil.separateValidFromInvalidRequests;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_081;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupsByIdentifiersRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteDataGroupsByIdentifiers implements DeleteDataGroupsByIdentifiersRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataGroupsByIdentifiers.class);

    private DataGroupPAndPService dataGroupPAndPService;

    /**
     * Method that listens on the direct:deleteDataGroupsByIdentifiersRequestedInternal endpoint and uses the DataGroup
     * client to forward the delete request to the p&p service.
     *
     * @param request Internal Request of list with {@link PresentationIdentifier} to be send to the client
     * @return InternalRequest containing list of {@link BatchResponseItemExtended}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUPS_BY_IDENTIFIERS)
    public InternalRequest<List<BatchResponseItemExtended>> deleteDataGroupsByIdentifiers(
        @Body InternalRequest<List<PresentationIdentifier>> request) {

        LOGGER.info("Trying to delete data groups by Identifier");
        return getInternalRequest(processDataGroupRequest(request.getData()), request.getInternalRequestContext());
    }

    private List<BatchResponseItemExtended> processDataGroupRequest(
        List<PresentationIdentifier> presentationIdentifiers) {
        List<Boolean> indexValidaCorrelation = new ArrayList<>();

        List<PresentationIdentifier> invalidRequestPayloads = new ArrayList<>();
        List<PresentationIdentifier> validRequest = new ArrayList<>();

        separateValidFromInvalidRequests(presentationIdentifiers, indexValidaCorrelation, invalidRequestPayloads,
            validRequest);

        List<BatchResponseItemExtended> response = dataGroupPAndPService.deleteDataGroupsByIdentifiers(validRequest);

        List<BatchResponseItemExtended> responseInvalidIdentifier = createResponseForInvalidIdentifiers(
            invalidRequestPayloads);
        return mergeResponses(indexValidaCorrelation, response, responseInvalidIdentifier);
    }

    private List<BatchResponseItemExtended> createResponseForInvalidIdentifiers(
        List<PresentationIdentifier> identifiers) {
        return identifiers.stream()
            .map(this::getBatchResponseItemExtended)
            .collect(Collectors.toList());
    }

    private BatchResponseItemExtended getBatchResponseItemExtended(PresentationIdentifier identifier) {
        return new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withResourceId(identifier.getIdIdentifier())
            .withErrors(Collections.singletonList(ERR_AG_081.getErrorMessage()));
    }
}
