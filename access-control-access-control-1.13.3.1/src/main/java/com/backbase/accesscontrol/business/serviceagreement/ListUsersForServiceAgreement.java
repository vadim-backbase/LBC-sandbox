package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static java.util.Collections.emptyList;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving a List of Users for service Agreement. This class is the business process component of
 * the access-group presentation service, communicating with the p&p service and user presentation service, retrieving
 * all Users for Service Agreement.
 */
@Service
@AllArgsConstructor
public class ListUsersForServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListUsersForServiceAgreement.class);
    private UserManagementService userManagementService;
    private PersistenceLegalEntityService persistenceLegalEntityService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    /**
     * Sends request to pandp for retrieving users on given service agreement.
     *
     * @param internalRequest    internal request
     * @param serviceAgreementId service agreement id
     * @param searchQuery        query parameter
     * @param from               Beginning of the page
     * @param size               Pagination size
     * @param cursor             Pagination cursor
     * @return internal request of {@link ListElementsWrapper}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_USERS_FOR_SERVICE_AGREEMENT)
    public InternalRequest<ListElementsWrapper<ServiceAgreementUsersGetResponseBody>> getUsersForServiceAgreement(
        InternalRequest<Void> internalRequest,
        @Header("id") String serviceAgreementId,
        @Header("query") String searchQuery,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("cursor") String cursor) {
        LOGGER.info(
            "Trying to list all users for service agreement id {}, query parameter {}, "
                + "from parameter {} and size parameter {}",
            serviceAgreementId, searchQuery, from, size);
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> response =
            getUsersForServiceAgreement(serviceAgreementId, searchQuery, from, size, cursor);
        return getInternalRequest(response, internalRequest.getInternalRequestContext());
    }

    private ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersForServiceAgreement(
        String serviceAgreementId, String query, Integer from, Integer size, String cursor) {
        LOGGER.info("Getting Service Agreement with id {}", serviceAgreementId);
        ServiceAgreementItem serviceAgreementItem = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId);

        if (serviceAgreementItem.getIsMaster()) {
            return getUsersOfMasterServiceAgreement(query, from, size, cursor, serviceAgreementItem);
        }

        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements
            .ServiceAgreementUsersGetResponseBody serviceAgreementUsers = persistenceServiceAgreementService
            .getServiceAgreementUsers(serviceAgreementId);

        if (serviceAgreementUsers.getUserIds().isEmpty()) {
            return new ListElementsWrapper<>(emptyList(), 0L);
        }

        List<Participant> serviceAgreementParticipants = persistenceServiceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);

        Map<String, String> participantMap = serviceAgreementParticipants.stream()
            .collect(Collectors.toMap(Participant::getId, Participant::getName));

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = userManagementService
            .getUsersForServiceAgreement(serviceAgreementUsers.getUserIds(), query, from, size, cursor);

        addLegalEntityNameForUser(participantMap, usersOfServiceAgreementDto);

        return usersOfServiceAgreementDto;
    }

    private ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersOfMasterServiceAgreement(String query,
        Integer from, Integer size, String cursor, ServiceAgreementItem serviceAgreementItem) {

        String creatorLegalEntityId = serviceAgreementItem.getCreatorLegalEntity();
        String legalEntityName = persistenceLegalEntityService.getLegalEntityById(creatorLegalEntityId).getName();

        Map<String, String> legalEntityMap = new HashMap<>();
        legalEntityMap.put(creatorLegalEntityId, legalEntityName);

        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest item =
            new GetUsersByLegalEntityIdsRequest();
        item.setLegalEntityIds(Lists.newArrayList(creatorLegalEntityId));
        item.setQuery(query);
        item.setFrom(from);
        item.setCursor(cursor);
        item.setSize(size);

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = userManagementService
            .getUsersForCreatorLegalEntity(item);

        addLegalEntityNameForUser(legalEntityMap, usersOfServiceAgreementDto);

        return usersOfServiceAgreementDto;
    }

    @SuppressWarnings("squid:S3864")
    private void addLegalEntityNameForUser(Map<String, String> participantMap,
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> data) {
        data.setRecords(
            data.getRecords()
                .stream()
                .peek(user -> user.setLegalEntityName(
                    participantMap.get(user.getLegalEntityId())))
                .collect(Collectors.toList())
        );

    }

}
