package com.backbase.accesscontrol.api.service.it.useraccess;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.PUT;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.UsersServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link UsersServiceApiController#putAssignUserPermissions}
 */
public class PutAssignUserPermissionsIT extends TestDbWireMock {

    private static final String PUT_USER_PERMISSIONS_URL = "/accessgroups/users/permissions/user-permissions";
    private static final String POST_USERS_PERSISTENCE_URL = "/service-api/v2/users/bulk/externalids";

    private static final String EXTERNAL_USER_ID = "externalUserId";
    private static final String USER_ID = UUID.randomUUID().toString();

    private static final String EXTERNAL_USER_ID1 = "externalUserId1";
    private static final String USER_ID1 = UUID.randomUUID().toString();

    private static final String EXTERNAL_USER_ID2 = "externalUserId2";
    private static final String USER_ID2 = UUID.randomUUID().toString();

    private LegalEntity legalEntity;
    private LegalEntity legalEntity1;

    private LegalEntity legalEntityNotRoot;
    private ServiceAgreement serviceAgreement;
    private ServiceAgreement serviceAgreementTimeBound;
    private ServiceAgreement serviceAgreementTimeBound1;
    private ServiceAgreement serviceAgreementTimeBound2;
    private ServiceAgreement serviceAgreement4;

    private FunctionGroup functionGroup;
    private FunctionGroup functionGroupTemplate;
    private FunctionGroup functionGroupWithTimeBound;
    private FunctionGroup functionGroupWithTimeBound1;
    private FunctionGroup functionGroupWithTimeBound2;
    private FunctionGroup functionGroup4;
    private FunctionGroup functionGroup5;
    private DataGroup dataGroup;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;
    private DataGroup dataGroup3;
    private GroupedFunctionPrivilege groupedFunctionPrivilege;
    private UserContext userContext1;
    private UserContext userContext2;
    private UserContext userContext3;
    private UserContext userContext4;

    private ApplicableFunctionPrivilege apfBf1003Edit;
    private ApplicableFunctionPrivilege manageDataGroupsApproveAfp;

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        apfBf1003Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1003", "edit");

        manageDataGroupsApproveAfp = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "approve");

        legalEntity = createLegalEntity("le-name", "Backbase", null);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        legalEntity1 = createLegalEntity("le-name01", "Backbase01", null);
        legalEntity1 = legalEntityJpaRepository.save(legalEntity1);

        legalEntityNotRoot = createLegalEntity("le-name1", "Backbase1", legalEntity);
        legalEntityNotRoot = legalEntityJpaRepository.save(legalEntityNotRoot);

        serviceAgreement = createServiceAgreement("sa-01", "SA-01", "desc", legalEntity, legalEntity.getId(),
            legalEntity.getId());
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(newHashSet(apsDefaultRegular));
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        serviceAgreement4 = createServiceAgreement("sa-04", "SA-04", "desc", legalEntity1, legalEntity1.getId(),
            legalEntity1.getId());
        serviceAgreement4.setMaster(true);
        serviceAgreement4.setPermissionSetsRegular(newHashSet(apsDefaultRegular));
        serviceAgreement4 = serviceAgreementJpaRepository.save(serviceAgreement4);

        serviceAgreementTimeBound = createServiceAgreement("sa-02", "SA-02", "desc", legalEntityNotRoot,
            legalEntityNotRoot.getId(),
            legalEntityNotRoot.getId());
        serviceAgreementTimeBound1 = createServiceAgreement("sa-03", "SA-03", "desc3", legalEntityNotRoot,
            legalEntityNotRoot.getId(),
            legalEntityNotRoot.getId());
        serviceAgreementTimeBound2 = createServiceAgreement("sa-032", "SA-032", "desc32", legalEntityNotRoot,
            legalEntityNotRoot.getId(),
            legalEntityNotRoot.getId());
        Map<String, Participant> participants = new HashMap<>();
        Map<String, Participant> participants1 = new HashMap<>();
        Map<String, Participant> participants2 = new HashMap<>();
        Set<ParticipantUser> users = new HashSet<>();
        Set<ParticipantUser> users1 = new HashSet<>();
        Set<ParticipantUser> users2 = new HashSet<>();
        serviceAgreementTimeBound.setMaster(false);
        serviceAgreementTimeBound.setStartDate(new Date(1));
        serviceAgreementTimeBound.setEndDate(new Date(500000));
        serviceAgreementTimeBound.setParticipants(participants);
        serviceAgreementTimeBound.setPermissionSetsRegular(newHashSet(apsDefaultRegular));
        serviceAgreementTimeBound = serviceAgreementJpaRepository.save(serviceAgreementTimeBound);

        serviceAgreementTimeBound1.setMaster(false);
        serviceAgreementTimeBound1.setStartDate(new Date(100000));
        serviceAgreementTimeBound1.setEndDate(null);
        serviceAgreementTimeBound1.setParticipants(participants1);
        serviceAgreementTimeBound1 = serviceAgreementJpaRepository.save(serviceAgreementTimeBound1);

        serviceAgreementTimeBound2.setMaster(false);
        serviceAgreementTimeBound2.setStartDate(new Date(1000));
        serviceAgreementTimeBound2.setEndDate(new Date(500000));
        serviceAgreementTimeBound2.setParticipants(participants2);
        serviceAgreementTimeBound2 = serviceAgreementJpaRepository.save(serviceAgreementTimeBound2);

        Participant participant = new Participant()
            .withShareUsers(true)
            .withParticipantUsers(users)
            .withLegalEntity(legalEntityNotRoot)
            .withServiceAgreement(serviceAgreementTimeBound);
        ParticipantUser participantUser = new ParticipantUser()
            .withParticipant(participant)
            .withUserId(USER_ID1);
        users.add(participantUser);
        Participant saveParticipant = participantJpaRepository.save(participant);
        participants.put(saveParticipant.getId(), saveParticipant);

        Participant participant1 = new Participant()
            .withShareUsers(true)
            .withParticipantUsers(users1)
            .withLegalEntity(legalEntityNotRoot)
            .withServiceAgreement(serviceAgreementTimeBound1);
        ParticipantUser participantUser1 = new ParticipantUser()
            .withParticipant(participant1)
            .withUserId(USER_ID2);
        users1.add(participantUser1);
        Participant saveParticipant1 = participantJpaRepository.save(participant1);
        participants1.put(saveParticipant1.getId(), saveParticipant1);

        Participant participant2 = new Participant()
            .withShareUsers(true)
            .withParticipantUsers(users2)
            .withLegalEntity(legalEntityNotRoot)
            .withServiceAgreement(serviceAgreementTimeBound2);
        ParticipantUser participantUser2 = new ParticipantUser()
            .withParticipant(participant2)
            .withUserId(USER_ID2);
        users2.add(participantUser2);
        Participant saveParticipant2 = participantJpaRepository.save(participant2);
        participants1.put(saveParticipant2.getId(), saveParticipant2);

        functionGroup = getFunctionGroup(null, "name", "description", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        functionGroupTemplate = getFunctionGroup(null, "name-template", "description-template", new HashSet<>(),
            FunctionGroupType.TEMPLATE, serviceAgreement);
        functionGroupTemplate.setAssignablePermissionSet(apsDefaultRegular);
        functionGroupTemplate = functionGroupJpaRepository.save(functionGroupTemplate);

        functionGroupWithTimeBound = getFunctionGroup(null, "name1", "description1",
            new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreementTimeBound);
        functionGroupWithTimeBound.setStartDate(new Date(1000));
        functionGroupWithTimeBound.setEndDate(new Date(2000));
        functionGroupWithTimeBound = functionGroupJpaRepository.save(functionGroupWithTimeBound);

        functionGroupWithTimeBound1 = getFunctionGroup(null, "name2", "description2",
            new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreementTimeBound1);
        functionGroupWithTimeBound1.setStartDate(null);
        functionGroupWithTimeBound1.setEndDate(new Date(2000000000));
        functionGroupWithTimeBound1 = functionGroupJpaRepository.save(functionGroupWithTimeBound1);

        functionGroupWithTimeBound2 = getFunctionGroup(null, "name22", "description22",
            new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreementTimeBound2);
        functionGroupWithTimeBound2 = functionGroupJpaRepository.save(functionGroupWithTimeBound2);

        functionGroup4 = getFunctionGroup(null, "name4", "description", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement4);
        functionGroup4 = functionGroupJpaRepository.save(functionGroup4);

        functionGroup5 = getFunctionGroup(null, "name05", "description", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement4);
        functionGroup5 = functionGroupJpaRepository.save(functionGroup5);

        dataGroup = createDataGroup("dag01", "ARRANGEMENTS", "dag01", serviceAgreement);
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        dataGroup1 = createDataGroup("dag02", "ARRANGEMENTS", "dag02", serviceAgreementTimeBound);
        dataGroup1 = dataGroupJpaRepository.save(dataGroup1);

        dataGroup2 = createDataGroup("dag03", "ARRANGEMENTS", "dag03", serviceAgreementTimeBound1);
        dataGroup2 = dataGroupJpaRepository.save(dataGroup2);

        dataGroup3 = createDataGroup("dag033", "ARRANGEMENTS", "dag033", serviceAgreementTimeBound2);
        dataGroup3 = dataGroupJpaRepository.save(dataGroup3);

        userContext1 = new UserContext(USER_ID1, serviceAgreementTimeBound.getId());

        UserAssignedFunctionGroup uaFg01 = new UserAssignedFunctionGroup(functionGroupWithTimeBound, userContext1);
        uaFg01.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup1.getId()), uaFg01)));

        userContext1.setUserAssignedFunctionGroups(newHashSet(uaFg01));
        userContextJpaRepository.save(userContext1);

        userContext2 = new UserContext(USER_ID2, serviceAgreementTimeBound1.getId());

        UserAssignedFunctionGroup uaFg02 = new UserAssignedFunctionGroup(functionGroupWithTimeBound1, userContext2);
        uaFg02.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup2.getId()), uaFg02)));

        userContext2.setUserAssignedFunctionGroups(newHashSet(uaFg02));
        userContextJpaRepository.save(userContext2);

        userContext3 = new UserContext(USER_ID2, serviceAgreementTimeBound2.getId());

        UserAssignedFunctionGroup uaFg03 = new UserAssignedFunctionGroup(functionGroupWithTimeBound2, userContext3);
        uaFg03.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup3.getId()), uaFg03)));

        userContext3.setUserAssignedFunctionGroups(newHashSet(uaFg03));
        userContextJpaRepository.save(userContext3);

        userContext4 = new UserContext(USER_ID, serviceAgreement4.getId());

        UserAssignedFunctionGroup uaFg04 = new UserAssignedFunctionGroup(functionGroup4, userContext4);

        userContext4.setUserAssignedFunctionGroups(newHashSet(uaFg04));

        userContextJpaRepository.save(userContext4);
    }

    @Test
    public void shouldSuccessfullyUpdateUsersPermissionsWithTemplateFromRoot() throws Exception {
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID1)
            .withExternalServiceAgreementId(serviceAgreementTimeBound.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroupTemplate.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup1.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID1.toUpperCase());
        user.setId(USER_ID1);
        user.legalEntityId(legalEntityNotRoot.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID1.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString,
            HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID1, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreementTimeBound.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());

        assertTrue(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID1, serviceAgreementTimeBound.getId(),
                functionGroupTemplate.getId()).isPresent());

        assertFalse(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID1, serviceAgreementTimeBound.getId(),
                functionGroupWithTimeBound.getId()).isPresent());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreementTimeBound.getId())
            .withUserId(USER_ID1)));
    }

    @Test
    public void shouldSuccessfullyUpdateEmptyUsersPermissions() throws Exception {
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString,
            HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreement.getId())
            .withUserId(USER_ID)));
    }

    @Test
    public void shouldSuccessfullyUpdateUsersPermissionsWithEmptyDataGroups() throws Exception {
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement4.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup5.getId()))
                .withDataGroupIdentifiers(emptyList())));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(legalEntity1.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString,
            HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement4.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());

        assertTrue(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID, serviceAgreement4.getId(),
                functionGroup5.getId()).isPresent());

        assertFalse(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID, serviceAgreement4.getId(),
                functionGroup4.getId()).isPresent());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreement4.getId())
            .withUserId(USER_ID)));
    }

    @Test
    public void shouldUpdatePermissionsAndRemoveOnlyRelatedSelfApprovalPoliciesFromCombinationWhenDataGroupsAreAssigned()
        throws Exception {
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(manageDataGroupsApproveAfp.getId());
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);

        functionGroup.setPermissions(Set.of(groupedFunctionPrivilege));
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        FunctionGroupItemId functionGroupItemId = new FunctionGroupItemId();
        functionGroupItemId.setFunctionGroupId(functionGroup.getId());
        functionGroupItemId.setApplicableFunctionPrivilegeId(manageDataGroupsApproveAfp.getId());

        FunctionGroupItemEntity functionGroupItemEntity = new FunctionGroupItemEntity();
        functionGroupItemEntity.setFunctionGroupItemId(functionGroupItemId);

        SelfApprovalPolicy policy = new SelfApprovalPolicy();
        policy.setCanSelfApprove(true);
        policy.setFunctionGroupItem(functionGroupItemEntity);

        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.addPolicies(Set.of(policy));
        combination.getDataGroupIds().add(dataGroup.getId());

        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroup.addCombination(combination);
        userContext.setUserAssignedFunctionGroups(Set.of(userAssignedFunctionGroup));

        userContextJpaRepository.save(userContext);

        PresentationFunctionGroupDataGroup functionGroupDataGroup = new PresentationFunctionGroupDataGroup()
            .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup.getId()))
            .withDataGroupIdentifiers(List.of(new PresentationIdentifier().withIdIdentifier(dataGroup.getId())));

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions();
        assignUserPermissions.setExternalUserId(EXTERNAL_USER_ID);
        assignUserPermissions.setExternalServiceAgreementId(serviceAgreement.getExternalId());
        assignUserPermissions.setFunctionGroupDataGroups(List.of(functionGroupDataGroup));

        GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID);
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPost(POST_USERS_PERSISTENCE_URL, List.of(user), 200);

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, List.of(assignUserPermissions), PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {});

        assertThat(responseItemsExtended, hasSize(1));
        assertThat(responseItemsExtended.get(0).getStatus(), equalTo(BatchResponseStatusCode.HTTP_STATUS_OK));
        assertThat(responseItemsExtended.get(0).getResourceId(), equalTo(EXTERNAL_USER_ID));
        assertThat(responseItemsExtended.get(0).getExternalServiceAgreementId(), equalTo(serviceAgreement.getExternalId()));
        assertThat(responseItemsExtended.get(0).getErrors(), empty());

        List<UserAssignedFunctionGroup> assignedFunctionGroups = userAssignedFunctionGroupJpaRepository
            .findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(USER_ID, serviceAgreement.getId(),
                List.of(FunctionGroupType.DEFAULT));

        assertThat(assignedFunctionGroups, hasSize(1));
        UserAssignedFunctionGroup assignedFunctionGroup = assignedFunctionGroups.iterator().next();
        assertThat(assignedFunctionGroup.getFunctionGroupId(), equalTo(functionGroup.getId()));
        assertThat(assignedFunctionGroup.getUserAssignedFunctionGroupCombinations(), hasSize(1));

        UserAssignedFunctionGroupCombination assignedCombination = assignedFunctionGroup
            .getUserAssignedFunctionGroupCombinations().iterator().next();

        assertThat(assignedCombination.getDataGroupIds(), hasSize(1));
        assertThat(assignedCombination.getDataGroupIds(), contains(dataGroup.getId()));
        assertThat(assignedCombination.getSelfApprovalPolicies(), empty());
    }

    @Test
    public void shouldUpdatePermissionsAndRemoveCombinationWithSelfApprovalPoliciesWhenNoDataGroupsWerePreviouslyAssigned()
        throws Exception {
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(manageDataGroupsApproveAfp.getId());
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);

        functionGroup.setPermissions(Set.of(groupedFunctionPrivilege));
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        FunctionGroupItemId functionGroupItemId = new FunctionGroupItemId();
        functionGroupItemId.setFunctionGroupId(functionGroup.getId());
        functionGroupItemId.setApplicableFunctionPrivilegeId(manageDataGroupsApproveAfp.getId());

        FunctionGroupItemEntity functionGroupItemEntity = new FunctionGroupItemEntity();
        functionGroupItemEntity.setFunctionGroupItemId(functionGroupItemId);

        SelfApprovalPolicy policy = new SelfApprovalPolicy();
        policy.setCanSelfApprove(true);
        policy.setFunctionGroupItem(functionGroupItemEntity);

        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.addPolicies(Set.of(policy));

        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroup.addCombination(combination);
        userContext.setUserAssignedFunctionGroups(Set.of(userAssignedFunctionGroup));

        userContextJpaRepository.save(userContext);

        PresentationFunctionGroupDataGroup functionGroupDataGroup = new PresentationFunctionGroupDataGroup()
            .withFunctionGroupIdentifier(new PresentationIdentifier().withIdIdentifier(functionGroup.getId()));

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions();
        assignUserPermissions.setExternalUserId(EXTERNAL_USER_ID);
        assignUserPermissions.setExternalServiceAgreementId(serviceAgreement.getExternalId());
        assignUserPermissions.setFunctionGroupDataGroups(List.of(functionGroupDataGroup));

        GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID);
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPost(POST_USERS_PERSISTENCE_URL, List.of(user), 200);

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, List.of(assignUserPermissions), PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {});

        assertThat(responseItemsExtended, hasSize(1));
        assertThat(responseItemsExtended.get(0).getStatus(), equalTo(BatchResponseStatusCode.HTTP_STATUS_OK));
        assertThat(responseItemsExtended.get(0).getResourceId(), equalTo(EXTERNAL_USER_ID));
        assertThat(responseItemsExtended.get(0).getExternalServiceAgreementId(), equalTo(serviceAgreement.getExternalId()));
        assertThat(responseItemsExtended.get(0).getErrors(), empty());

        List<UserAssignedFunctionGroup> assignedFunctionGroups = userAssignedFunctionGroupJpaRepository
            .findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(USER_ID, serviceAgreement.getId(),
                List.of(FunctionGroupType.DEFAULT));

        assertThat(assignedFunctionGroups, hasSize(1));
        UserAssignedFunctionGroup assignedFunctionGroup = assignedFunctionGroups.iterator().next();
        assertThat(assignedFunctionGroup.getFunctionGroupId(), equalTo(functionGroup.getId()));
        assertThat(assignedFunctionGroup.getUserAssignedFunctionGroupCombinations(), empty());
    }

    @Test
    public void shouldSuccessfullyUpdateFunctionGroupInUsersPermissionsWhenUpdateSameFgWithoutDg() throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroup);
        functionGroup.setPermissions(newHashSet(groupedFunctionPrivilege));
        functionGroup = functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.flush();
        functionGroup.getPermissionsStream().forEach(gfp -> {
            groupedFunctionPrivilege = gfp.equals(groupedFunctionPrivilege) ? gfp : groupedFunctionPrivilege;
        });
        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());
        UserAssignedFunctionGroup uaFg0 = new UserAssignedFunctionGroup(functionGroup, userContext);
        uaFg0.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Collections.emptySet(), uaFg0)));
        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg0));
        userContextJpaRepository.save(userContext);
        FunctionGroup newFunctionGroup = getFunctionGroup(null, "name2", "description", new HashSet<>(),
            FunctionGroupType.TEMPLATE, serviceAgreement);
        newFunctionGroup.setAssignablePermissionSet(apsDefaultRegular);
        newFunctionGroup = functionGroupJpaRepository.save(newFunctionGroup);
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(asList(
                new PresentationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(
                        new PresentationIdentifier()
                            .withIdIdentifier(functionGroup.getId()))
                    .withDataGroupIdentifiers(emptyList()),
                new PresentationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(new PresentationIdentifier()
                        .withIdIdentifier(newFunctionGroup.getId()))
                    .withDataGroupIdentifiers(emptyList())));
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));
        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));
        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);
        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });
        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());
        assertTrue(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID, serviceAgreement.getId(),
                functionGroup.getId()).isPresent());
        assertTrue(userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID, serviceAgreement.getId(),
                newFunctionGroup.getId()).isPresent());
        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreement.getId())
            .withUserId(USER_ID)));
    }

    @Test
    public void shouldSuccessfullyUpdateDataGroupInUsersPermissions() throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroup);

        functionGroup.setPermissions(newHashSet(groupedFunctionPrivilege));
        functionGroup = functionGroupJpaRepository.saveAndFlush(functionGroup);

        functionGroup.getPermissionsStream().forEach(gfp -> {
            groupedFunctionPrivilege = gfp.equals(groupedFunctionPrivilege) ? gfp : groupedFunctionPrivilege;
        });

        DataGroup newDataGroup = createDataGroup("dag02", "ARRANGEMENTS", "dag02", serviceAgreement);
        newDataGroup = dataGroupJpaRepository.save(newDataGroup);

        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());

        UserAssignedFunctionGroup uaFg0 = new UserAssignedFunctionGroup(functionGroup, userContext);
        uaFg0.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Sets.newHashSet(newDataGroup.getId()), uaFg0)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg0));
        userContextJpaRepository.save(userContext);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(
                    new PresentationIdentifier()
                        .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(asList(new PresentationIdentifier()
                        .withIdIdentifier(dataGroup.getId()),
                    new PresentationIdentifier()
                        .withIdIdentifier(newDataGroup.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreement.getId())
            .withUserId(USER_ID)));
    }

    @Test
    public void shouldSuccessfullyUpdateFunctionGroupInUsersPermissions() throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroup);

        functionGroup.setPermissions(newHashSet(groupedFunctionPrivilege));
        functionGroup = functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.flush();

        functionGroup.getPermissionsStream().forEach(gfp -> {
            groupedFunctionPrivilege = gfp.equals(groupedFunctionPrivilege) ? gfp : groupedFunctionPrivilege;
        });

        DataGroup newDataGroup = createDataGroup("dag02", "ARRANGEMENTS", "dag02", serviceAgreement);
        newDataGroup = dataGroupJpaRepository.save(newDataGroup);

        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());

        UserAssignedFunctionGroup uaFg0 = new UserAssignedFunctionGroup(functionGroup, userContext);
        uaFg0.setUserAssignedFunctionGroupCombinations(
            singleton(new UserAssignedFunctionGroupCombination(Sets.newHashSet(newDataGroup.getId()), uaFg0)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg0));
        userContextJpaRepository.save(userContext);

        FunctionGroup newFunctionGroup = getFunctionGroup(null, "name2", "description", new HashSet<>(),
            FunctionGroupType.TEMPLATE, serviceAgreement);
        newFunctionGroup.setAssignablePermissionSet(apsDefaultRegular);
        newFunctionGroup = functionGroupJpaRepository.save(newFunctionGroup);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(asList(
                new PresentationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(
                        new PresentationIdentifier()
                            .withNameIdentifier(new NameIdentifier()
                                .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                                .withName(functionGroup.getName())))
                    .withDataGroupIdentifiers(asList(new PresentationIdentifier()
                            .withNameIdentifier(new NameIdentifier()
                                .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                                .withName(dataGroup.getName())),
                        new PresentationIdentifier()
                            .withIdIdentifier(newDataGroup.getId()))),
                new PresentationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(new PresentationIdentifier()
                        .withIdIdentifier(functionGroup.getId()))
                    .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                        .withIdIdentifier(newDataGroup.getId()))),
                new PresentationFunctionGroupDataGroup()
                    .withFunctionGroupIdentifier(new PresentationIdentifier()
                        .withNameIdentifier(new NameIdentifier()
                            .withName(newFunctionGroup.getName())
                            .withExternalServiceAgreementId(newFunctionGroup.getServiceAgreement().getExternalId())))
                    .withDataGroupIdentifiers(new ArrayList<>())));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(legalEntity.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(0, responseItemsExtended.get(0).getErrors().size());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreement.getId())
            .withUserId(USER_ID)));
    }

    @Test
    public void shouldReturnBadRequestIfServiceAgreementDoesNotExist() throws Exception {
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId("random")
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID1);
        user.legalEntityId(legalEntity.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals("random", responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(ERR_ACQ_006.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));
    }

    @Test
    public void shouldReturnBadRequestIfUserDoesNotBelongToServiceAgreementAndIsMaster() throws Exception {
        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup.getId())))));

        String randomLegalEntityId = getUuid();

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(randomLegalEntityId);

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals("User with id " + USER_ID + " does not belong in service agreement",
            responseItemsExtended.get(0).getErrors().get(0));
    }

    @Test
    public void shouldReturnBadRequestIfUserDoesNotBelongToServiceAgreementAndIsCustom() throws Exception {
        ServiceAgreement customServiceAgreement = createServiceAgreement("sa-custom", "SA-01", "desc", legalEntity,
            legalEntity.getId(),
            legalEntity.getId());
        serviceAgreement.setMaster(false);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID)
            .withExternalServiceAgreementId(customServiceAgreement.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(new PresentationIdentifier()
                    .withIdIdentifier(functionGroup.getId()))
                .withDataGroupIdentifiers(singletonList(new PresentationIdentifier()
                    .withIdIdentifier(dataGroup.getId())))));

        String randomLegalEntityId = getUuid();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID.toUpperCase());
        user.setId(USER_ID);
        user.legalEntityId(randomLegalEntityId);


        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreement.getExternalId(), responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals("User with id " + USER_ID + " does not belong in service agreement",
            responseItemsExtended.get(0).getErrors().get(0));
    }

    @Test
    public void shouldAssignPermissionsWithTimeBound() throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroupWithTimeBound);

        functionGroupWithTimeBound.getPermissions().add(groupedFunctionPrivilege);
        functionGroupWithTimeBound = functionGroupJpaRepository.saveAndFlush(functionGroupWithTimeBound);

        DataGroup newDataGroup1 = createDataGroup("dag021", "ARRANGEMENTS", "dag021",
            serviceAgreementTimeBound);
        newDataGroup1 = dataGroupJpaRepository.save(newDataGroup1);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID1)
            .withExternalServiceAgreementId(serviceAgreementTimeBound.getExternalId())
            .withFunctionGroupDataGroups(Collections.singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(
                    new PresentationIdentifier()
                        .withIdIdentifier(functionGroupWithTimeBound.getId()))
                .withDataGroupIdentifiers(asList(new PresentationIdentifier()
                        .withIdIdentifier(dataGroup1.getId()),
                    new PresentationIdentifier()
                        .withIdIdentifier(newDataGroup1.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID1.toUpperCase());
        user.setId(USER_ID1);
        user.legalEntityId(legalEntityNotRoot.getId());


        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID1.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(Collections.singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID1, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreementTimeBound.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreementTimeBound.getId())
            .withUserId(USER_ID1)));
    }

    @Test
    public void shouldAssignPermissionsWithTimeBoundAndShouldHaveStartDateFromSaAndEndDateFromFgInUserPrivilegesTables()
        throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroupWithTimeBound1);

        functionGroupWithTimeBound1.getPermissions().add(groupedFunctionPrivilege);
        functionGroupWithTimeBound.setStartDate(null);
        functionGroupWithTimeBound1 = functionGroupJpaRepository.saveAndFlush(functionGroupWithTimeBound1);

        DataGroup newDataGroup2 = createDataGroup("dag0211", "ARRANGEMENTS", "dag0211",
            serviceAgreementTimeBound1);
        newDataGroup2 = dataGroupJpaRepository.saveAndFlush(newDataGroup2);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID2)
            .withExternalServiceAgreementId(serviceAgreementTimeBound1.getExternalId())
            .withFunctionGroupDataGroups(Collections.singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(
                    new PresentationIdentifier()
                        .withIdIdentifier(functionGroupWithTimeBound1.getId()))
                .withDataGroupIdentifiers(asList(new PresentationIdentifier()
                        .withIdIdentifier(dataGroup2.getId()),
                    new PresentationIdentifier()
                        .withIdIdentifier(newDataGroup2.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID2.toUpperCase());
        user.setId(USER_ID2);
        user.legalEntityId(legalEntityNotRoot.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID2.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID2, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreementTimeBound1.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());
        assertEquals(1, responseItemsExtended.size());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreementTimeBound1.getId())
            .withUserId(USER_ID2)));
    }

    @Test
    public void shouldAssignPermissionsWithTimeBoundAndShouldHaveStartAndEndDateFromSaInUserPrivilegesTables()
        throws Exception {
        groupedFunctionPrivilege = getGroupedFunctionPrivilege(null, apfBf1003Edit, functionGroupWithTimeBound2);

        functionGroupWithTimeBound2.getPermissions().add(groupedFunctionPrivilege);
        functionGroupWithTimeBound2 = functionGroupJpaRepository.saveAndFlush(functionGroupWithTimeBound2);

        DataGroup newDataGroup22 = createDataGroup("dag02112", "ARRANGEMENTS", "dag02112",
            serviceAgreementTimeBound2);
        newDataGroup22 = dataGroupJpaRepository.saveAndFlush(newDataGroup22);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(EXTERNAL_USER_ID2)
            .withExternalServiceAgreementId(serviceAgreementTimeBound2.getExternalId())
            .withFunctionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .withFunctionGroupIdentifier(
                    new PresentationIdentifier()
                        .withIdIdentifier(functionGroupWithTimeBound2.getId()))
                .withDataGroupIdentifiers(asList(new PresentationIdentifier()
                        .withIdIdentifier(dataGroup3.getId()),
                    new PresentationIdentifier()
                        .withIdIdentifier(newDataGroup22.getId())))));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setExternalId(EXTERNAL_USER_ID2.toUpperCase());
        user.setId(USER_ID2);
        user.legalEntityId(legalEntityNotRoot.getId());

        addStubPostEqualToJson(POST_USERS_PERSISTENCE_URL,
            objectMapper.writeValueAsString(singletonList(user)),
            200,
            objectMapper
                .writeValueAsString(Lists.newArrayList(EXTERNAL_USER_ID2.toUpperCase())));

        String requestAsString = objectMapper.writeValueAsString(singletonList(assignUserPermissions));

        String returnedResponse = executeRequest(PUT_USER_PERMISSIONS_URL, requestAsString, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseItemsExtended = objectMapper
            .readValue(returnedResponse, new TypeReference<>() {
            });

        assertEquals(1, responseItemsExtended.size());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, responseItemsExtended.get(0).getStatus());
        assertEquals(EXTERNAL_USER_ID2, responseItemsExtended.get(0).getResourceId());
        assertEquals(serviceAgreementTimeBound2.getExternalId(),
            responseItemsExtended.get(0).getExternalServiceAgreementId());

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(serviceAgreementTimeBound2.getId())
            .withUserId(USER_ID2)));
    }
}
