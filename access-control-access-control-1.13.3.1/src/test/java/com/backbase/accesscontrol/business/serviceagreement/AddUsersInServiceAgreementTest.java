package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_079;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.AddUsersInServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddUsersInServiceAgreementTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private AddUsersInServiceAgreementHandler addUsersInServiceAgreementHandler;

    @InjectMocks
    private AddUsersInServiceAgreement addUsersInServiceAgreement;

    @Test
    public void shouldAddUsersInServiceAgreement() {
        String userId1 = "U-01";
        String userId2 = "U-02";
        List<String> usersToAdd = asList(userId1, userId2);
        String serviceAgreementId = "SA-01";
        String participantLegalEntity = "LE-01";
        PresentationUsersForServiceAgreementRequestBody usersPostRequest = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(usersToAdd);

        List<Participant> participants = singletonList(new Participant().withId(participantLegalEntity));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(userId1);
        user.setLegalEntityId(participantLegalEntity);

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId2);
        user1.setLegalEntityId(participantLegalEntity);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> users = asList(
            user,
            user1
        );
        GetUsersList list = new GetUsersList();
        list.setUsers(users);
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participants);
        mockGetUsersByIds(usersToAdd, users);
        mockUsersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(list, participants, false);
        doNothing().when(addUsersInServiceAgreementHandler).handleRequest(any(), any());

        InternalRequest<PresentationUsersForServiceAgreementRequestBody> request = getInternalRequest(usersPostRequest);

        InternalRequest<Void> response = addUsersInServiceAgreement
            .addUserInServiceAgreement(request, serviceAgreementId);

        ArgumentCaptor<SingleParameterHolder<String>> parameterHolderArgumentCaptor = ArgumentCaptor
            .forClass(SingleParameterHolder.class);

        UsersDto userAddPandp = new UsersDto()
            .withLegalEntityId(participantLegalEntity)
            .withUsers(usersToAdd);

        assertNotNull(response);

        verify(addUsersInServiceAgreementHandler)
            .handleRequest(parameterHolderArgumentCaptor.capture(), eq(singletonList(userAddPandp)));

        assertEquals(serviceAgreementId, parameterHolderArgumentCaptor.getValue().getParameter());
    }

    @Test
    public void shouldThrowExceptionWhenNotAllUsersBelongToServiceAgreementsParticipants() {
        List<String> usersToAdd = asList("U-01", "U-02");
        String serviceAgreementId = "SA-01";
        String participantLegalEntity = "LE-01";
        List<Participant> participants = singletonList(new Participant().withId(participantLegalEntity));

        List<GetUser> users= new ArrayList<>();
        GetUsersList list = new GetUsersList();
        list.setUsers(users);
        mockGetUsersByIds(usersToAdd,  users);

        mockUsersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(list, participants, true);
        mockGetServiceAgreementParticipants(serviceAgreementId, participants);

        InternalRequest<PresentationUsersForServiceAgreementRequestBody> request =
            getInternalRequest(
                new PresentationUsersForServiceAgreementRequestBody()
                    .withUsers(usersToAdd));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addUsersInServiceAgreement.addUserInServiceAgreement(request, serviceAgreementId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_079.getErrorMessage(), ERR_AG_079.getErrorCode())));
    }

    private void mockUsersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(GetUsersList usersToAdd,
        List<Participant> participants, boolean usersDoNotBelongToAdminLE) {
        when(
            userManagementService.usersDoNotBelongInLegalEntityParticipantsOnServiceAgreement(usersToAdd, participants))
            .thenReturn(usersDoNotBelongToAdminLE);
    }

    private void mockGetServiceAgreementParticipants(String serviceAgreementId, List<Participant> participants) {
        when(persistenceServiceAgreementService
            .getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(participants);
    }

    private void mockGetUsersByIds(List<String> usersToAdd, List<com.backbase.dbs.user.api.client.v2.model.GetUser> data) {

        com.backbase.dbs.user.api.client.v2.model.GetUsersList usersGetResponseBody= new GetUsersList();
        usersGetResponseBody.setUsers(data);
        when(userManagementService.getUsersByIds(eq(usersToAdd), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(usersGetResponseBody);
    }
}