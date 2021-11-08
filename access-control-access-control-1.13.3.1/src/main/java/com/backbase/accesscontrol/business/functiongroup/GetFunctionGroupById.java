package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Get function group business consumer. This class is the business process component of the access-group presentation
 * service, communicating with the p&p service and retrieving a single function group.
 */
@Service
@AllArgsConstructor
public class GetFunctionGroupById {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetFunctionGroupById.class);

    private FunctionGroupPAndPService functionGroupPAndPService;
    private FunctionGroupMapper functionGroupMapper;

    /**
     * Send a request to the persistence layer using the query pandp service.
     *
     * @param request the internal request
     * @param id      the function group id
     * @return a internal request with a body of type {@link FunctionGroupByIdGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_FUNCTION_GROUP_BY_ID)
    public InternalRequest<FunctionGroupByIdGetResponseBody> getFunctionGroupById(@Body InternalRequest<Void> request,
        @Header("id") String id) {
        LOGGER.info("Trying to fetch function group by given ID {}", id);

        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups
            .functiongroups.FunctionGroupByIdGetResponseBody response = functionGroupPAndPService
            .getFunctionGroupById(id);

        FunctionGroupByIdGetResponseBody functionBody = functionGroupMapper
            .persistenceFunctionGroupByIdToPresentationFunctionGroupById(response);
        return getInternalRequest(functionBody, request.getInternalRequestContext());
    }

}
