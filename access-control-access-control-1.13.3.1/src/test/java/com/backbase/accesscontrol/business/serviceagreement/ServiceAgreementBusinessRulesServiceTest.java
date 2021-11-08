package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_108;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_109;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_110;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.ServiceAgreementParticipantDto;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementBusinessRulesServiceTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ApplicationProperties applicationProperties;

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    @Test
    public void shouldNotBeServiceAgreementInPendingWhenApprovalIsOff() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        assertFalse(serviceAgreementBusinessRulesService.isServiceAgreementInPendingState("test"));
        verify(persistenceServiceAgreementService, times(0)).isServiceAgreementInPendingState(anyString());
    }

    @Test
    public void shouldCheckServiceAgreementInPendingWhenApprovalIsOn() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        serviceAgreementBusinessRulesService.isServiceAgreementInPendingState("test");
        verify(persistenceServiceAgreementService).isServiceAgreementInPendingState(eq("test"));
    }

    @Test
    public void shouldNotBeServiceAgreementByExternalIdInPendingWhenApprovalIsOff() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        assertFalse(serviceAgreementBusinessRulesService.isServiceAgreementInPendingStateByExternalId("test"));
        verify(persistenceServiceAgreementService, times(0))
            .isServiceAgreementInPendingStateByExternalId(anyString());
    }

    @Test
    public void shouldCheckServiceAgreementByExternalIdInPendingWhenApprovalIsOn() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        serviceAgreementBusinessRulesService.isServiceAgreementInPendingStateByExternalId("test");
        verify(persistenceServiceAgreementService).isServiceAgreementInPendingStateByExternalId(eq("test"));
    }

    @Test
    public void shouldNotExistServiceAgreementWithExternalIdInPendingWhenApprovalIsOff() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        assertFalse(serviceAgreementBusinessRulesService.existsPendingServiceAgreementWithExternalId("test"));
        verify(persistenceServiceAgreementService, times(0)).existsPendingServiceAgreementWithExternalId(anyString());
    }

    @Test
    public void shouldCheckForServiceAgreementWithExternalIdInPendingWhenApprovalIsOn() {
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        serviceAgreementBusinessRulesService.existsPendingServiceAgreementWithExternalId("test");
        verify(persistenceServiceAgreementService).existsPendingServiceAgreementWithExternalId(eq("test"));
    }

    @Test
    public void shouldReturnFalseWhenParticipantIsValid() {
        boolean validParticipant = serviceAgreementBusinessRulesService.isInvalidParticipant(Collections
            .singletonList(new ServiceAgreementParticipantDto().withSharingAccounts(true).withSharingUsers(false)));

        assertFalse(validParticipant);
    }

    @Test
    public void shouldReturnTrueWhenParticipantIsInvalid() {
        boolean invalidParticipant = serviceAgreementBusinessRulesService.isInvalidParticipant(Collections
            .singletonList(new ServiceAgreementParticipantDto().withSharingAccounts(false).withSharingUsers(false)));

        assertTrue(invalidParticipant);
    }

    @Test
    public void shouldReturnFalseWhenParticipantSharingUsersExists() {
        boolean validParticipant = serviceAgreementBusinessRulesService.participantSharingUsersNotExists(
            asList(new ServiceAgreementParticipantDto().withSharingAccounts(false).withSharingUsers(true),
                new ServiceAgreementParticipantDto().withSharingAccounts(true).withSharingUsers(false)));

        assertFalse(validParticipant);
    }

    @Test
    public void shouldReturnTrueWhenParticipantSharingUsersNotExists() {
        boolean invalidParticipant = serviceAgreementBusinessRulesService.participantSharingUsersNotExists(Collections
            .singletonList(new ServiceAgreementParticipantDto().withSharingAccounts(false).withSharingUsers(false)));

        assertTrue(invalidParticipant);
    }

    @Test
    public void shouldReturnFalseWhenParticipantSharingAccountsExists() {
        boolean validParticipant = serviceAgreementBusinessRulesService.participantSharingAccountsNotExists(
            asList(new ServiceAgreementParticipantDto().withSharingAccounts(true).withSharingUsers(false),
                new ServiceAgreementParticipantDto().withSharingAccounts(false).withSharingUsers(true)));

        assertFalse(validParticipant);
    }

    @Test
    public void shouldReturnTrueWhenParticipantSharingAccountsNotExists() {
        boolean invalidParticipant = serviceAgreementBusinessRulesService.participantSharingAccountsNotExists(
            Collections.singletonList(
                new ServiceAgreementParticipantDto().withSharingAccounts(false).withSharingUsers(false)));

        assertTrue(invalidParticipant);
    }

    @Test
    public void shouldReturnFalseWhenAdminUsersBelongToTheGivenLegalEntities() {
        String legalEntity1 = "LE-01";
        HashSet<String> admins1 = new HashSet<>(Collections.singletonList("U-01"));
        String legalEntity2 = "LE-02";
        HashSet<String> admins2 = new HashSet<>(Collections.singletonList("U-02"));

        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put(legalEntity1, admins1);
        adminsToAssign.put(legalEntity2, admins2);

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(legalEntity1, admins1);
        usersByLegalEntity.put(legalEntity2, admins2);

        boolean response = serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(usersByLegalEntity, adminsToAssign);

        assertFalse(response);
    }

    @Test
    public void shouldReturnFalseWhenAdminListIsEmpty() {
        String legalEntity1 = "LE-01";
        HashSet<String> admins1 = new HashSet<>(Collections.singletonList("U-01"));
        String legalEntity2 = "LE-02";
        String legalEntity3 = "LE-03";

        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put(legalEntity1, admins1);
        adminsToAssign.put(legalEntity2, new HashSet<>());

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(legalEntity1, admins1);
        usersByLegalEntity.put(legalEntity3, new HashSet<>(Collections.singletonList("U-03")));

        boolean response = serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(usersByLegalEntity, adminsToAssign);

        assertFalse(response);
    }


    @Test
    public void shouldReturnTrueWhenAdminUsersNotBelongToTheGivenLegalEntities() {
        String legalEntity1 = "LE-01";
        HashSet<String> admins1 = new HashSet<>(Collections.singletonList("U-01"));
        String legalEntity2 = "LE-02";
        HashSet<String> admins2 = new HashSet<>(Collections.singletonList("SOME OTHER USER"));

        HashMap<String, Set<String>> adminsToAssign = new HashMap<>();
        adminsToAssign.put(legalEntity1, admins1);
        adminsToAssign.put(legalEntity2, admins2);

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(legalEntity1, admins1);
        usersByLegalEntity.put(legalEntity2, new HashSet<>(Collections.singletonList("U-02")));
        boolean response = serviceAgreementBusinessRulesService
            .adminUsersNotBelongToTheGivenLegalEntities(usersByLegalEntity, adminsToAssign);

        assertTrue(response);
    }

    @Test
    public void shouldReturnFalseIfServiceAgreementIsNotMaster() {
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withIsMaster(false);
        boolean response = serviceAgreementBusinessRulesService
            .isServiceAgreementRootMasterServiceAgreement(serviceAgreement);

        assertFalse(response);
    }

    @Test
    public void shouldReturnFalseIfServiceAgreementIsMasterAndNotUnderRootLegalEntity() {
        String creatorLegalEntity = "LE-creator";
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withIsMaster(true)
            .withCreatorLegalEntity(creatorLegalEntity);

        when(persistenceLegalEntityService.getLegalEntityById(eq(creatorLegalEntity)))
            .thenReturn(new LegalEntity().withParent(new LegalEntity().withId("LE-parent")));
        boolean response = serviceAgreementBusinessRulesService
            .isServiceAgreementRootMasterServiceAgreement(serviceAgreement);

        assertFalse(response);
    }

    @Test
    public void shouldReturnTrueIfServiceAgreementIsMasterAndIsUnderRootLegalEntity() {
        String creatorLegalEntity = "LE-creator";
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withIsMaster(true)
            .withCreatorLegalEntity(creatorLegalEntity);

        when(persistenceLegalEntityService.getLegalEntityById(eq(creatorLegalEntity)))
            .thenReturn(new LegalEntity());
        boolean response = serviceAgreementBusinessRulesService
            .isServiceAgreementRootMasterServiceAgreement(serviceAgreement);

        assertTrue(response);
    }

    @Test
    public void serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNullShouldReturnFalse() {
        ServiceAgreementPutRequestBody putData = new ServiceAgreementPutRequestBody()
            .withExternalId("exId");
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId("SA-01");
        ServiceAgreement serviceAgreementByExternal = new ServiceAgreement()
            .withExternalId("exId")
            .withId("SA-01");

        mockGetServiceAgreementByExternalId(putData, serviceAgreementByExternal);
        boolean response = serviceAgreementBusinessRulesService
            .serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNull(putData, serviceAgreement);

        assertFalse(response);
    }

    @Test
    public void serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNullShouldReturnTrue() {
        ServiceAgreementPutRequestBody putData = new ServiceAgreementPutRequestBody()
            .withExternalId("exId");
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId("SA-01");
        ServiceAgreement serviceAgreementByExternal = new ServiceAgreement()
            .withExternalId("exId")
            .withId("SA-02");

        mockGetServiceAgreementByExternalId(putData, serviceAgreementByExternal);
        boolean response = serviceAgreementBusinessRulesService
            .serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNull(putData, serviceAgreement);

        assertTrue(response);
    }

    @Test
    public void shouldReturnFalseWhenNoDuplicateParticipant() {
        String participantId1 = "LE-01";
        String participantId2 = "LE-02";

        boolean duplicateParticipant = serviceAgreementBusinessRulesService
            .isDuplicateParticipant(asList(participantId1, participantId2));

        assertFalse(duplicateParticipant);
    }

    @Test
    public void shouldReturnTrueWhenThereIsDuplicateParticipantInTheList() {
        String participantId1 = "LE-01";
        String participantId2 = "LE-01";

        List<String> serviceAgreementParticipants = asList(participantId1, participantId2);
        boolean duplicateParticipant = serviceAgreementBusinessRulesService
            .isDuplicateParticipant(serviceAgreementParticipants);

        assertTrue(duplicateParticipant);
    }

    @Test
    public void shouldReturnFalseIfAdminsToBeUpatedBelongToTheParticipants() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String user1Participant1 = "U-01";
        String user2Participant1 = "U-02";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant2 = "U-03";
        String user2Participant2 = "U-04";
        Set<String> adminsForParticipant1 = Sets.newHashSet(user1Participant1, user2Participant1);
        Set<String> adminsForParticipant2 = Sets.newHashSet(user1Participant2, user2Participant2);

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant1);
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant2);
        List<Participant> participants = asList(participant1, participant2);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> adminsToBeUpdated = asList(
            createUser(participantId1, user1Participant1),
            createUser(participantId1, user2Participant1),
            createUser(participantId2, user1Participant2),
            createUser(participantId2, user2Participant2));

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(participantId1, adminsForParticipant1);
        usersByLegalEntity.put(participantId2, adminsForParticipant2);
        mockGetUsersByLegalEntity(adminsToBeUpdated, usersByLegalEntity);

        assertFalse(serviceAgreementBusinessRulesService
            .adminsDoNotBelongToParticipantsLegalEntities(adminsToBeUpdated, participants));
    }

    @Test
    public void shouldReturnTrueIfAdminsToBeUpdatedDoNotBelongToTheParticipants() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String user1Participant1 = "U-01";
        String user2Participant1 = "U-02";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant2 = "U-03";
        String user2Participant2 = "U-04";
        Set<String> adminsForParticipant1 = Sets.newHashSet(user1Participant1, user2Participant1);
        Set<String> adminsForParticipant2 = Sets.newHashSet(user1Participant2, user2Participant2);

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant1);
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant2);
        List<Participant> participants = asList(participant1, participant2);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> adminsToBeUpdated = asList(
            createUser(participantId1, user1Participant1),
            createUser(participantId1, user2Participant1),
            createUser(participantId2, user1Participant2),
            createUser("LE-03", user2Participant2));

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(participantId1, adminsForParticipant1);
        usersByLegalEntity.put(participantId2, new HashSet<>(Collections.singletonList(user1Participant2)));
        usersByLegalEntity.put("LE-03", new HashSet<>(Collections.singletonList(user2Participant2)));
        mockGetUsersByLegalEntity(adminsToBeUpdated, usersByLegalEntity);

        assertTrue(serviceAgreementBusinessRulesService
            .adminsDoNotBelongToParticipantsLegalEntities(adminsToBeUpdated, participants));
    }

    @Test
    public void shouldReturnFalseIfAdminsAreNotProvidedForAllParticipantsButTheProvidedAreValid() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant2 = "U-03";
        String user2Participant2 = "U-04";
        Set<String> admins = Sets.newHashSet(user1Participant2, user2Participant2);

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(new HashSet<>());
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(admins);
        List<Participant> participants = asList(participant1, participant2);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> adminsToBeUpdated = asList(
            createUser(participantId2, user1Participant2),
            createUser(participantId2, user2Participant2));

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(participantId2, admins);
        mockGetUsersByLegalEntity(adminsToBeUpdated, usersByLegalEntity);

        assertFalse(serviceAgreementBusinessRulesService
            .adminsDoNotBelongToParticipantsLegalEntities(adminsToBeUpdated, participants));
    }

    @Test
    public void shouldReturnFalseIfThereAreNoAdminsForEveryParticipant() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant3 = "U-07";
        String participantId3 = "Not Valid";
        Set<String> adminsForParticipant3 = Sets.newHashSet(user1Participant3);

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true);
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true);
        Participant participant3 = new Participant()
            .withId(participantId3)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant3);
        List<Participant> participants = asList(participant1, participant2, participant3);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> adminsToBeUpdated = Collections.singletonList(
            createUser(participantId3, user1Participant3));

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(participantId3, adminsForParticipant3);
        mockGetUsersByLegalEntity(adminsToBeUpdated, usersByLegalEntity);

        assertFalse(serviceAgreementBusinessRulesService
            .adminsDoNotBelongToParticipantsLegalEntities(adminsToBeUpdated, participants));
    }

    @Test
    public void shouldReturnTrueIfAdminsBelongToAnotherLegalEntity() {
        String participantId1 = "f9400fb7a3b3ca4cafb8136b06baccfe";
        String participantId2 = "f9400fb7a3b3ca4cafb8136b06baccfa";
        String user1Participant3 = "U-07";
        String participantId3 = "f9400fb7a3b3ca4cafb8136b06bacabc";
        String user1Participant1 = "U-01";
        String user2Participant1 = "U-02";
        String user1Participant2 = "U-03";
        String user2Participant2 = "U-04";
        Set<String> adminsForParticipant1 = Sets.newHashSet(user1Participant1, user2Participant1);
        Set<String> adminsForParticipant2 = Sets.newHashSet(user1Participant2, user2Participant2);
        Set<String> adminsForParticipant3 = Sets.newHashSet(user1Participant3);

        Participant participant1 = new Participant()
            .withId(participantId1)
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant1);
        Participant participant2 = new Participant()
            .withId(participantId2)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant2);
        Participant participant3 = new Participant()
            .withId(participantId3)
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(adminsForParticipant3);
        List<Participant> participants = asList(participant1, participant2, participant3);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> adminsToBeUpdated = asList(
            createUser(participantId1, user1Participant1),
            createUser(participantId1, user2Participant1),
            createUser(participantId2, user2Participant2),
            createUser(participantId2, user2Participant2),
            createUser("LE-05", user1Participant3));

        HashMap<String, Set<String>> usersByLegalEntity = new HashMap<>();
        usersByLegalEntity.put(participantId1, adminsForParticipant1);
        usersByLegalEntity.put(participantId2, adminsForParticipant2);
        usersByLegalEntity.put("LE-05", adminsForParticipant3);
        mockGetUsersByLegalEntity(adminsToBeUpdated, usersByLegalEntity);

        assertTrue(serviceAgreementBusinessRulesService
            .adminsDoNotBelongToParticipantsLegalEntities(adminsToBeUpdated, participants));
    }

    @Test
    public void shouldReturnTrueForBothNull() {
        assertTrue(serviceAgreementBusinessRulesService.isPeriodValid(null, null));
    }

    @Test
    public void shouldReturnTrueForFromBeforeUntil() {
        assertTrue(serviceAgreementBusinessRulesService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000)));
    }

    @Test
    public void shouldReturnTrueForUntilIsNull() {
        assertTrue(serviceAgreementBusinessRulesService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            null));
    }

    @Test
    public void shouldReturnTrueForFromIsNull() {
        assertTrue(serviceAgreementBusinessRulesService.isPeriodValid(
            null,
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000)));
    }

    @Test
    public void shouldReturnFalseUntilIsBeforeFrom() {
        assertFalse(serviceAgreementBusinessRulesService.isPeriodValid(
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000),
            new Date(System.currentTimeMillis() + 3600 * 1000)));
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsAJobRolePending() {
        String serviceAgreementId = "saId";
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingJobRoleInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId));
        assertEquals(ERR_AG_108.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsADataGroupPending() {
        String serviceAgreementId = "saId";
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingDataGroupInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId));
        assertEquals(ERR_AG_109.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsAUserPermissionPending() {
        String serviceAgreementId = "saId";
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingPermissionsInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId));
        assertEquals(ERR_AG_110.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsAJobRolePendingExternalSaId() {
        String serviceAgreementId = "saId";
        String serviceAgreementExternalId = "exSaId";
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withExternalId(serviceAgreementExternalId);
        when(serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId)).thenReturn(Optional.of(serviceAgreement));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingJobRoleInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService
                .checkPendingValidationsInServiceAgreementExternalServiceAgreementId(serviceAgreementExternalId));
        assertEquals(ERR_AG_108.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsADataGroupPendingExternalSaId() {
        String serviceAgreementId = "saId";
        String serviceAgreementExternalId = "exSaId";
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withExternalId(serviceAgreementExternalId);
        when(serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId)).thenReturn(Optional.of(serviceAgreement));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingDataGroupInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService
                .checkPendingValidationsInServiceAgreementExternalServiceAgreementId(serviceAgreementExternalId));
        assertEquals(ERR_AG_109.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsAUserPermissionPendingExternalSaId() {
        String serviceAgreementId = "saId";
        String serviceAgreementExternalId = "exSaId";
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withExternalId(serviceAgreementExternalId);
        when(serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId)).thenReturn(Optional.of(serviceAgreement));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingPermissionsInServiceAgreement(serviceAgreementId)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService
                .checkPendingValidationsInServiceAgreementExternalServiceAgreementId(serviceAgreementExternalId));
        assertEquals(ERR_AG_110.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldTestPendingDeleteOfFunctionOrDataGroupInServiceAgreementExternalId() {
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withExternalId("serviceAgreementExternalId")
            .withId("id");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(serviceAgreementJpaRepository.findByExternalId("serviceAgreementExternalId"))
            .thenReturn(Optional.of(serviceAgreement));
        serviceAgreementBusinessRulesService
            .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreementExternalId(serviceAgreement.getExternalId());
        verify(serviceAgreementJpaRepository, times(1))
            .findByExternalId(eq(serviceAgreement.getExternalId()));
    }

    @Test
    public void shouldThrowBadRequestWhenPendingDeleteOfDataGroupInServiceAgreement() {
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withExternalId("serviceAgreementExternalId")
            .withId("id");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingDeleteDataGroupInServiceAgreement(serviceAgreement.getId())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService
                .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreement.getId()));
        assertEquals(ERR_AG_109.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowBadRequestWhenPendingDeleteOfFunctionInServiceAgreement() {
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withExternalId("serviceAgreementExternalId")
            .withId("id");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(persistenceServiceAgreementService
            .existsPendingDeleteJobRoleInServiceAgreement(serviceAgreement.getId())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementBusinessRulesService
                .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreement.getId()));
        assertEquals(ERR_AG_108.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    private void mockGetServiceAgreementByExternalId(ServiceAgreementPutRequestBody putData,
        ServiceAgreement serviceAgreementByExternal) {
        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(eq(putData.getExternalId())))
            .thenReturn(Optional.of(serviceAgreementByExternal));
    }

    private com.backbase.dbs.user.api.client.v2.model.GetUser createUser(String participantId1,
        String user1Participant1) {

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(user1Participant1);
        user1.setLegalEntityId(participantId1);
        return user1;
    }

    private void mockGetUsersByLegalEntity(List<com.backbase.dbs.user.api.client.v2.model.GetUser> users,
        HashMap<String, Set<String>> usersByLegalEntity) {
        when(userManagementService.getUsersByLegalEntityId(users))
            .thenReturn(usersByLegalEntity);
    }
}