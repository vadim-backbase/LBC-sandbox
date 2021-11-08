package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_033;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetUnexposedUsersForServiceAgreementTest {

    private static final String SERVICE_AGREEMENT_ID = "SA01";

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @InjectMocks
    private GetUnexposedUsers getUnexposedUsers;


    @Test
    public void shouldPassIfGetUnexposedUsersForServiceAgreementIsInvoked() {
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;
        String exposedUser1 = "U1";
        String exposedUser2 = "U2";
        String participantSharingUserId = "participant1";
        String participantSharingAccountsId = "participant2";
        String participantSharingUserName = "participantName1";
        String participantSharingAccountsName = "participantName2";
        HashSet<String> userIds = new HashSet<>(asList(exposedUser1, exposedUser2));

        UnexposedUsersGetResponseBody unexposedUser1 = getUnexposedUsersGetResponseBody(participantSharingUserId,
            participantSharingUserName, "U3");
        UnexposedUsersGetResponseBody unexposedUser2 = getUnexposedUsersGetResponseBody(participantSharingUserId,
            participantSharingUserName, "U4");
        Participant participantSharingUsers = createParticipant(participantSharingUserId, participantSharingUserName,
            true, false);
        Participant participantSharingAccounts = createParticipant(participantSharingAccountsId,
            participantSharingAccountsName, false, true);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList unexposedUsersBodies = getUsersByLegalEntityIdsPostResponseBody(
            totalNumberOfRecords, unexposedUser1, unexposedUser2);

        ServiceAgreementItem serviceAgreementItem = new ServiceAgreementItem()
            .withId(SERVICE_AGREEMENT_ID)
            .withIsMaster(false);

        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(asList(participantSharingUsers, participantSharingAccounts, participantSharingAccounts));
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);
        when(persistenceServiceAgreementService
            .getServiceAgreementUsers(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(new ServiceAgreementUsersGetResponseBody().withUserIds(userIds));
        mockGetUnexposedUsers(from, size, userIds, new HashSet<>(Collections.singletonList(participantSharingUserId)),
            unexposedUsersBodies);

        InternalRequest<PaginationDto<UnexposedUsersGetResponseBody>> businessProcessResult = getUnexposedUsers
            .getUnexposedUsers(
                SERVICE_AGREEMENT_ID, from, size, null, null);

        assertEquals(asList(unexposedUser1, unexposedUser2), businessProcessResult.getData().getRecords());
    }

    @Test
    public void shouldThrowBadRequestWhenNoParticipantsSharingUsers() {
        ServiceAgreementItem serviceAgreementItem = new ServiceAgreementItem()
            .withId(SERVICE_AGREEMENT_ID)
            .withIsMaster(false);

        mockGetParticipants(new ArrayList<>());
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> getUnexposedUsers.getUnexposedUsers(SERVICE_AGREEMENT_ID, null, null, null, null));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_033.getErrorMessage(), ERR_AG_033.getErrorCode())));
    }

    @Test
    public void shouldReturnEmptyListWhenGetUnexposedUsersForMSAIsInvoked() {
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;
        String exposedUser1 = "U1";
        String exposedUser2 = "U2";
        String participantSharingUserId = "participant1";
        String participantSharingUserName = "participantName1";
        HashSet<String> userIds = new HashSet<>(asList(exposedUser1, exposedUser2));

        UnexposedUsersGetResponseBody unexposedUser1 = getUnexposedUsersGetResponseBody(participantSharingUserId,
            participantSharingUserName, "U3");
        UnexposedUsersGetResponseBody unexposedUser2 = getUnexposedUsersGetResponseBody(participantSharingUserId,
            participantSharingUserName, "U4");
        GetUsersList unexposedUsersBodies = getUsersByLegalEntityIdsPostResponseBody(
            totalNumberOfRecords, unexposedUser1, unexposedUser2);

        ServiceAgreementItem serviceAgreementItem = new ServiceAgreementItem()
            .withId(SERVICE_AGREEMENT_ID)
            .withIsMaster(true);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(serviceAgreementItem);
        mockGetUnexposedUsers(from, size, userIds, new HashSet<>(Collections.singletonList(participantSharingUserId)),
            unexposedUsersBodies);

        InternalRequest<PaginationDto<UnexposedUsersGetResponseBody>> businessProcessResult = getUnexposedUsers
            .getUnexposedUsers(
                SERVICE_AGREEMENT_ID, from, size, null, null);

        assertEquals(0, businessProcessResult.getData().getRecords().size());
    }


    private com.backbase.dbs.user.api.client.v2.model.GetUsersList getUsersByLegalEntityIdsPostResponseBody(
        Long totalNumberOfRecords,
        UnexposedUsersGetResponseBody unexposedUser1, UnexposedUsersGetResponseBody unexposedUser2) {
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new GetUsersList();

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(unexposedUser1.getId());
        user1.setLegalEntityId(unexposedUser1.getLegalEntityId());

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setId(unexposedUser2.getId());
        user2.setLegalEntityId(unexposedUser2.getLegalEntityId());
        list.setUsers(Lists.newArrayList(user1, user2));
        list.setTotalElements(totalNumberOfRecords);
        return list;
    }

    private Participant createParticipant(String participantSharingUserId, String participantSharingUserName,
        Boolean sharingUsers, Boolean sharingAccounts) {
        return new Participant().withId(participantSharingUserId).withName(participantSharingUserName)
            .withSharingUsers(sharingUsers).withSharingAccounts(sharingAccounts);
    }

    private void mockGetUnexposedUsers(Integer from, Integer size, HashSet<String> userIds,
        HashSet<String> participantsSharingUsersIds, GetUsersList unexposedUsersBodies) {
        when(userManagementService
            .getUnexposedUsers(eq(participantsSharingUsersIds), eq(userIds), eq(from), eq(size), eq(null), eq(null),
                eq("legalEntityId,fullName")))
            .thenReturn(unexposedUsersBodies);
    }

    private void mockGetParticipants(List<Participant> participants) {
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(participants);
    }

    private UnexposedUsersGetResponseBody getUnexposedUsersGetResponseBody(String participantSharingUserId,
        String participantSharingUserName, String userId) {
        return new UnexposedUsersGetResponseBody()
            .withId(userId)
            .withLegalEntityId(participantSharingUserId)
            .withLegalEntityName(participantSharingUserName);
    }
}
