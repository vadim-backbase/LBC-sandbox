package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_017;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_047;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.LegalEntityResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserAccessPermissionCheckServiceTest {

    @Mock
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private MasterServiceAgreementFallbackProperties fallbackProperties;

    @InjectMocks
    private UserAccessPermissionCheckService userAccessPermissionCheckService;

    @Test
    public void shouldPassWhenUserHasAllPrivileges() {

        String userId = "userId";
        String serviceAgreementId = "serviceAgreementId";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String commaSeparatedPrivileges = "execute,read";

        Set<String> privileges = Sets.newLinkedHashSet(commaSeparatedPrivileges.split(","));
        Set<String> appFnPrivilegesIds = Sets.newLinkedHashSet("appFnPId", "appFnPId2");

        when(businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(resourceName), eq(privileges)))
            .thenReturn(appFnPrivilegesIds);

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setBusinessFunctionName(functionName);
        applicableFunctionPrivilege.setBusinessFunctionResourceName(resourceName);
        applicableFunctionPrivilege.setPrivilegeName("execute");
        when(businessFunctionCache.getApplicableFunctionPrivilegeById("appFnPId"))
            .thenReturn(applicableFunctionPrivilege);

        applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setBusinessFunctionName(functionName);
        applicableFunctionPrivilege.setBusinessFunctionResourceName(resourceName);
        applicableFunctionPrivilege.setPrivilegeName("read");
        when(businessFunctionCache.getApplicableFunctionPrivilegeById("appFnPId2"))
            .thenReturn(applicableFunctionPrivilege);

        when(userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                eq(userId),
                eq(serviceAgreementId),
                eq(ServiceAgreementState.ENABLED),
                eq(appFnPrivilegesIds)))
            .thenReturn(new ArrayList<>(appFnPrivilegesIds));

        userAccessPermissionCheckService
            .checkUserPermission(userId, serviceAgreementId, functionName, resourceName, commaSeparatedPrivileges);

        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                userId, serviceAgreementId, ServiceAgreementState.ENABLED, appFnPrivilegesIds);
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHaveAllPrivileges() {

        String userId = "userId";
        String serviceAgreementId = "serviceAgreementId";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String commaSeparatedPrivileges = "execute,read";

        Set<String> privileges = Sets.newLinkedHashSet(commaSeparatedPrivileges.split(","));
        Set<String> appFnPrivilegesIds = Sets.newLinkedHashSet("appFnPId");

        when(businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(resourceName), eq(privileges)))
            .thenReturn(appFnPrivilegesIds);

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setBusinessFunctionName(functionName);
        applicableFunctionPrivilege.setBusinessFunctionResourceName(resourceName);
        applicableFunctionPrivilege.setPrivilegeName("read");
        when(businessFunctionCache.getApplicableFunctionPrivilegeById("appFnPId"))
            .thenReturn(applicableFunctionPrivilege);

        when(userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                eq(userId),
                eq(serviceAgreementId),
                eq(ServiceAgreementState.ENABLED),
                eq(appFnPrivilegesIds)))
            .thenReturn(new ArrayList<>(appFnPrivilegesIds));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userAccessPermissionCheckService
            .checkUserPermission(userId, serviceAgreementId, functionName, resourceName, commaSeparatedPrivileges));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode()));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserHasNoPrivileges() {

        String userId = "userId";
        String serviceAgreementId = "serviceAgreementId";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String commaSeparatedPrivileges = "execute,read";

        Set<String> privileges = Sets.newLinkedHashSet(commaSeparatedPrivileges.split(","));
        Set<String> appFnPrivilegesIds = emptySet();

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName),
            eq(resourceName), eq(privileges))).thenReturn(appFnPrivilegesIds);

        when(userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                eq(userId),
                eq(serviceAgreementId),
                eq(ServiceAgreementState.ENABLED),
                eq(appFnPrivilegesIds)))
            .thenReturn(new ArrayList<>());

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userAccessPermissionCheckService
            .checkUserPermission(userId, serviceAgreementId, functionName, resourceName, commaSeparatedPrivileges));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode()));
    }

    @Test
    public void shouldReturnEmptyListWhenServiceAgreementFromContextIsMasterAndUserHasNoAccess() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(asList(resourceLegalEntity));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(contextServiceAgreementId);
        serviceAgreement.setMaster(true);
        when(persistenceServiceAgreementService
            .getById(contextServiceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities response = userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(data);

        assertEquals(0, response.getLegalEntities().size());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenServiceAgreementFromContextDoesNotExist() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(asList(resourceLegalEntity));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(contextServiceAgreementId);
        serviceAgreement.setMaster(true);
        when(persistenceServiceAgreementService
            .getById(contextServiceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenThrow(getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(data));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldReturnEmptyListWhenServiceAgreementFromContextIsNull() {
        when(fallbackProperties.isEnabled()).thenReturn(true);
        String userLegalEntity = "LE-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(null)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(singletonList(resourceLegalEntity));

        ContextLegalEntities response = userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(data);
        assertEquals(0, response.getLegalEntities().size());

    }

    @Test
    public void shouldThrowForbiddenExceptionWhenServiceAgreementFromContextIsNullAndFallbackDisabled() {
        when(fallbackProperties.isEnabled()).thenReturn(false);
        String userLegalEntity = "LE-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(null)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(singletonList(resourceLegalEntity));

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> userAccessPermissionCheckService
                .checkUserAccessToEntitlementsResources(data));
        assertThat(forbiddenException,
            new ForbiddenErrorMatcher(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode()));
    }

    @Test
    public void shouldReturnProxyWrapperWhenServiceAgreementFromContextIsMasterAndUserHasAccess() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(asList(resourceLegalEntity));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(contextServiceAgreementId);
        serviceAgreement.setMaster(true);
        when(persistenceServiceAgreementService
            .getById(contextServiceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        when(validateLegalEntityHierarchyService.getLegalEntityHierarchy(
            eq(userLegalEntity),
            eq(singletonList(resourceLegalEntity))
        )).thenReturn(singletonList(resourceLegalEntity));

        ContextLegalEntities response = userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(data);

        assertEquals(1, response.getLegalEntities().size());
        assertTrue(response.getLegalEntities().contains(resourceLegalEntity));
    }

    @Test
    public void shouldReturnProxyWrapperWhenServiceAgreementFromContextIsCustomAndUserHasAccess() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String resourceLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withLegalEntityIds(asList(resourceLegalEntity));

        ServiceAgreement serviceAgreement = createServiceAgreement(
            "SA", "extId", "description",
            createLegalEntity("LE", "extId", null),
            resourceLegalEntity,
            userLegalEntity
        );
        Participant participant = createParticipant(userLegalEntity, false, true);
        serviceAgreement.addParticipant(participant);
        when(persistenceServiceAgreementService
            .getById(contextServiceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities response = userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(data);

        assertEquals(1, response.getLegalEntities().size());
        assertTrue(response.getLegalEntities().contains(resourceLegalEntity));
    }

    @Test
    public void shouldReturnContextLegalEntitiesWhenRequestedResourceTypeIsUserAndResourcesToCheckRelatedToAccounts() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String accountLegalEntity = "LE-02";
        EntitlementsResource data = new EntitlementsResource()
                .withContextServiceAgreementId(contextServiceAgreementId)
                .withUserLegalEntityId(userLegalEntity)
                .withLegalEntityIds(Collections.singletonList(accountLegalEntity))
                .withAccessResourceType(AccessResourceType.ACCOUNT);

        ServiceAgreement serviceAgreement = createServiceAgreement(
                "SA", "extId", "description",
                createLegalEntity("LE", "extId", null),
                accountLegalEntity,
                userLegalEntity
        );

        when(persistenceServiceAgreementService
                .getById(contextServiceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
                .thenReturn(serviceAgreement);

        ContextLegalEntities response = userAccessPermissionCheckService
                .checkUserAccessToEntitlementsResources(data);

        assertEquals(1, response.getLegalEntities().size());
        assertTrue(response.getLegalEntities().contains(accountLegalEntity));
    }

    @Test
    public void userHasNoAccessToServiceAgreementThatNotExist() {
        String serviceAgreementId = "SA-02";
        String contextServiceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenThrow(getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userAccessPermissionCheckService.checkUserAccessToServiceAgreement(new ServiceAgreementResource()
            .withServiceAgreementId(serviceAgreementId)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withUserLegalEntityId(userLegalEntity)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void userHasNoAccessToServiceAgreementShouldReturnFalse() {
        String serviceAgreementId = "SA-02";
        String userLegalEntity = "LE-01";
        String resourceLegalEntityId = "LE-02";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement
            .setCreatorLegalEntity(createLegalEntity(userLegalEntity, "name", null, null, LegalEntityType.CUSTOMER));
        serviceAgreement.addParticipant(asList(
            createParticipant(userLegalEntity, false, true),
            createParticipant(resourceLegalEntityId, false, true)
        ));
        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        userAccessPermissionCheckService
            .checkUserAccessToServiceAgreement(new ServiceAgreementResource()
                .withUserLegalEntityId(userLegalEntity)
                .withContextServiceAgreementId(serviceAgreementId)
                .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
                .withServiceAgreementId(serviceAgreementId));

        verify(persistenceServiceAgreementService, times(1))
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

    }

    @Test
    public void testCustomServiceAgreementWithUserLegalEntitySharingAccounts() {
        String serviceAgreementId = "sa-5";
        String userLegalEntity = "le-1";
        String resourceLegalEntityId = "le-5";
        List<Participant> participants = Lists.newArrayList(
            createParticipant(userLegalEntity, false, true),
            createParticipant(resourceLegalEntityId, false, true)
        );
        LegalEntity creatorLegalEntity = getLegalEntity(resourceLegalEntityId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.addParticipant(participants);
        ServiceAgreementResource data = new ServiceAgreementResource()
            .withServiceAgreementId(serviceAgreementId)
            .withContextServiceAgreementId(serviceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withUserLegalEntityId(userLegalEntity);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        userAccessPermissionCheckService.checkUserAccessToServiceAgreement(data);

        verify(persistenceServiceAgreementService, times(1))
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

    }

    @Test
    public void testCustomServiceAgreementWithUserLegalEntitySharingUser() {
        String serviceAgreementId = "sa-5";
        String userLegalEntity = "le-1";
        String resourceLegalEntityId = "le-5";

        List<Participant> participants = Lists.newArrayList(
            createParticipant(userLegalEntity, true, true),
            createParticipant(resourceLegalEntityId, false, true)
        );
        LegalEntity creatorLegalEntity = getLegalEntity(resourceLegalEntityId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.addParticipant(participants);
        ServiceAgreementResource data = new ServiceAgreementResource()
            .withServiceAgreementId(serviceAgreementId)
            .withContextServiceAgreementId(serviceAgreementId)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT)
            .withUserLegalEntityId(userLegalEntity);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        userAccessPermissionCheckService
            .checkUserAccessToServiceAgreement(data);

        verify(persistenceServiceAgreementService, times(1))
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
    }

    @Test
    public void testCustomServiceAgreementNoAccessWithUserLegalEntitySharingAccounts() {
        String serviceAgreementId = "sa-5";
        String userLegalEntity = "le-1";
        String resourceLegalEntityId = "le-5";

        List<Participant> participants = Lists.newArrayList(
            createParticipant(userLegalEntity, false, true),
            createParticipant(resourceLegalEntityId, true, true)
        );
        LegalEntity creatorLegalEntity = getLegalEntity(resourceLegalEntityId);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.addParticipant(participants);
        ServiceAgreementResource data = new ServiceAgreementResource()
            .withServiceAgreementId(serviceAgreementId)
            .withContextServiceAgreementId(serviceAgreementId)
            .withAccessResourceType(AccessResourceType.USER)
            .withUserLegalEntityId(userLegalEntity);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userAccessPermissionCheckService
            .checkUserAccessToServiceAgreement(data));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_ACQ_047.getErrorMessage(), ERR_ACQ_047.getErrorCode()));
    }

    @Test
    public void shouldReturnAllFromUsersHierarchyWhenServiceAgreementIsMaster() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(userLegalEntity)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT);
        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        when(persistenceLegalEntityService.getListOfAllSubEntityIds(userLegalEntity))
            .thenReturn(Lists.newArrayList(userLegalEntity, legalEntityChild));

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(2));
        assertThat(validLegalEntities.getLegalEntities(),
            hasItems(legalEntityChild, userLegalEntity)
        );
    }

    @Test
    public void shouldReturnAllFromUsersHierarchyWhenNoContextIsSelected() {
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(null)
            .withUserLegalEntityId(userLegalEntity)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT);

        when(persistenceLegalEntityService.getListOfAllSubEntityIds(userLegalEntity))
            .thenReturn(Lists.newArrayList(userLegalEntity, legalEntityChild));

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(2));
        assertThat(validLegalEntities.getLegalEntities(),
            hasItems(legalEntityChild, userLegalEntity)
        );
    }

    @Test
    public void shouldReturnAllFromServiceAgreementWhenCustomServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(legalEntityChild)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT);

        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);
        List<Participant> participants = Lists.newArrayList(
            createParticipant(legalEntityChild, false, true),
            createParticipant(userLegalEntity, false, true)
        );

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(participants);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(2));
        assertThat(validLegalEntities.getLegalEntities(),
            hasItems(legalEntityChild, userLegalEntity)
        );
    }

    @Test
    public void shouldReturnOnlyProviderWhenCustomServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(legalEntityChild)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT);

        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);
        List<Participant> participants = Lists.newArrayList(
            createParticipant(legalEntityChild, true, false),
            createParticipant(userLegalEntity, true, false),
            createParticipant("LE-03", false, true)
        );

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(participants);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(2));
        assertThat(validLegalEntities.getLegalEntities(),
            hasItems(legalEntityChild, userLegalEntity)
        );
    }

    @Test
    public void shouldReturnLegalEntitiesWhenUserIsProviderAndConsumer() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(legalEntityChild)
            .withAccessResourceType(AccessResourceType.USER_OR_ACCOUNT);

        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);
        List<Participant> participants = Lists.newArrayList(
            createParticipant(legalEntityChild, true, true),
            createParticipant(userLegalEntity, true, false),
            createParticipant("LE-04", false, true),
            createParticipant("LE-05", false, true),
            createParticipant("LE-06", false, true)
        );

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(participants);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(5));

        assertThat(validLegalEntities.getLegalEntities(),
            containsInAnyOrder("LE-04", "LE-05", "LE-06", userLegalEntity, legalEntityChild));
    }

    @Test
    public void shouldReturnValidLegalEntitiesForListingUsers() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(legalEntityChild)
            .withAccessResourceType(AccessResourceType.USER);

        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);
        List<Participant> participants = Lists.newArrayList(
            createParticipant(legalEntityChild, true, true),
            createParticipant(userLegalEntity, true, false),
            createParticipant("LE-04", false, true),
            createParticipant("LE-05", false, true),
            createParticipant("LE-06", false, true)
        );

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(participants);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(2));

        assertThat(validLegalEntities.getLegalEntities(),
            containsInAnyOrder(userLegalEntity, legalEntityChild));
    }

    @Test
    public void shouldReturnValidLegalEntitiesForListingAccounts() {
        String serviceAgreementId = "SA-01";
        String userLegalEntity = "LE-01";
        String legalEntityChild = "LE-02";

        LegalEntityResource requestData = new LegalEntityResource()
            .withContextServiceAgreementId(serviceAgreementId)
            .withUserLegalEntityId(legalEntityChild)
            .withAccessResourceType(AccessResourceType.ACCOUNT);

        LegalEntity creatorLegalEntity = getLegalEntity(userLegalEntity);
        List<Participant> participants = Lists.newArrayList(
            createParticipant(legalEntityChild, true, true),
            createParticipant(userLegalEntity, true, false),
            createParticipant("LE-04", false, true),
            createParticipant("LE-05", false, true),
            createParticipant("LE-06", false, true)
        );

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(participants);

        when(persistenceServiceAgreementService
            .getById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(serviceAgreement);

        ContextLegalEntities validLegalEntities = userAccessPermissionCheckService
            .getLegalEntitiesThatUserHasAccessTo(requestData);

        assertThat(validLegalEntities.getLegalEntities(), hasSize(4));

        assertThat(validLegalEntities.getLegalEntities(),
            containsInAnyOrder(legalEntityChild, "LE-04", "LE-05", "LE-06"));
    }

    private Participant createParticipant(String legalEntityId, boolean shareUsers, boolean shareAccounts) {
        LegalEntity legalEntity = getLegalEntity(legalEntityId);
        Participant participant = new Participant();
        participant.setLegalEntity(legalEntity);
        participant.setShareUsers(shareUsers);
        participant.setShareAccounts(shareAccounts);
        return participant;
    }


    private LegalEntity getLegalEntity(String userLegalEntity) {
        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId(userLegalEntity);
        return creatorLegalEntity;
    }
}
