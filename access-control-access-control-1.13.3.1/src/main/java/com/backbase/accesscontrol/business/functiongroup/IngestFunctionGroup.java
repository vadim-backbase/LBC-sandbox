package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.persistence.functiongroup.IngestFunctionGroupHandler;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for adding new Function Group. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class IngestFunctionGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestFunctionGroup.class);

    private DateTimeService dateTimeService;
    private IngestFunctionGroupHandler ingestFunctionGroupHandler;
    private FunctionGroupMapper functionGroupMapper;


    /**
     * Method that listens on the direct:addFunctionGroupRequestedInternal endpoint
     *
     * @param request Internal Request of {@link PresentationFunctionGroup} type to be send by the client
     * @return InternalRequest of {@link PresentationIngestFunctionGroupPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_INGEST_FUNCTION_GROUP)
    public InternalRequest<PresentationIngestFunctionGroupPostResponseBody> ingestFunctionGroup(
        InternalRequest<PresentationFunctionGroup> request) {
        LOGGER.info("Trying to add function group");

        return getInternalRequest(getResult(request.getData()), request.getInternalRequestContext());
    }

    private PresentationIngestFunctionGroupPostResponseBody getResult(
        PresentationFunctionGroup request) {

        dateTimeService.validateTimebound(request.getValidFromDate(), request.getValidFromTime(),
            request.getValidUntilDate(), request.getValidUntilTime());


        FunctionGroupIngest functionGroupBaseDto =
            functionGroupMapper.presentationFunctionGroupBaseToFunctionGroupIngest(request);

        return ingestFunctionGroupHandler.handleRequest(new EmptyParameterHolder(), functionGroupBaseDto);
    }
}
