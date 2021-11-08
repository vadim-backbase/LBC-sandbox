package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.routes.functiongroup.AddFunctionGroupRoute;
import com.backbase.accesscontrol.routes.functiongroup.AddFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.DeleteFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.DeleteFunctionGroupRoute;
import com.backbase.accesscontrol.routes.functiongroup.DeleteFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.GetFunctionGroupByIdRoute;
import com.backbase.accesscontrol.routes.functiongroup.GetFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.IngestFunctionGroupRoute;
import com.backbase.accesscontrol.routes.functiongroup.IngestFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.ListFunctionGroupsRoute;
import com.backbase.accesscontrol.routes.functiongroup.ListFunctionGroupsRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.UpdateFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.UpdateFunctionGroupRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import java.util.List;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service for FunctionGroups Forwards the request on to relevant camel route using route proxies.
 */

@Service
public class FunctionGroupsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupsService.class);

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ADD_FUNCTION_GROUP)
    private AddFunctionGroupRouteProxy addFunctionGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_FUNCTION_GROUPS)
    private ListFunctionGroupsRouteProxy listFunctionGroupsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_FUNCTION_GROUP_BY_ID)
    private DeleteFunctionGroupByIdRouteProxy deleteFunctionGroupByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_FUNCTION_GROUP_BY_ID)
    private GetFunctionGroupByIdRouteProxy getFunctionGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP_BY_ID)
    private UpdateFunctionGroupByIdRouteProxy updateFunctionGroupByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP)
    private UpdateFunctionGroupRouteProxy updateFunctionGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_FUNCTION_GROUP)
    private DeleteFunctionGroupRouteProxy deleteFunctionGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_INGEST_FUNCTION_GROUP)
    private IngestFunctionGroupRouteProxy ingestFunctionGroupRouteProxy;

    @Autowired
    private InternalRequestContext context;

    /**
     * Forwards the request to {@link AddFunctionGroupRoute}.
     *
     * @param groupBase function group
     * @return FunctionGroupsPostResponseBody
     */
    public FunctionGroupsPostResponseBody addFunctionGroup(
        FunctionGroupBase groupBase) {
        LOGGER.info("Trying to add new function group {}", groupBase);
        return addFunctionGroupRouteProxy.addFunctionGroup(getInternalRequest(groupBase, context)).getData();
    }

    /**
     * Forwards the request to {@link ListFunctionGroupsRoute}.
     *
     * @param serviceAgreementId the service agreement id
     * @return a list of functional groups
     */
    public List<FunctionGroupsGetResponseBody> getAllFunctionGroup(
        String serviceAgreementId) {
        LOGGER.info("Trying to get all function groups for service agreement with id {}", serviceAgreementId);
        return listFunctionGroupsRouteProxy.getFunctionGroups(getVoidInternalRequest(context), serviceAgreementId)
            .getData();
    }

    /**
     * Forwards the request to {@link DeleteFunctionGroupByIdRouteProxy}.
     *
     * @param id ID of the Functional group that is deleted.
     */
    public void deleteFunctionGroup(String id) {
        LOGGER.info("Trying to delete function group with id {}", id);
        deleteFunctionGroupByIdRouteProxy.deleteFunctionGroup(getVoidInternalRequest(context), id);
    }

    /**
     * Forwards the request to {@link DeleteFunctionGroupRoute}.
     *
     * @param presentationIdentifiers list of {@link PresentationIdentifier}
     * @return list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> deleteFunctionGroup(List<PresentationIdentifier> presentationIdentifiers) {
        LOGGER.info("Trying to delete batch function groups");

        return deleteFunctionGroupRouteProxy
            .deleteFunctionGroup(getInternalRequest(presentationIdentifiers, context)).getData();
    }

    /**
     * Forwards the request to {@link GetFunctionGroupByIdRoute}.
     *
     * @param id the id of the function group
     * @return an Internal request of type FunctionGroupByIdGetResponseBody
     */
    public FunctionGroupByIdGetResponseBody getFunctionGroupById(String id) {
        LOGGER.info("Trying to get function group with id {}", id);
        return getFunctionGroupRouteProxy.getFunctionGroupById(getVoidInternalRequest(context), id).getData();
    }

    /**
     * Update function group.
     *
     * @param id - id of function group
     * @param body - function group data
     */
    public void updateFunctionGroup(String id, FunctionGroupByIdPutRequestBody body) {
        LOGGER.info("Trying to update function group with id {}", id);
        updateFunctionGroupByIdRouteProxy.updateFunctionGroupById(getInternalRequest(body, context), id);
    }

    /**
     * Update of function group.
     *
     * @param request list of {@link PresentationFunctionGroupPutRequestBody}
     * @return list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> updateFunctionGroupsBatch(
        List<PresentationFunctionGroupPutRequestBody> request) {
        LOGGER.info("Trying to update batch function groups");

        return updateFunctionGroupRouteProxy.updateFunctionGroup(getInternalRequest(request, context)).getData();
    }

    /**
     * Forwards the request to {@link IngestFunctionGroupRoute}.
     *
     * @param presentationFunctionGroup request
     * @return PresentationIngestFunctionGroupPostResponseBody
     */
    public PresentationIngestFunctionGroupPostResponseBody ingestFunctionGroup(
        PresentationFunctionGroup presentationFunctionGroup) {
        LOGGER.info("Trying to add new function group {}", presentationFunctionGroup);
        return ingestFunctionGroupRouteProxy
            .ingestFunctionGroup(getInternalRequest(presentationFunctionGroup, context)).getData();
    }
}
