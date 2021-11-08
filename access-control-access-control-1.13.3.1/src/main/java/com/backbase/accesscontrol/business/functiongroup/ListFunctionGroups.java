package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a List of Function Groups. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving all function access groups for a
 * given service agreement id.
 */
@Service
@AllArgsConstructor
public class ListFunctionGroups {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListFunctionGroups.class);

    private FunctionGroupMapper functionGroupMapper;
    private FunctionGroupPAndPService functionGroupPAndPService;

    /**
     * Retrieves all Function Groups.
     *
     * @param internalRequest    the internal request
     * @param serviceAgreementId the id of the service Agreement for which to retrieve the functional groups
     * @return BusinessProcessResult of list {@link FunctionGroupsGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_FUNCTION_GROUPS)
    public InternalRequest<List<FunctionGroupsGetResponseBody>> getAllFunctionGroups(
        InternalRequest<Void> internalRequest, @Header
        ("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to list function groups");

        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupsGetResponseBody> responseBodies = functionGroupPAndPService
            .getFunctionGroups(serviceAgreementId);

        List<FunctionGroupsGetResponseBody> response =
            functionGroupMapper.pandpFunctionGroupsToPresentationFunctionGroups(responseBodies);

        return getInternalRequest(response, internalRequest.getInternalRequestContext());
    }


}
