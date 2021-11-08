package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementClientCommunicationServiceTest {

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private MasterServiceAgreementFallbackProperties fallbackProperties;

    @InjectMocks
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;

    @Test
    public void testGetUsers() {
        String adminId = "1";
        Set<String> admins = newHashSet(adminId);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new GetUsersList();
        when(userManagementService.getUsers(adminId)).thenReturn(list);
        Optional<GetUsersList> users = serviceAgreementClientCommunicationService.getUsers(admins);
        assertTrue(users.isPresent());
        assertNotNull(users.get());
        assertEquals(list, users.get());
    }

    @Test
    public void testGetEmptyUsers() {
        Set<String> admins = emptySet();
        Optional<GetUsersList> users = serviceAgreementClientCommunicationService.getUsers(admins);
        assertTrue(users.isPresent());
        assertEquals(0, users.get().getUsers().size());
        verify(userManagementService, times(0)).getUsers(any(String.class));
    }

    @Test
    public void testGetServiceAgreementIdForUserWithUserId() {
        String serviceAgreementId = "001";
        String userId = "U-01";

        mockUserByInternalId(userId);

        String serviceAgreementIdForUserWithUserId = serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(userId, serviceAgreementId);
        assertEquals(serviceAgreementId, serviceAgreementIdForUserWithUserId);
    }

    @Test
    public void testGetServiceAgreementIdForUserWithUserIdWithMasterServiceAgreement() {
        String serviceAgreementId = "001";
        String userId = "U-01";

        mockUserByInternalId(userId);
        when(fallbackProperties.isEnabled()).thenReturn(true);
        when(persistenceLegalEntityService.getMasterServiceAgreement(anyString()))
            .thenReturn(new ServiceAgreement().withId(serviceAgreementId));

        String serviceAgreementIdForUserWithUserId = serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(userId, null);
        assertEquals(serviceAgreementId, serviceAgreementIdForUserWithUserId);
    }

    @Test
    public void testGetServiceAgreementIdShouldThrowForbiddenExceptionWhenSAIsNullAndFallbackDisabled() {
        String userId = "U-01";

        mockUserByInternalId(userId);
        when(fallbackProperties.isEnabled()).thenReturn(false);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> serviceAgreementClientCommunicationService
                .getServiceAgreementIdForUserWithUserId(userId, null));
        assertThat(forbiddenException,
            new ForbiddenErrorMatcher(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode()));
    }

    @Test
    public void shouldReturnListOfAdminsToBeUpdated() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String user1Participant1 = "U-01";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant2 = "U-03";
        String user2Participant2 = "U-04";

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet(user1Participant1));
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet(user1Participant2, user2Participant2));
        List<Participant> participants = asList(participant1, participant2);

        GetUsersList adminsToBeUpdated = new GetUsersList();
        adminsToBeUpdated.setUsers(asList(
            createUser(participantId1, user1Participant1),
            createUser(participantId2, user1Participant2),
            createUser(participantId2, user2Participant2)));

        String adminsString = String.join(",",
            asList(user1Participant1, user1Participant2, user2Participant2));
        mockGetUsers(adminsToBeUpdated, adminsString);

        GetUsersList response = serviceAgreementClientCommunicationService.getAdminsToBeUpdated(participants);

        assertEquals(adminsToBeUpdated, response);
    }

    @Test
    public void shouldReturnEmptyListWhenNoAdminsAreProvided() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(new HashSet<>());
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(new HashSet<>());
        List<Participant> participants = asList(participant1, participant2);

        GetUsersList response = serviceAgreementClientCommunicationService.getAdminsToBeUpdated(participants);

        assertTrue(response.getUsers().isEmpty());
    }

    private void mockGetUsers(GetUsersList adminsToBeUpdated, String adminsString) {
        when(userManagementService.getUsers(adminsString))
            .thenReturn(adminsToBeUpdated);
    }

    private void mockUserByInternalId(String userId) {

        GetUser user = createUser("1", "1");
        user.setExternalId("1");
        user.setFullName("FullName");
        when(userManagementService.getUserByInternalId(userId))
            .thenReturn(user);
    }

    private com.backbase.dbs.user.api.client.v2.model.GetUser createUser(String participantId1,
        String user1Participant1) {
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(user1Participant1);
        user1.setLegalEntityId(participantId1);
        return user1;
    }
}
