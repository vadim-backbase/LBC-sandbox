package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_082;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipant;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipants;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.GetUserToServiceAgreementUsersGetResponseBodyMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementServiceTest {

    @Spy
    private final PayloadConverter payloadConverter = new PayloadConverter(Collections.singletonList(
        spy(Mappers.getMapper(GetUserToServiceAgreementUsersGetResponseBodyMapper.class))));
    @Mock
    private com.backbase.dbs.user.api.client.v2.UserManagementApi userManagementApi;
    @InjectMocks
    private UserManagementService userManagementService;

    @Mock
    private UserContextUtil userContextUtil;

    @Test
    public void testGetUsers() {
        String userAdminsToCheck = "user IDs";

        List<GetUser> list = new ArrayList<>();
        GetUser user = new GetUser();
        user.setId("id");
        list.add(user);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList data = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        data.setUsers(list);

        when(
            userManagementApi.getUsersInBulk(eq(userAdminsToCheck), isNull(), isNull(), isNull(), isNull()))
            .thenAnswer(ans -> data);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList users = userManagementService
            .getUsers(userAdminsToCheck);
        assertEquals(data.getUsers(), users.getUsers());
    }

    @Test
    public void testGetAuthenticatedUserNameEmpty() {

        when(userContextUtil.getOptionalAuthenticatedUserName()).thenReturn(Optional.empty());
        Optional<String> authenticatedUserName = userContextUtil.getOptionalAuthenticatedUserName();

        assertFalse(authenticatedUserName.isPresent());
    }

    @Test
    public void testGetUsersByLegalEntityId() {
        GetUser user1 = new GetUser();
        user1.setLegalEntityId("1");
        user1.setId("1");
        GetUser user12 = new GetUser();
        user12.setLegalEntityId("1");
        user12.setId("2");
        GetUser user2 = new GetUser();
        user2.setLegalEntityId("2");
        user2.setId("1");

        Map<String, Set<String>> users = userManagementService.getUsersByLegalEntityId(asList(user1, user12, user2));

        assertThat(users.get(user1.getLegalEntityId()), containsInAnyOrder(user1.getId(), user12.getId()));
        assertThat(users.get(user2.getLegalEntityId()), containsInAnyOrder(user2.getId()));
    }

    @Test
    public void testGetUserByExternalId() {
        String externalId = "User external ID";

        GetUser data = new GetUser();
        when(
            userManagementApi.getUserByExternalId(eq(externalId), eq(true)))
            .thenAnswer(ans -> data);

        GetUser userByExternalId = userManagementService
            .getUserByExternalId(externalId);

        assertEquals(data, userByExternalId);

    }

    @Test
    public void testGetUserByInternalId() {
        String internalId = "U-01";

        GetUser user = new GetUser();
        user.setExternalId("user");
        user.setId(internalId);

        when(
            userManagementApi.getUserById(internalId, true))
            .thenAnswer(ans -> user);

        GetUser userByInternalId = userManagementService.getUserByInternalId(internalId);

        assertEquals(user, userByInternalId);
    }

    @Test
    public void shouldThrowBadRequestFromPresentation() {
        String code = "code";
        String message = "message";
        LinkedHashSet<String> userExternalIds = new LinkedHashSet<>(asList("1", "2", "3"));

        when(
            userManagementApi.getUsersByExternalIds(Lists.newArrayList(userExternalIds)))
            .thenThrow(getBadRequestException(message, code));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> userManagementService.getUserByExternalIds(userExternalIds));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(message, code)));
    }

    @Test
    public void shouldThrowInternalServerExceptionFromPresentation() {
        String message = "message";
        LinkedHashSet<String> legalEntityIds = new LinkedHashSet<>(asList("1", "2", "3"));

        when(
            userManagementApi.getUsersByExternalIds(Lists.newArrayList(asList("1", "2", "3"))))
            .thenThrow(getInternalServerErrorException(message));

        assertThrows(InternalServerErrorException.class,
            () -> userManagementService.getUserByExternalIds(legalEntityIds));
    }

    @Test
    public void shouldReturnUsers() {
        LinkedHashSet<String> legalEntityIds = new LinkedHashSet<>(asList("1", "2", "3"));

        GetUser user1 = new GetUser();
        user1.setExternalId("1");
        GetUser user2 = new GetUser();
        user2.setExternalId("2");
        GetUser user3 = new GetUser();
        user3.setExternalId("3");

        List<GetUser> data = new ArrayList<>();
        data.add(user1);
        data.add(user2);
        data.add(user3);
        when(
            userManagementApi.getUsersByExternalIds(asList("1", "2", "3")))
            .thenAnswer(ans -> data);

        List<GetUser> userByExternalIdsData = userManagementService
            .getUserByExternalIds(legalEntityIds);

        assertEquals(3, userByExternalIdsData.size());
        assertThat(userByExternalIdsData,
            hasItems(
                hasProperty("externalId", is("1")),
                hasProperty("externalId", is("2")),
                hasProperty("externalId", is("3"))
            )
        );
    }

    @Test
    public void shouldReturnCorrectlyGroupedUsersByExternalId() {
        Map<String, GetUser> usersMap = new HashMap<>();
        usersMap.put("u1_1", getUsers("u1_1", null, null));
        usersMap.put("u1_2", getUsers("u1_2", null, null));
        usersMap.put("u2_1", getUsers("u2_1", null, null));
        usersMap.put("u2_2", getUsers("u2_2", null, null));
        usersMap.put("u3_1", getUsers("u3_1", null, null));
        usersMap.put("u3_2", getUsers("u3_2", null, null));

        ArrayList<GetUser> data = new ArrayList<>(usersMap.values());

        when(
            userManagementApi.getUsersByExternalIds(
                anyList()))
            .thenAnswer(ans -> data);

        Map<String, GetUser> usersGroupedByExternalId = userManagementService
            .getUsersGroupedByExternalId(new ServiceAgreementIngestPostRequestBody()
                .withParticipantsToIngest(getParticipants(
                    getParticipant("1", asList("u1_1", "u1_2"), false, true, new ArrayList<>()),
                    getParticipant("2", asList("u2_1", "u2_2"), true, true, new ArrayList<>()),
                    getParticipant("3", asList("u3_1", "u3_2"), true, false, new ArrayList<>())
                )));
        assertEquals(usersGroupedByExternalId, usersMap);
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidAdmins() {
        Set<ParticipantIngest> participants = getParticipants(
            getParticipant("1", asList("u1_1", "u1_2", null), false, true, new ArrayList<>())
        );
        ServiceAgreementIngestPostRequestBody ingestPostRequestBody = new ServiceAgreementIngestPostRequestBody()
            .withParticipantsToIngest(participants);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> userManagementService
                .getUsersGroupedByExternalId(ingestPostRequestBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_082.getErrorMessage(), ERR_AG_082.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidUsers() {
        Set<ParticipantIngest> participants = getParticipants(
            getParticipant("2", asList("u2_1", "u2_2"), true, true, asList("u2_1", null))
        );
        ServiceAgreementIngestPostRequestBody ingestPostRequestBody = new ServiceAgreementIngestPostRequestBody()
            .withParticipantsToIngest(participants);
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> userManagementService
                .getUsersGroupedByExternalId(ingestPostRequestBody));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_082.getErrorMessage(), ERR_AG_082.getErrorCode())));
    }

    @Test
    public void shouldReturnEmptyUsersListWhenAdminsAndNoUsersAreProvidedInTheServiceAgreement() {
        Map<String, GetUser> usersGroupedByExternalId = userManagementService
            .getUsersGroupedByExternalId(new ServiceAgreementIngestPostRequestBody()
                .withParticipantsToIngest(getParticipants(
                    getParticipant("1", new ArrayList<>(), false, true, new ArrayList<>()),
                    getParticipant("2", new ArrayList<>(), true, true, new ArrayList<>()),
                    getParticipant("3", new ArrayList<>(), true, false, new ArrayList<>())
                )));

        assertTrue(usersGroupedByExternalId.isEmpty());
    }

    @Test
    public void shouldReturnAdminAndAssignedUsers() {
        Map<String, GetUser> usersMap = new HashMap<>();
        usersMap.put("u1_1", getUsers("u1_1", null, null));
        usersMap.put("u1_2", getUsers("u1_2", null, null));
        usersMap.put("u2_1", getUsers("u2_1", null, null));
        usersMap.put("u2_2", getUsers("u2_2", null, null));
        usersMap.put("u3_1", getUsers("u3_1", null, null));
        usersMap.put("u3_2", getUsers("u3_2", null, null));
        usersMap.put("exposed_1", getUsers("exposed_1", null, null));
        usersMap.put("exposed_2", getUsers("exposed_2", null, null));
        ArrayList<GetUser> data = new ArrayList<>(usersMap.values());

        when(
            userManagementApi.getUsersByExternalIds(
                anyList()))
            .thenAnswer(ans -> data);

        Map<String, GetUser> usersGroupedByExternalId = userManagementService
            .getUsersGroupedByExternalId(new ServiceAgreementIngestPostRequestBody()
                .withParticipantsToIngest(getParticipants(
                    getParticipant("1", asList("U1_1", "u1_2"), false, true, new ArrayList<>()),
                    getParticipant("2", asList("u2_1", "u2_2"), false, true, new ArrayList<>()),
                    getParticipant("3", asList("u3_1", "u3_2"), true, false, asList("exposed_1", "exposed_2", "u3_1"))
                )));
        assertEquals(usersGroupedByExternalId, usersMap);
    }

    @Test
    public void testGetUnexposedUsers() {
        int from = 0;
        int size = 5;
        String participant2 = "LE-02";
        String participant1 = "LE-01";
        String user1 = "U-01";
        String user2 = "U-02";
        Set<String> participantsSharingUsers = new HashSet<>(asList(participant1, participant1));
        Set<String> exposedUserIds = new HashSet<>(asList("U-04", "U-05"));
        GetUser user11 = new GetUser();
        user11.setId(user1);
        user11.setLegalEntityId(participant1);
        GetUser user12 = new GetUser();
        user12.setId(user2);
        user12.setLegalEntityId(participant2);
        List<GetUser> users = asList(
            user11,
            user12
        );
        com.backbase.dbs.user.api.client.v2.model.GetUsersList data = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        data.setTotalElements(2L);
        data.setUsers(users);

        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest usersByLegalEntityIdsRequest =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest();
        usersByLegalEntityIdsRequest.setSortOrder(null);
        usersByLegalEntityIdsRequest.setCursor(null);
        usersByLegalEntityIdsRequest.setQuery(null);
        usersByLegalEntityIdsRequest.setSize(size);
        usersByLegalEntityIdsRequest.setFrom(from);
        usersByLegalEntityIdsRequest.setLegalEntityIds(Lists.newArrayList(participantsSharingUsers));
        usersByLegalEntityIdsRequest.setExcludeIds(Lists.newArrayList(exposedUserIds));

        when(
            userManagementApi.getUsersByLegalEntityIds(usersByLegalEntityIdsRequest, true))
            .thenAnswer(ans -> data);

        com.backbase.dbs.user.api.client.v2.model.GetUsersList responseExposedUsers = userManagementService
            .getUnexposedUsers(participantsSharingUsers, exposedUserIds, from, size, null, null, null);

        assertEquals(2, responseExposedUsers.getTotalElements().intValue());
        assertTrue(responseExposedUsers.getUsers().containsAll(users));
    }

    @Test
    public void testGetUsersForServiceAgreement() {
        Integer from = 1;
        Integer size = 2;
        List<String> users = asList("U1", "U2");
        GetUser user1 = new GetUser();
        user1.setId("U1");
        GetUser user2 = new GetUser();
        user2.setId("U2");
        String userIds = String.join(",", users);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList data =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        data.setTotalElements(2L);
        data.setUsers(Lists.newArrayList(user1, user2));

        when(
            userManagementApi.getUsersInBulk(eq(userIds), isNull(), eq(from), isNull(), eq(size)))
            .thenAnswer(ans -> data);

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> response = userManagementService
            .getUsersForServiceAgreement(new HashSet<>(users), null, from, size, null);

        List<String> resultUsers = response.getRecords().stream()
            .map(ServiceAgreementUsersGetResponseBody::getId)
            .collect(Collectors.toList());

        verify(userManagementApi).getUsersInBulk(eq(userIds), isNull(), eq(from), isNull(), eq(size));
        assertEquals(users.size(), resultUsers.size());
        assertTrue(resultUsers.containsAll(users));
    }


    @Test
    public void shouldCallGetUsersByExternalIds() {
        String externalUserId = "U-01";
        List<String> userExternalIds = asList(externalUserId, "U-02", "U-03");
        GetUser user1 = new GetUser();
        user1.setId("U1");
        GetUser user2 = new GetUser();
        user2.setId("U2");
        GetUser user3 = new GetUser();
        user2.setId(externalUserId);
        List<GetUser> data = new ArrayList<>();
        data.add(user1);
        data.add(user2);
        data.add(user3);

        when(
            userManagementApi.getUsersByExternalIdsBulk(userExternalIds))
            .thenAnswer(ans -> data);
        List<GetUser> response = userManagementService
            .getUsersByExternalIds(userExternalIds);

        verify(userManagementApi).getUsersByExternalIdsBulk(userExternalIds);

        assertEquals(userExternalIds.size(), response.size());
        assertEquals(data, response);
    }

    @Test
    public void testGetUsersForCreatorLegalEntity() {
        GetUser user = new GetUser();
        user.setFullName("name");
        user.setId("id");
        user.setLegalEntityId("leId");
        List<GetUser> users = new ArrayList<>();
        users.add(user);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new GetUsersList();
        list.setUsers(users);
        com.backbase.dbs.user.api.client.v2.model.GetUsersByLegalEntityIdsRequest item =
            new GetUsersByLegalEntityIdsRequest();
        item.setSize(null);
        item.setFrom(null);
        item.setLegalEntityIds(Lists.newArrayList("leId"));
        when(
            userManagementApi.getUsersByLegalEntityIds(refEq(item), eq(true)))
            .thenAnswer(ans -> list);

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> response = userManagementService
            .getUsersForCreatorLegalEntity(item);

        assertEquals(user.getExternalId(), response.getRecords().get(0).getExternalId());
        assertEquals(user.getLegalEntityId(), response.getRecords().get(0).getLegalEntityId());
    }

    private GetUser getUsers(String externalId, String legalEntityId, String id) {
        GetUser getUser = new GetUser();
        getUser.setExternalId(externalId);
        getUser.setId(id);
        getUser.setLegalEntityId(legalEntityId);
        return getUser;
    }
}