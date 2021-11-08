package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.business.persistence.serviceagreement.UpdateServiceAgreementStateHandler;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.routes.serviceagreement.AddServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.AddUsersInServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.DeleteBatchServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.EditServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetAssignedUserPermissionsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementByExternalIdRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementParticipantsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementRoute;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetUnexposedUsersRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.IngestServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListAdminsForServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListServiceAgreementsHierarchyRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListServiceAgreementsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListUsersForServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.RemoveUsersFromServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateAssignUsersPermissionsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateBatchAdminsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateParticipantsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateServiceAgreementAdminsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateUsersInServiceAgreementRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import java.util.List;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service for Service Agreement Forwards the request on to relevant camel route using route proxies.
 */
@Service
public class ServiceAgreementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementService.class);

    @Autowired
    private InternalRequestContext internalRequestContext;

    @Autowired
    private UpdateServiceAgreementStateHandler updateServiceAgreementStateHandler;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_SERVICE_AGREEMENTS)
    private ListServiceAgreementsRouteProxy listServiceAgreementsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ADD_SERVICE_AGREEMENT)
    private AddServiceAgreementRouteProxy addServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_ADMINS)
    private UpdateServiceAgreementAdminsRouteProxy updateServiceAgreementAdminsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_ID)
    private GetServiceAgreementRouteProxy getServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_USERS_FOR_SERVICE_AGREEMENT)
    private ListUsersForServiceAgreementRouteProxy listUsersForServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ADD_USERS_IN_SERVICE_AGREEMENT)
    private AddUsersInServiceAgreementRouteProxy addUsersInServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_REMOVE_USERS_FROM_SERVICE_AGREEMENT)
    private RemoveUsersFromServiceAgreementRouteProxy removeUsersFromServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_INGEST_SERVICE_AGREEMENT)
    private IngestServiceAgreementRouteProxy ingestServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_SERVICE_AGREEMENT)
    private UpdateServiceAgreementRouteProxy updateServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT)
    private ListAdminsForServiceAgreementRouteProxy listAdminUsersForServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_PARTICIPANTS)
    private GetServiceAgreementParticipantsRouteProxy getServiceAgreementParticipantsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID)
    private GetServiceAgreementByExternalIdRouteProxy getServiceAgreementByExternalIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_UNEXPOSED_USERS)
    private GetUnexposedUsersRouteProxy getUnexposedUsersRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_EDIT_SERVICE_AGREEMENT)
    private EditServiceAgreementRouteProxy editServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_SERVICE_AGREEMENTS_HIERARCHY)
    private ListServiceAgreementsHierarchyRouteProxy listServiceAgreementsHierarchyRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_INGEST_PARTICIPANT_UPDATE)
    private UpdateParticipantsRouteProxy updateServiceAgreementParticipant;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_USERS_IN_SA)
    private UpdateUsersInServiceAgreementRouteProxy updateUsersInServiceAgreementRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_INGEST_ADMINS_UPDATE)
    private UpdateBatchAdminsRouteProxy updateBatchAdminsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS)
    private UpdateAssignUsersPermissionsRouteProxy updateAssignUsersPermissionsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_ASSIGNED_USERS_PERMISSIONS)
    private GetAssignedUserPermissionsRouteProxy getAssignedUserPermissionsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_BATCH_SERVICE_AGREEMENT)
    private DeleteBatchServiceAgreementRouteProxy deleteBatchServiceAgreementRouteProxy;

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_ADD_SERVICE_AGREEMENT endpoint.
     *
     * @param serviceAgreementPostRequestBody incoming data {@link ServiceAgreementPostRequestBody}.
     * @return {@link ServiceAgreementPostResponseBody}
     */
    public ServiceAgreementPostResponseBody addServiceAgreement(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody) {
        LOGGER.info("Trying to add new service agreement {}", serviceAgreementPostRequestBody);
        return addServiceAgreementRouteProxy
            .addServiceAgreement(getInternalRequest(serviceAgreementPostRequestBody, internalRequestContext))
            .getData();
    }


    /**
     * Forwards the request to ListServiceAgreementsRoute.
     *
     * @return {@link PaginationDto} of type {@link ServiceAgreementGetResponseBody}
     */
    public PaginationDto<ServiceAgreementGetResponseBody> getServiceAgreements(
        String creatorId,
        String query,
        Integer from,
        Integer size,
        String cursor) {
        LOGGER.info("Trying to get service agreements with creator {}", creatorId);
        return listServiceAgreementsRouteProxy
            .getServiceAgreements(getVoidInternalRequest(internalRequestContext), creatorId, query, from, size,
                cursor).getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_UPDATE_ADMINS endpoint.
     *
     * @param serviceAgreementId - serviceagreement id that should be updated.
     * @param adminsPutRequestBody - {@link AdminsPutRequestBody}
     */
    public void updateAdmins(AdminsPutRequestBody adminsPutRequestBody, String serviceAgreementId) {
        LOGGER.info("Trying to update admins of service agreement with id {}", serviceAgreementId);

        updateServiceAgreementAdminsRouteProxy
            .updateAdmins(getInternalRequest(adminsPutRequestBody, internalRequestContext), serviceAgreementId);
    }

    /**
     * Forwards the request to ListUsersForServiceAgreementRoute.
     *
     * @param serviceAgreementId service agreement id
     * @param searchQuery search query
     * @param from starting page
     * @param size page size
     * @param cursor search cursor
     * @return {@link ListElementsWrapper}
     */
    public ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersForServiceAgreement(
        String serviceAgreementId, String searchQuery, Integer from, Integer size, String cursor) {
        LOGGER.info(
            "Trying to get all users for service agreement with id {}, "
                + "query parameter {}, from parameter {} and size parameter {}",
            serviceAgreementId, searchQuery, from, size);
        return listUsersForServiceAgreementRouteProxy
            .getUsersForServiceAgreement(getVoidInternalRequest(internalRequestContext), serviceAgreementId,
                searchQuery, from, size, cursor).getData();
    }

    /**
     * Forwards the request to AddUserInServiceAgreementRoute.
     *
     * @param usersAddPostRequestBody {@link PresentationUsersForServiceAgreementRequestBody}
     * @param serviceAgreementId id of service agreement
     */
    public void addUsersInServiceAgreement(PresentationUsersForServiceAgreementRequestBody usersAddPostRequestBody,
        String serviceAgreementId) {
        LOGGER.info("Trying to add users {} in service agreement {}", usersAddPostRequestBody.getUsers(),
            serviceAgreementId);
        addUsersInServiceAgreementRouteProxy
            .addUsersInServiceAgreement(getInternalRequest(usersAddPostRequestBody, internalRequestContext),
                serviceAgreementId);
    }

    /**
     * Forwards the request to {@link GetServiceAgreementRoute}.
     *
     * @param serviceAgreementId the id of the service agreement
     * @return {@link ServiceAgreementItemGetResponseBody}
     */
    public ServiceAgreementItemGetResponseBody getServiceAgreementById(String serviceAgreementId) {
        LOGGER.info("Trying to get service agreement with id {}", serviceAgreementId);
        return getServiceAgreementRouteProxy
            .getServiceAgreementById(getVoidInternalRequest(internalRequestContext), serviceAgreementId).getData();
    }

    /**
     * Forwards the request to RemoveUserFromServiceAgreementRoute.
     *
     * @param usersForServiceAgreementRequestBody {@link PresentationUsersForServiceAgreementRequestBody}
     * @param serviceAgreementId id of service agreement
     */
    public void removeUsersFromServiceAgreement(
        PresentationUsersForServiceAgreementRequestBody usersForServiceAgreementRequestBody,
        String serviceAgreementId) {
        LOGGER
            .info("Trying to remove users {} from service agreement {}", usersForServiceAgreementRequestBody.getUsers(),
                serviceAgreementId);

        removeUsersFromServiceAgreementRouteProxy.removeUsersFromServiceAgreement(
            getInternalRequest(usersForServiceAgreementRequestBody, internalRequestContext), serviceAgreementId);
    }

    /**
     * Forwards the request to direct:business.ingestServiceAgreement.
     *
     * @param serviceAgreementIngestPostRequestBody the data to be ingested
     * @return {@link ServiceAgreementIngestPostResponseBody}
     */
    public ServiceAgreementIngestPostResponseBody ingestServiceAgreement(
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody) {

        return ingestServiceAgreementRouteProxy.ingestServiceAgreement(
            getInternalRequest(serviceAgreementIngestPostRequestBody, internalRequestContext)).getData();
    }

    /**
     * Forwards the request to UpdateServiceAgreementRoute.
     *
     * @param serviceAgreementPutRequestBody - {@link ServiceAgreementPutRequestBody}
     * @param serviceAgreementId - id of the service agreement
     */
    public void updateServiceAgreement(ServiceAgreementPutRequestBody serviceAgreementPutRequestBody,
        String serviceAgreementId) {
        LOGGER.info("Trying to update name, description, externalId and status for service agreement {}",
            serviceAgreementId);

        updateServiceAgreementRouteProxy
            .updateServiceAgreement(getInternalRequest(serviceAgreementPutRequestBody, internalRequestContext),
                serviceAgreementId).getData();
    }

    /**
     * Forwards the request to UpdateServiceAgreementRoute.
     *
     * @param serviceAgreementStatePutRequestBody - {@link ServiceAgreementStatePutRequestBody}
     * @param serviceAgreementId - id of the service agreement
     */
    public void updateServiceAgreementState(ServiceAgreementStatePutRequestBody serviceAgreementStatePutRequestBody,
        String serviceAgreementId) {
        LOGGER.info("Trying to update status for service agreement {}", serviceAgreementId);

        updateServiceAgreementStateHandler
            .handleRequest(new SingleParameterHolder<>(serviceAgreementId), serviceAgreementStatePutRequestBody);
    }

    /**
     * Forwards the request to ListUsersForServiceAgreementRoute.
     *
     * @param serviceAgreementId id of service agreement
     * @return list of {@link ServiceAgreementUsersGetResponseBody}
     */
    public List<ServiceAgreementUsersGetResponseBody> getServiceAgreementAdmins(String serviceAgreementId) {
        LOGGER.info("Trying to get all admins for service agreement with id {}", serviceAgreementId);
        return listAdminUsersForServiceAgreementRouteProxy
            .getAdminsForServiceAgreement(getVoidInternalRequest(internalRequestContext), serviceAgreementId)
            .getData();
    }

    /**
     * Forwards the request to GetServiceAgreementParticipantsRoute.
     *
     * @param serviceAgreementId id of service agreement
     * @return list of {@link ServiceAgreementParticipantsGetResponseBody}
     */
    public List<ServiceAgreementParticipantsGetResponseBody> getServiceAgreementParticipants(
        String serviceAgreementId) {
        LOGGER.info("Trying to get participants in service agreement with id {}", serviceAgreementId);
        return getServiceAgreementParticipantsRouteProxy
            .getServiceAgreementParticipants(getVoidInternalRequest(internalRequestContext), serviceAgreementId)
            .getData();
    }

    /**
     * Forwards the request to {@link GetServiceAgreementByExternalIdRouteProxy}.
     *
     * @param externalId the id of the service agreement
     * @return {@link ServiceAgreementExternalIdGetResponseBody}
     */
    public ServiceAgreementExternalIdGetResponseBody getServiceAgreementByExternalId(String externalId) {
        LOGGER.info("Trying to get service agreement with external id {}", externalId);
        return getServiceAgreementByExternalIdRouteProxy
            .getServiceAgreementByExternalId(getVoidInternalRequest(internalRequestContext), externalId).getData();
    }

    /**
     * Forwards the request to GetUnexposedUsersRoute.
     *
     * @param serviceAgreementId - id of the service agreement from context
     * @param from - Beginning of the page
     * @param size - Pagination size
     * @param query - query parameter for searching unexposed users.
     * @param cursor - Pagination cursor
     * @return {@link PaginationDto} of type {@link UnexposedUsersGetResponseBody}
     */
    public PaginationDto<UnexposedUsersGetResponseBody> getUnexposedUsers(String serviceAgreementId,
        Integer from, Integer size, String query, String cursor) {
        LOGGER.info("Trying to get unexposed users for service agreement {}.", serviceAgreementId);
        return getUnexposedUsersRouteProxy.getUnexposedUsers(serviceAgreementId, from, size, query, cursor).getData();
    }

    /**
     * Forwards the request to EditServiceAgreementRoute.
     *
     * @param serviceAgreementSave - the internal request
     * @param id - id of the service agreement
     */
    public void editServiceAgreement(ServiceAgreementSave serviceAgreementSave, String id) {
        LOGGER.info("Trying to update service agreement {}", id);

        editServiceAgreementRouteProxy
            .editServiceAgreement(getInternalRequest(serviceAgreementSave, internalRequestContext), id);
    }

    /**
     * Forwards the request to ListServiceAgreementsHierarchyRoute.
     *
     * @param creatorId - id of the legal entity
     * @param userId - id of the user
     * @param query search query
     * @param from starting page
     * @param size page size
     * @param cursor search cursor
     * @return {@link PaginationDto} of type {@link PresentationServiceAgreement}
     */
    public PaginationDto<PresentationServiceAgreement> listServiceAgreements(String creatorId, String userId,
        String query, Integer from, Integer size, String cursor) {
        LOGGER.info("Trying to get service agreements under hierarchy of creator {}", creatorId);
        return listServiceAgreementsHierarchyRouteProxy
            .listServiceAgreements(getVoidInternalRequest(internalRequestContext), creatorId, userId, query, from,
                size, cursor).getData();
    }

    /**
     * Forwards the request to UpdateParticipantsRouteProxy.
     *
     * @param presentationParticipantsPut - data to be updated {@link PresentationParticipantsPut}
     * @return - list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> updateParticipants(PresentationParticipantsPut presentationParticipantsPut) {
        LOGGER.info("Updating participants {}", presentationParticipantsPut);
        return updateServiceAgreementParticipant
            .updateParticipants(getInternalRequest(presentationParticipantsPut, internalRequestContext)).getData();
    }

    /**
     * Forwards the request to UpdateBatchAdminsRouteProxy.
     *
     * @param presentationServiceAgreementUsersUpdate - data of {@link PresentationServiceAgreementUsersUpdate}
     * @return - list of {@link BatchResponseItemExtended}
     */
    public List<BatchResponseItemExtended> updateServiceAgreementAdminsBatch(
        PresentationServiceAgreementUsersUpdate presentationServiceAgreementUsersUpdate) {
        return updateBatchAdminsRouteProxy
            .updateBatchAdmins(getInternalRequest(presentationServiceAgreementUsersUpdate, internalRequestContext))
            .getData();
    }

    /**
     * Forwards the request to UpdateUsersInServiceAgreementRouteProxy.
     *
     * @param presentationServiceAgreementUsersUpdate - {@link PresentationServiceAgreementUsersUpdate}
     * @return resulting list of {@link BatchResponseItemExtended}.
     */

    public List<BatchResponseItemExtended> updateUsersInServiceAgreement(
        PresentationServiceAgreementUsersUpdate presentationServiceAgreementUsersUpdate) {
        return updateUsersInServiceAgreementRouteProxy.updateUsersInServiceAgreement(
            getInternalRequest(presentationServiceAgreementUsersUpdate, internalRequestContext)).getData();

    }

    /**
     * Updates permission for user in a given service agreement.
     *
     * @param presentationFunctionDataGroupItems {@link PresentationFunctionDataGroupItems}
     * @param id service agreement
     * @param userId user id
     * @return {@link PresentationApprovalStatus}
     */
    public PresentationApprovalStatus putAssignUsersPermissions(
        PresentationFunctionDataGroupItems presentationFunctionDataGroupItems, String id, String userId) {
        LOGGER.info("Trying to update users permission {} for user id {} in service agreement id {}",
            presentationFunctionDataGroupItems, userId, id);
        return updateAssignUsersPermissionsRouteProxy.putAssignUsersPermissions(
            getInternalRequest(presentationFunctionDataGroupItems, internalRequestContext), id, userId).getData();
    }

    /**
     * Returns permission for user in a given service agreement with "approvalId".
     *
     * @param serviceAgreementId service agreement id.
     * @param userId id of an user.
     * @return permissions of the user wrapped into InternalRequest.
     */
    public PresentationApprovalPermissions getAssignedUsersPermissions(String serviceAgreementId, String userId) {
        LOGGER.info("Trying to get users permissions for user id {} in service agreement is {}", userId,
            serviceAgreementId);
        return getAssignedUserPermissionsRouteProxy
            .getAssignedUsersPermissions(getVoidInternalRequest(internalRequestContext), serviceAgreementId, userId)
            .getData();
    }

    /**
     * Forwards the request to {@link DeleteBatchServiceAgreementRouteProxy}.
     *
     * @param presentationDeleteServiceAgreements - service agreements to be deleted
     * @return list of {@link BatchResponseItem}
     */
    public List<BatchResponseItem> batchDeleteServiceAgreement(
        PresentationDeleteServiceAgreements presentationDeleteServiceAgreements) {
        LOGGER.info("Trying to delete batch service agreement {} ", presentationDeleteServiceAgreements);
        return deleteBatchServiceAgreementRouteProxy.deleteBatchServiceAgreement(
            getInternalRequest(presentationDeleteServiceAgreements, internalRequestContext)).getData();
    }
}

