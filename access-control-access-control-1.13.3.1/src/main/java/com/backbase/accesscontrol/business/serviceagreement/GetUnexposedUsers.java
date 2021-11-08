package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving unexposed users This class is the business process component of the access-group
 * presentation service, communicating with the p&p service and user presentation service, retrieving all unexposed
 * Users for Service Agreement.
 */
@Service
@AllArgsConstructor
public class GetUnexposedUsers {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetUnexposedUsers.class);

    private UserManagementService userManagementService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    /**
     * Retrieve all unexposed users by service agreement id.
     *
     * @param serviceAgreementId service agreement unique id
     * @param from               Beginning of the page
     * @param size               Pagination size
     * @param query              query parameter for searching unexposed users.
     * @param cursor             Pagination cursor
     * @return BusinessProcessResult with response data.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_UNEXPOSED_USERS)
    public InternalRequest<PaginationDto<UnexposedUsersGetResponseBody>> getUnexposedUsers(
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("query") String query,
        @Header("cursor") String cursor) {
        LOGGER.info(
            "Trying to list all unexposed users for service agreement id {}, query parameter {}, "
                + "from parameter {} and size parameter {}",
            serviceAgreementId, query, from, size);

        ServiceAgreementItem serviceAgreementItem = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId);

        if (serviceAgreementItem.getIsMaster()) {
            PaginationDto<UnexposedUsersGetResponseBody> body = new PaginationDto<>(1L,
                new ArrayList<>());
            return getInternalRequest(body, new DefaultInternalRequestContext());
        }

        List<Participant> participants = getParticipants(serviceAgreementId);
        if (participants.isEmpty()) {
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_033.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_033.getErrorCode());
        }

        List<Participant> participantsSharingUsers = getParticipantsSharingUsers(participants);
        Set<String> participantsSharingUsersIds = getParticipantsSharingUsersIds(participantsSharingUsers);
        Set<String> exposedUserIds = getExposedUsers(serviceAgreementId);

        GetUsersList unexposedUsers = userManagementService
            .getUnexposedUsers(participantsSharingUsersIds, exposedUserIds, from, size, query, cursor,
                "legalEntityId,fullName");

        PaginationDto<UnexposedUsersGetResponseBody> body = new PaginationDto<>(unexposedUsers.getTotalElements(),
            createListOfUnexposedUsersGetResponseBodies(participantsSharingUsers, unexposedUsers));
        return getInternalRequest(body, new DefaultInternalRequestContext());
    }

    private Set<String> getParticipantsSharingUsersIds(List<Participant> participantsSharingUsers) {
        return participantsSharingUsers
            .stream()
            .map(Participant::getId)
            .collect(Collectors.toSet());
    }

    private Set<String> getExposedUsers(String serviceAgreementId) {
        return persistenceServiceAgreementService
            .getServiceAgreementUsers(serviceAgreementId).getUserIds();
    }

    private List<UnexposedUsersGetResponseBody> createListOfUnexposedUsersGetResponseBodies(
        List<Participant> participantsSharingUsers,
        GetUsersList unexposedUsers) {
        return unexposedUsers.getUsers().stream()
            .map(user -> new UnexposedUsersGetResponseBody()
                .withId(user.getId())
                .withExternalId(user.getExternalId())
                .withFullName(user.getFullName())
                .withLegalEntityId(user.getLegalEntityId())
                .withLegalEntityName(getLegalEntityName(user.getLegalEntityId(), participantsSharingUsers)))
            .collect(Collectors.toList());
    }

    private List<Participant> getParticipants(String serviceAgreementId) {
        return persistenceServiceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);
    }

    private List<Participant> getParticipantsSharingUsers(List<Participant> participants) {
        return participants
            .stream()
            .filter(Participant::getSharingUsers)
            .collect(Collectors.toList());
    }

    private String getLegalEntityName(String legalEntityId, List<Participant> participantsSharingUsers) {
        String name = "";
        Optional<Participant> participantOptional = participantsSharingUsers.stream()
            .filter(participant -> participant.getId().equals(legalEntityId))
            .findFirst();

        if (participantOptional.isPresent()) {
            name = participantOptional.get().getName();
        }
        return name;
    }
}
