package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_041;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_026;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_027;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_028;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.TimeBoundValidatorService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserContextFunctionGroupServiceImplTest {

    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private UserContextJpaRepository userContextJpaRepository;
    @Mock
    private UserContextService userContextService;
    @Spy
    private TimeBoundValidatorService timeBoundValidatorService = new TimeBoundValidatorService("UTC");
    @InjectMocks
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    @Captor
    private ArgumentCaptor<UserAssignedFunctionGroup> userAssignedFunctionGroupCaptor;

    @Test
    public void shouldRemoveFunctionGroupFromUserAccessUnderMasterServiceAgreement() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroup(functionGroup);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        mockUserAssignedFunctionGroups(functionGroupId, userId, serviceAgreementId, Optional.of(assignedFunctionGroup));

        userAccessFunctionGroupService.deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId);

        verify(userAssignedFunctionGroupJpaRepository, times(1)).delete(assignedFunctionGroup);
    }

    @Test
    public void shouldRemoveFunctionGroupFromUserAccessUnderCustomServiceAgreementWhenUserIsExposed() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        HashMap<String, Participant> providers = new HashMap<>();
        Participant provider = new Participant();
        provider.setShareUsers(true);
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setUserId(userId);
        provider.setParticipantUsers(new HashSet<>(singletonList(participantUser)));
        providers.put("LE-01", provider);
        serviceAgreement.setParticipants(providers);

        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroup(functionGroup);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        mockUserAssignedFunctionGroups(functionGroupId, userId, serviceAgreementId, Optional.of(assignedFunctionGroup));

        userAccessFunctionGroupService.deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId);

        verify(userAssignedFunctionGroupJpaRepository, times(1)).delete(assignedFunctionGroup);
    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveFunctionGroupWhenFGDoesNotExist() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId("SA-02"));
        functionGroup.setId(functionGroupId);

        mockGetFunctionGroupById(functionGroupId, Optional.empty(), FunctionGroupType.DEFAULT);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveFunctionGroupWhenFGDoesNotBelongToTheServiceAgreement() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId("SA-02"));
        functionGroup.setId(functionGroupId);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_026.getErrorMessage(), ERR_ACQ_026.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveFunctionGroupWhenServiceAgreementDoesNotExist() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);
        mockGetServiceAgreementById(serviceAgreementId, Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveFunctionGroupFromUserAccessUnderCustomServiceAgreementWhenUserIsNotExposed() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        HashMap<String, Participant> providers = new HashMap<>();
        Participant provider = new Participant();
        provider.setShareUsers(true);
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setUserId("U-02");
        provider.setParticipantUsers(new HashSet<>(singletonList(participantUser)));
        providers.put("LE-01", provider);
        serviceAgreement.setParticipants(providers);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_027.getErrorMessage(), ERR_ACQ_027.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionOnRemoveFunctionGroupFromUserAccessUnderMasterServiceAgreementWhenFGisNotAssignedToUser() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        mockGetFunctionGroupById(functionGroupId, Optional.of(functionGroup), FunctionGroupType.DEFAULT);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        mockUserAssignedFunctionGroups(functionGroupId, userId, serviceAgreementId, Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .deleteFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreementId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_028.getErrorMessage(), ERR_ACQ_028.getErrorCode()));
    }

    @Test
    public void shouldAssignSystemFunctionGroupToUserAccess() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afp_id");

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(applicableFunctionPrivilege.getId());
        functionGroup.setPermissions(new HashSet<>(singletonList(groupedFunctionPrivilege)));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.getFunctionGroups().add(functionGroup);

        UserContext userContext = new UserContext(userId, serviceAgreementId);

        userAccessFunctionGroupService.addSystemFunctionGroupToUserAccess(functionGroupId, serviceAgreement,
            userContext);

        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .save(userAssignedFunctionGroupCaptor.capture());

        assertEquals(userContext, userAssignedFunctionGroupCaptor.getValue().getUserContext());
        assertEquals(functionGroup, userAssignedFunctionGroupCaptor.getValue().getFunctionGroup());
    }

    @Test
    public void shouldThrowBadRequestExceptionOnAssignSystemFunctionGroupWhenFunctionGroupDoesNotExist() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);

        UserContext userContext = new UserContext(userId, serviceAgreementId);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .addSystemFunctionGroupToUserAccess(functionGroupId, serviceAgreement, userContext));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionOnAssignSystemFunctionGroupWhenFGDoesNotExist() {
        String functionGroupId = "FG-01";
        String userId = "U-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId("SA-02"));
        functionGroup.setId(functionGroupId);

        UserContext userContext = new UserContext(userId, "");

        mockGetFunctionGroupById(functionGroupId, Optional.empty(), FunctionGroupType.SYSTEM);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .addSystemFunctionGroupToUserAccess(functionGroupId, new ServiceAgreement(), userContext));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldRemoveSystemFunctionGroupFromUserAccess() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setId(functionGroupId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);

        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroup(functionGroup);

        mockUserAssignedFunctionGroups(functionGroupId, userId, serviceAgreementId, Optional.of(assignedFunctionGroup));
        doNothing().when(userContextService).delete(any());
        userAccessFunctionGroupService
            .deleteSystemFunctionGroupFromUserAccess(functionGroupId, userId, serviceAgreement);

        verify(userContextService, times(1)).delete(ArgumentMatchers.any());
    }

    @Test
    public void shouldAddSystemFunctionGroupToUserAccessWithDefaultFG() {
        String functionGroupId = "FG-01";
        String userId = "U-01";
        String serviceAgreementId = "SA-01";

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afp_id");

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setId(functionGroupId);
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(applicableFunctionPrivilege.getId());
        functionGroup.setPermissions(new HashSet<>(singletonList(groupedFunctionPrivilege)));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.getFunctionGroups().add(functionGroup);

        UserContext userContext = new UserContext(userId, serviceAgreementId);

        userAccessFunctionGroupService.addSystemFunctionGroupToUserAccess(functionGroupId, serviceAgreement,
            userContext);

        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .save(userAssignedFunctionGroupCaptor.capture());

        assertEquals(userContext, userAssignedFunctionGroupCaptor.getValue().getUserContext());
        assertEquals(functionGroup, userAssignedFunctionGroupCaptor.getValue().getFunctionGroup());
    }

    @Test
    public void shouldReturnUsersAndFunctionGroupIdsWhenDataGroupTypeAndDataItemIdNotProvided() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";

        String functionGroupId1 = "FG-01";
        String functionGroupId2 = "FG-02";
        String functionGroupId3 = "FG-03";
        String userId1 = "U-01";
        String userId2 = "U-02";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);

        Map<String, Set<String>> userFunctionGroupIds = new HashMap<>();
        userFunctionGroupIds.put(userId1, newHashSet(functionGroupId1, functionGroupId2));
        userFunctionGroupIds.put(userId2, newHashSet(functionGroupId3));

        Set<String> appFnPrivilegeIds = Collections.singleton("appFnId");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(null),
            eq(Collections.singleton(privilege)))).thenReturn(appFnPrivilegeIds);

        when(userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndAfpIds(eq(serviceAgreementId), eq(appFnPrivilegeIds)))
            .thenReturn(userFunctionGroupIds);

        Map<String, Set<String>> usersFunctionGroups = userAccessFunctionGroupService
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, null, null);

        verify(userAssignedFunctionGroupJpaRepository)
            .findByServiceAgreementIdAndAfpIds(eq(serviceAgreementId), eq(appFnPrivilegeIds));

        assertEquals(2, usersFunctionGroups.keySet().size());
        assertTrue(usersFunctionGroups.keySet().containsAll(asList(userId1, userId2)));
        assertEquals(1, usersFunctionGroups.get(userId2).size());
        assertTrue(usersFunctionGroups.get(userId2).contains(functionGroupId3));
        assertEquals(2, usersFunctionGroups.get(userId1).size());
        assertTrue(usersFunctionGroups.get(userId1).containsAll(asList(functionGroupId1, functionGroupId2)));
    }

    @Test
    public void shouldReturnUsersAndFunctionGroupIdsWhenDataGroupTypeAndDataItemIdAreProvided() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";
        String dataGroupType = "ARRANGEMENTS";
        String dataItemId = "DG_ITEM_ID";

        String functionGroupId1 = "FG-01";
        String functionGroupId2 = "FG-02";
        String functionGroupId3 = "FG-03";
        String userId1 = "U-01";
        String userId2 = "U-02";
        Set<String> afpIds = newHashSet("afp1", "afp2");

        Map<String, Set<String>> userFgIds = new HashMap<>();
        userFgIds.put(userId1, newHashSet(functionGroupId1, functionGroupId2));
        userFgIds.put(userId2, newHashSet(functionGroupId3));

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(null),
            eq(Collections.singleton(privilege)))).thenReturn(afpIds);

        when(userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(eq(serviceAgreementId), eq(dataItemId),
                eq(dataGroupType), eq(afpIds))).thenReturn(userFgIds);

        Map<String, Set<String>> usersFunctionGroups = userAccessFunctionGroupService
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId);

        verify(userAssignedFunctionGroupJpaRepository)
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(eq(serviceAgreementId), eq(dataItemId),
                eq(dataGroupType), eq(afpIds));

        assertEquals(2, usersFunctionGroups.keySet().size());
        assertTrue(usersFunctionGroups.keySet().containsAll(asList(userId1, userId2)));
        assertEquals(1, usersFunctionGroups.get(userId2).size());
        assertTrue(usersFunctionGroups.get(userId2).contains(functionGroupId3));
        assertEquals(2, usersFunctionGroups.get(userId1).size());
        assertTrue(usersFunctionGroups.get(userId1).containsAll(asList(functionGroupId1, functionGroupId2)));
    }

    @Test
    public void shouldValidateRemoveUsers() {
        Set<String> allUserIdsToBeRemoved = new HashSet<>(asList("1", "3", "5", "7"));
        String serviceAgreementId = "Sa_ID";
        when(userAssignedFunctionGroupJpaRepository
            .countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(serviceAgreementId, allUserIdsToBeRemoved,
                FunctionGroupType.DEFAULT)).thenReturn(0L);

        userAccessFunctionGroupService
            .checkIfUsersHaveAssignedPrivilegesForServiceAgreement(serviceAgreementId, allUserIdsToBeRemoved);
    }

    @Test
    public void shouldThrowBadRequestWhenValidatingRemoveUsers() {
        Set<String> allUserIdsToBeRemoved = new HashSet<>(asList("1", "3", "5", "7"));
        String serviceAgreementId = "Sa_ID";
        when(userAssignedFunctionGroupJpaRepository
            .countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(serviceAgreementId, allUserIdsToBeRemoved,
                FunctionGroupType.DEFAULT)).thenReturn(1L);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userAccessFunctionGroupService
            .checkIfUsersHaveAssignedPrivilegesForServiceAgreement(serviceAgreementId, allUserIdsToBeRemoved));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_041.getErrorMessage(), ERR_ACC_041.getErrorCode()));
    }

    @Test
    public void shouldGetAllByServiceAgreementExternalIdAndFunctionGroupType() {
        String externalServiceAgreementId = "EX_SA_ID";
        FunctionGroupType functionGroupType = FunctionGroupType.DEFAULT;
        String userId = "user_id";
        List<String> userIds = singletonList(userId);

        when(userAssignedFunctionGroupJpaRepository
            .findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(externalServiceAgreementId,
                functionGroupType)).thenReturn(userIds);

        List<String> allUserIds = userAccessFunctionGroupService
            .getAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(externalServiceAgreementId,
                functionGroupType);

        assertEquals(userIds.size(), allUserIds.size());
        assertThat(allUserIds, hasItems(userId));
    }


    private void mockUserAssignedFunctionGroups(String functionGroupId, String userId, String serviceAgreementId,
        Optional<UserAssignedFunctionGroup> userAssignedFunctionGroup) {
        when(userAssignedFunctionGroupJpaRepository.findByUserIdAndServiceAgreementIdAndFunctionGroupId(userId,
            serviceAgreementId,
            functionGroupId))
            .thenReturn(userAssignedFunctionGroup);
    }

    private void mockGetServiceAgreementById(String serviceAgreementId, Optional<ServiceAgreement> serviceAgreement) {
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED))
            .thenReturn(serviceAgreement);
    }

    private void mockGetFunctionGroupById(String functionGroupId, Optional<FunctionGroup> functionGroup2,
        FunctionGroupType type) {
        when(functionGroupJpaRepository.findByIdAndType(functionGroupId, type))
            .thenReturn(functionGroup2);
    }
}
