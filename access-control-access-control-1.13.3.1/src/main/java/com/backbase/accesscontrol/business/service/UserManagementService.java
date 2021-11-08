package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_082;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.dbs.user.api.client.v2.UserManagementApi;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

    protected UserManagementApi userManagementApi;
    protected PayloadConverter payloadConverter;

    /**
     * Retrieve all users bodies by list of users ids.
     *
     * @param usersIds list of users ids
     * @param query    Filter by service agreement name
     * @param from     Beginning of the page
     * @param size     Pagination size
     * @param cursor   Pagination cursor
     * @return {@link ListElementsWrapper}
     */
    public ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersForServiceAgreement(Set<String> usersIds,
        String query, Integer from, Integer size, String cursor) {
        GetUsersList usersByIds = getUsersByIds(new ArrayList<>(usersIds), query, from, size, cursor);
        return convertObject(usersByIds);
    }

    /**
     * Retrieve all users bodies by creator legal entity.
     *
     * @return List of users {@link ServiceAgreementUsersGetResponseBody}
     */
    public ListElementsWrapper<ServiceAgreementUsersGetResponseBody> getUsersForCreatorLegalEntity(
        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest getUsersByLegalEntityIdsRequest) {
        if (Objects.isNull(getUsersByLegalEntityIdsRequest.getFrom())) {
            getUsersByLegalEntityIdsRequest.setFrom(0);
        }
        if (Objects.isNull(getUsersByLegalEntityIdsRequest.getSize())) {
            getUsersByLegalEntityIdsRequest.setSize(10);
        }
        GetUsersList getUsers =
            userManagementApi.getUsersByLegalEntityIds(getUsersByLegalEntityIdsRequest, true);
        return convertObject(getUsers);
    }

    /**
     * Gets users from Presentation service by Internal User Ids sent as coma separated string.
     *
     * @param users  Internal User Ids of the users
     * @param query  Filter by service agreement name
     * @param from   Beginning of the page
     * @param size   Pagination size
     * @param cursor Pagination cursor
     * @return List of users {@link GetUsersList}
     */
    public GetUsersList getUsersByIds(List<String> users, String query,
        Integer from,
        Integer size, String cursor) {
        GetUsersList response;
        if (users.isEmpty()) {
            response = new GetUsersList();
            response.setUsers(new ArrayList<>());
            response.setTotalElements(0L);
        } else {
            String userIds = String.join(",", users);
            response =
                userManagementApi.getUsersInBulk(userIds, query, from, cursor, size);
        }
        return response;
    }

    /**
     * Method that checks if the users belong in the Legal Entities that are participants in the service agreement.
     *
     * @param users                        users list
     * @param serviceAgreementParticipants service agreement
     * @return if the users which belong in the legal entities are participants in the service agreement
     */
    public boolean usersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(GetUsersList users,
        List<Participant> serviceAgreementParticipants) {
        List<String> usersLegalEntities = users.getUsers()
            .stream()
            .map(GetUser::getLegalEntityId)
            .collect(Collectors.toList());

        List<String> participantsSharingUsers = serviceAgreementParticipants.stream()
            .filter(Participant::getSharingUsers)
            .map(Participant::getId)
            .collect(Collectors.toList());

        return !participantsSharingUsers.containsAll(usersLegalEntities);
    }

    private ListElementsWrapper<ServiceAgreementUsersGetResponseBody> convertObject(GetUsersList users) {
        List<ServiceAgreementUsersGetResponseBody> returningRequest = users
            .getUsers()
            .stream()
            .map(user -> payloadConverter.convert(user, ServiceAgreementUsersGetResponseBody.class))
            .collect(Collectors.toList());
        return new ListElementsWrapper<>(returningRequest, users.getTotalElements());
    }

    /**
     * Gets the user from User Manager Api by external ID.
     *
     * @param userExternalId user's external ID.
     * @return user object found by its user ID.
     */
    public GetUser getUserByExternalId(String userExternalId) {
        return userManagementApi.getUserByExternalId(userExternalId, true);
    }

    /**
     * Gets the user from User Manager Api by internal ID.
     *
     * @param userInternalId user's internal ID.
     * @return user object found by its user ID.
     */
    public GetUser getUserByInternalId(String userInternalId) {
        LOGGER.info("Getting user by internal id: {}", userInternalId);
        return userManagementApi.getUserById(userInternalId, true);
    }

    /**
     * Gets users from P&P service by Internal User Ids sent as coma separated string.
     *
     * @param userIds Internal User Ids of the users to be assigned as admins
     * @return List of users {@link GetUser}
     */
    public GetUsersList getUsers(String userIds) {
        return userManagementApi.getUsersInBulk(userIds, null, null, null, null);
    }

    /**
     * Returns stream from all participant users.
     *
     * @param participantsToIngest set of {@link ParticipantIngest}
     * @return stream from all participant users or empty if none present
     */
    private Stream<String> getUsers(Set<ParticipantIngest> participantsToIngest) {
        return participantsToIngest.stream()
            .filter(ParticipantIngest::getSharingUsers)
            .flatMap(
                participantToIngest -> Optional.ofNullable(participantToIngest.getUsers()).stream()
                    .flatMap(Collection::stream)
            )
            .map(this::getString);
    }

    /**
     * Returns list of users by provided set of external ids.
     *
     * @param userExternalIds list of external ids
     * @return list of users {@link GetUser}.
     */
    public List<GetUser> getUserByExternalIds(Set<String> userExternalIds) {
        return
            userManagementApi.getUsersByExternalIds(Lists.newArrayList(userExternalIds));
    }

    /**
     * Returns a map with UserIds grouped by LegalEntityId.
     *
     * @param users list of users.
     * @return map of UserIds grouped by LegalEntityId
     */
    public Map<String, Set<String>> getUsersByLegalEntityId(List<? extends GetUser> users) {
        return users.stream().collect(
            Collectors.groupingBy(GetUser::getLegalEntityId,
                Collectors.mapping(GetUser::getId, Collectors.toSet())
            )
        );
    }

    /**
     * Returns map of user grouped by its external id.
     *
     * @param ingestPostRequestBody list of users
     * @return users mapped by external id
     */
    public Map<String, GetUser> getUsersGroupedByExternalId(
        ServiceAgreementIngestPostRequestBody ingestPostRequestBody) {
        Set<String> userExternalIdsUnique = getUserExternalIds(ingestPostRequestBody);
        List<GetUser> userByExternal = getUsersByExternalIdsIfUsersAreProvided(
            userExternalIdsUnique);
        if (userExternalIdsUnique.size() != userByExternal.size()) {
            throw getBadRequestException(ERR_AG_082.getErrorMessage(), ERR_AG_082.getErrorCode());
        }
        return userByExternal.stream()
            .collect(Collectors.toMap(user -> getString(user.getExternalId()), userItemBase -> userItemBase));
    }

    private Set<String> getUserExternalIds(ServiceAgreementIngestPostRequestBody data) {
        return Stream.of(
            getAdminIds(data.getParticipantsToIngest()),
            getUsers(data.getParticipantsToIngest()))
            .reduce(Stream::concat)
            .orElseGet(Stream::empty)
            .collect(toSet());
    }

    private String getString(String nullableString) {
        if (Objects.isNull(nullableString)) {
            LOGGER.warn("User with id {} does not exist", nullableString);
            throw getBadRequestException(ERR_AG_082.getErrorMessage(), ERR_AG_082.getErrorCode());
        }
        return nullableString.toLowerCase();
    }

    private Stream<String> getAdminIds(Set<ParticipantIngest> participants) {
        return participants.stream()
            .flatMap(participant -> Optional.ofNullable(participant.getAdmins()).stream().flatMap(Collection::stream)
                .map(this::getString)
            );
    }

    /**
     * Returns all users from the LE participants in SA that share users without already exposed users.
     *
     * @param participantsSharingUsers participants that share users
     * @param exposedUserIds           ids of exposed users
     * @param from                     Beginning of the page
     * @param size                     Pagination size
     * @param query                    query parameter for searching unexposed users.
     * @param cursor                   Pagination cursor
     * @param sort                     sort parameter
     * @return {@link GetUsersList}
     */
    public GetUsersList getUnexposedUsers(
        Set<String> participantsSharingUsers,
        Set<String> exposedUserIds, Integer from, Integer size, String query, String cursor, String sort) {
        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest usersByLegalEntityIdsRequest = createUsersByLegalEntityIdsPostRequestBody(
            participantsSharingUsers, exposedUserIds, from, size, query, cursor, sort);
        return userManagementApi.getUsersByLegalEntityIds(usersByLegalEntityIdsRequest, true);

    }

    private GetUsersByLegalEntityIdsRequest createUsersByLegalEntityIdsPostRequestBody(
        Set<String> participantsSharingUsers, Set<String> exposedUserIds, Integer from, Integer size, String query,
        String cursor, String sort) {
        GetUsersByLegalEntityIdsRequest getUsersByLegalEntityIdsRequest =
            new GetUsersByLegalEntityIdsRequest();
        getUsersByLegalEntityIdsRequest.setLegalEntityIds(Lists.newArrayList(participantsSharingUsers));
        getUsersByLegalEntityIdsRequest.setExcludeIds(Lists.newArrayList(exposedUserIds));
        getUsersByLegalEntityIdsRequest.setFrom(from);
        getUsersByLegalEntityIdsRequest.setSize(size);
        getUsersByLegalEntityIdsRequest.setQuery(query);
        getUsersByLegalEntityIdsRequest.setCursor(cursor);
        getUsersByLegalEntityIdsRequest.setSortOrder(sort);
        return getUsersByLegalEntityIdsRequest;
    }

    private List<GetUser> getUsersByExternalIdsIfUsersAreProvided(
        Set<String> userExternalIdsUnique) {
        List<GetUser> userByExternalIds;
        if (userExternalIdsUnique.isEmpty()) {
            userByExternalIds = new ArrayList<>();
        } else {
            userByExternalIds = getUserByExternalIds(userExternalIdsUnique);
        }
        return userByExternalIds;
    }

    /**
     * Gets users from P&P service by External ids.
     *
     * @param userIds External User Ids
     * @return List of users {@link GetUser}
     */
    public List<GetUser> getUsersByExternalIds(List<String> userIds) {
        return userManagementApi.getUsersByExternalIdsBulk(userIds);
    }
}
