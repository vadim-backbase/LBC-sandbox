package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.routes.datagroup.AddDataGroupRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupsByIdentifiersRoute;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupsByIdentifiersRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.GetDataGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.ListDataGroupsRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.SearchDataGroupsRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.UpdateDataGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.UpdateDataGroupItemsByIdentifierRoute;
import com.backbase.accesscontrol.routes.datagroup.UpdateDataGroupItemsByIdentifierRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import java.util.List;
import java.util.Map;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service request/reply interface that is transport agnostic. Forwards on to relevant component
 * depending on service type.
 */
@Service
public class DataGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupService.class);

    @Autowired
    private InternalRequestContext internalRequestContext;

    @Produce(value = EndpointConstants.DIRECT_START_ADD_DATA_GROUP)
    private AddDataGroupRouteProxy addDataGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_DATA_GROUP_BY_ID)
    private GetDataGroupByIdRouteProxy getDataGroupByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_DATA_GROUPS)
    private ListDataGroupsRouteProxy listDataGroupsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_DATA_GROUP)
    private DeleteDataGroupRouteProxy deleteDataGroupRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_START_UPDATE_DATA_GROUP)
    private UpdateDataGroupByIdRouteProxy updateDataGroupByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_DATA_GROUPS_BY_IDENTIFIERS)
    private DeleteDataGroupsByIdentifiersRouteProxy deleteDataGroupsByIdentifiersRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_START_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER)
    private UpdateDataGroupItemsByIdentifierRouteProxy updateDataGroupItemsByIdentifierRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_SEARCH_DATA_GROUPS)
    private SearchDataGroupsRouteProxy searchDataGroupsRouteProxy;

    /**
     * Produces an Exchange to the DIRECT_START_ADD_DATA_GROUP endpoint.
     *
     * @param dataGroupBase - data group to be created.
     * @return {@link DataGroupsPostResponseBody}  for the created Data Group.
     */
    public DataGroupOperationResponse addDataGroup(DataGroupBase dataGroupBase) {
        LOGGER.info("Trying to add data group {}", dataGroupBase);
        return addDataGroupRouteProxy.addDataGroup(getInternalRequest(dataGroupBase, internalRequestContext))
            .getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_LIST_DATA_GROUPS endpoint.
     *
     * @param serviceAgreementId - service agreement id
     * @param type - data group type
     * @param includeItems - defines if should include data items in response.
     * @return a list of {@link DataGroupsGetResponseBody}
     */
    public List<DataGroupsGetResponseBody> getDataGroups(String serviceAgreementId, String type, boolean includeItems) {
        LOGGER.info("Trying to list data groups by service agreement {} and type {}", serviceAgreementId, type);
        return listDataGroupsRouteProxy
            .getDataGroups(getVoidInternalRequest(internalRequestContext), serviceAgreementId, type, includeItems)
            .getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_GET_DATA_GROUP_BY_ID endpoint.
     *
     * @param dataGroupId - id of the data group
     * @return {@link DataGroupByIdGetResponseBody} containing the data group.
     */
    public DataGroupByIdGetResponseBody getDataGroupById(String dataGroupId) {
        LOGGER.info("Trying to get data group by id {}", dataGroupId);
        return getDataGroupByIdRouteProxy
            .getDataGroupById(getVoidInternalRequest(internalRequestContext), dataGroupId).getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_DELETE_DATA_GROUP endpoint.
     *
     * @param id - id of the data group to be deleted
     * @return data of type {@link DataGroupOperationResponse}
     */
    public DataGroupOperationResponse deleteDataGroup(String id) {
        LOGGER.info("Trying to delete data group with id {}", id);
        return deleteDataGroupRouteProxy.deleteDataGroup(getVoidInternalRequest(internalRequestContext), id)
            .getData();
    }

    /**
     * Produces an Exchange to the DIRECT_START_UPDATE_DATA_GROUP endpoint.
     *
     * @param dataGroupByIdPutRequestBody - the data to be updated.
     * @param id - Id
     * @return data of type {@link DataGroupOperationResponse}
     */
    public DataGroupOperationResponse updateDataGroup(DataGroupByIdPutRequestBody dataGroupByIdPutRequestBody,
        String id) {
        LOGGER.info("Trying to update data group by id {}", id);
        return updateDataGroupByIdRouteProxy
            .updateDataGroupById(getInternalRequest(dataGroupByIdPutRequestBody, internalRequestContext), id)
            .getData();
    }

    /**
     * Forwards the request to {@link UpdateDataGroupItemsByIdentifierRoute}.
     *
     * @param dataGroupItemPutRequestBodies the data to be updated
     * @param responseContainer list of {@link BatchResponseItemExtended}
     * @param validResponses map of valid responses
     * @param internalDataItemsIdsByTypeAndExternalId map between internal and external ids
     * @return a list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> updateDataGroupItemsBatchByIdentifier(
        List<PresentationDataGroupItemPutRequestBody> dataGroupItemPutRequestBodies,
        List<BatchResponseItemExtended> responseContainer,
        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses,
        Map<String, Map<String, String>>
            internalDataItemsIdsByTypeAndExternalId) {
        LOGGER.info("Trying to update batch data group items");
        return updateDataGroupItemsByIdentifierRouteProxy.updateDataGroupItemsByIdentifier(
            getInternalRequest(dataGroupItemPutRequestBodies, internalRequestContext), responseContainer,
            validResponses, internalDataItemsIdsByTypeAndExternalId).getData();
    }

    /**
     * Forwards the request to {@link DeleteDataGroupsByIdentifiersRoute}.
     *
     * @param presentationIdentifierList the data to be deleted
     * @return a list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> deleteDataGroupsByIdentifiers(
        List<PresentationIdentifier> presentationIdentifierList) {
        LOGGER.info("Trying to delete batch data groups");
        return deleteDataGroupsByIdentifiersRouteProxy
            .deleteDataGroupsByIdentifiers(getInternalRequest(presentationIdentifierList, internalRequestContext))
            .getData();
    }

    /**
     * Mathod that redirect to business route for searching data groups.
     *
     * @param presentationGetDataGroupsRequest request filter.
     * @param type                             - type of requested data groups.
     * @return List of data groups grouped by service agreement.
     */
    public List<PresentationServiceAgreementWithDataGroups> searchDataGroups(
        PresentationGetDataGroupsRequest presentationGetDataGroupsRequest, String type) {
        LOGGER.info("Trying to get data groups by search criteria {}", presentationGetDataGroupsRequest);
        return searchDataGroupsRouteProxy
            .searchDataGroups(getInternalRequest(presentationGetDataGroupsRequest, internalRequestContext), type)
            .getData();
    }
}
