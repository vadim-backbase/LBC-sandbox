package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_112;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_114;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.configuration.CombinationConfig;
import com.backbase.accesscontrol.domain.*;
import com.backbase.accesscontrol.domain.dto.CheckDataItemsPermissions;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemIds;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody;
import com.backbase.accesscontrol.util.ExceptionUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
public class UserContextServiceTest {

    @Mock
    private UserContextJpaRepository userContextJpaRepository;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private CombinationConfig combinationConfig;
    @Mock
    private FunctionGroupService functionGroupService;
    @Captor
    private ArgumentCaptor<UserContext> userContextCaptor;
    @InjectMocks
    private UserContextService userContextService;

    private Page<ServiceAgreement> serviceAgreements;
    private String serviceAgreementId;
    private String functionGroupId;
    private Integer from;
    private Integer size;

    @Before
    public void setUp() {
        serviceAgreementId = "s_id";
        functionGroupId = "f_id";
        from = 0;
        size = 20;

        serviceAgreements = getServiceAgreements();

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setId("d_id");
    }

    @Test
    public void shouldReturnUserAccessIfOneAlreadyExists() {
        UserContext userContext = new UserContext("userId", "serviceAgreementId");
        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userContext.getUserId()), eq(userContext.getServiceAgreementId())))
            .thenReturn(Optional.of(userContext));

        UserContext responseUserAccess = userContextService
            .getOrCreateUserContext(userContext.getUserId(), userContext.getServiceAgreementId());
        assertEquals(userContext, responseUserAccess);
    }

    @Test
    public void shouldCreateUserAccessIfOneDoesNotExist() {
        UserContext userContext = new UserContext("userId", "serviceAgreementId");
        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userContext.getUserId()), eq(userContext.getServiceAgreementId())))
            .thenReturn(Optional.empty());
        when(userContextJpaRepository.save(any(UserContext.class)))
            .thenReturn(userContext);

        UserContext responseUserContext = userContextService
            .getOrCreateUserContext(userContext.getUserId(), userContext.getServiceAgreementId());
        assertEquals(userContext, responseUserContext);

        verify(userContextJpaRepository).save(userContextCaptor.capture());

        UserContext returnedUserContext = userContextCaptor.getValue();
        assertEquals(userContext.getUserId(), returnedUserContext.getUserId());
        assertEquals(userContext.getServiceAgreementId(), returnedUserContext.getServiceAgreementId());
    }

    @Test
    public void testGetUserContextByUserId() {
        String userId = "user1";
        when(serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions(anyString(), anyString(), any()))
            .thenReturn(getServiceAgreements());

        UserContextsGetResponseBody response = userContextService.getUserContextsByUserId(userId, "s", 0, 10);
        assertEquals(1, response.getElements().size());

        Element element = response.getElements().get(0);

        ServiceAgreement serviceAgreement = serviceAgreements.getContent().get(0);
        assertEquals(serviceAgreement.getName(), element.getServiceAgreementName());
        assertEquals(serviceAgreement.getId(), element.getServiceAgreementId());
        assertEquals(serviceAgreement.getExternalId(), element.getExternalId());
        assertEquals(serviceAgreement.getDescription(), element.getDescription());
        assertEquals(serviceAgreement.isMaster(), element.getServiceAgreementMaster());
    }

    @Test
    public void throwInvalidUserContextExceptionOnInvalidUserContext() {
        String userId = "user1";
        String serviceAgreementId = "sa1";

        when(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, serviceAgreementId))
            .thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextService.validateUserContext(userId, serviceAgreementId));
        assertThat(exception, new ForbiddenErrorMatcher("Invalid user context for user id " + userId, null));
    }

    @Test
    public void shouldCallValidationMethodWithoutLegalEntityWhenLEisNotProvided() {
        String userId = "user-001";
        String serviceAgreementId = "SA-001";

        when(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, serviceAgreementId))
            .thenReturn(true);

        userContextService.validateUserContext(userId, serviceAgreementId);

        verify(serviceAgreementJpaRepository).existContextForUserIdAndServiceAgreementId(userId, serviceAgreementId);
    }

    @Test
    public void testGetDataItemsPermissions() {

        String internalUserId = "user-001";
        String serviceAgreementFromContext = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("PAYEE_ID_1");
        dataItemIds3.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(userContextJpaRepository
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext)))
            .thenReturn(Collections.singletonList(new CheckDataItemsPermissions(15L, 2L, 2L)));
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(Sets.newHashSet("ARRANGEMENTS"));

        userContextService
            .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);

        verify(userContextJpaRepository, times(0))
            .checkIfPredefinedTypesAreInCombination(eq(15L), anySet());
        verify(userContextJpaRepository, times(1))
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext));
    }

    @Test
    public void testGetDataItemsPermissionsMultipleRecords() {

        String internalUserId = "user-001";
        String serviceAgreementFromContext = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("ARRANGEMENTS");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("CONTACT_ID_1");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(userContextJpaRepository
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext)))
            .thenReturn(asList(new CheckDataItemsPermissions(15L, 2L, 2L)
                , new CheckDataItemsPermissions(16L, 3L, 3L)));
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);

        userContextService
            .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);

        verify(userContextJpaRepository, times(0))
            .checkIfPredefinedTypesAreInCombination(eq(15L), anySet());
        verify(userContextJpaRepository, times(1))
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext));
    }

    @Test
    public void testGetDataItemsPermissionsNoNotOptionalInUniqueTypes() {

        String internalUserId = "user-001";
        String serviceAgreementFromContext = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("CONTACTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("ARRANGEMENTS");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("CONTACT_ID_1");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(userContextJpaRepository
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext)))
            .thenReturn(Collections.singletonList(new CheckDataItemsPermissions(15L, 1L, 1L)));
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);

        userContextService
            .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);

        verify(userContextJpaRepository, times(0))
            .checkIfPredefinedTypesAreInCombination(eq(15L), anySet());
        verify(userContextJpaRepository, times(1))
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId),
                eq(serviceAgreementFromContext));
    }

    @Test
    public void testGetDataItemsPermissionsEmptyListWithNotOptional() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("ARRANGEMENTS");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        dataItemsPermissions.setFunctionName("CONTACTS");
        dataItemsPermissions.setPrivilege("create");
        Set<String> afpIds = Sets.newHashSet("3");
        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(anyString(),
            eq(null), any())).thenReturn(afpIds);
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa)
        );
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    @Test
    public void testGetDataItemsPermissionsWrongPrivilege() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa)
        );
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_114.getErrorMessage(), ERR_AG_114.getErrorCode()));
    }

    @Test
    public void testGetDataItemsPermissionsEmptyListAndNoCombinationsWithoutNotOptionalTypes() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("TYPE_X","TYPE_Y");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("CONTACT_ID_1");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);
        when(userContextJpaRepository
            .checkIfPermissionIsAssignedWithoutDataGroups(anySet(),anyString(),anyString()))
            .thenReturn(true);

       userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa);

        verify(userContextJpaRepository, times(0))
            .checkIfPredefinedTypesAreInCombination(any(), anySet());
        verify(userContextJpaRepository, times(1))
            .checkIfPermissionIsAssignedWithoutDataGroups(eq(afpIds), eq(internalUserId),
                eq(sa));
    }

    @Test
    public void testGetDataItemsPermissionsEmptyListWithCombinationsAndWithoutNotOptionalTypes() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("TYPE_X");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        dataItemsPermissions.setFunctionName("CONTACTS");
        dataItemsPermissions.setPrivilege("create");
        Set<String> afpIds = Sets.newHashSet("3");
        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(anyString(),
            eq(null), any())).thenReturn(afpIds);
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);
        when(userContextJpaRepository
            .checkIfPermissionIsAssignedWithoutDataGroups(anySet(),anyString(),anyString()))
            .thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa)
        );
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    @Test
    public void testGetDataItemsPermissionsUniqueTypesAndCheckSumNotEqual() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("CONTACTS", "PAYEES");
        Set<String> notOptionalTypes = Sets.newHashSet("ARRANGEMENTS");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("CONTACT_ID_1");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(userContextJpaRepository
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId), eq(sa)))
            .thenReturn(Collections.singletonList(new CheckDataItemsPermissions(15L, 2L, 1L)));
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalTypes);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa)
        );
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    @Test
    public void testGetDataItemsPermissionsNotOptionalTypesNotInCombination() {

        String internalUserId = "user-001";
        String sa = "SA-001";
        String fgName = "SEPA";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES", "CONTACTS");
        HashSet<String> notOptionalWhenRequested = Sets.newHashSet("ARRANGEMENTS");
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("CONTACTS_ID_1");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName(fgName);
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        Set<String> afpIds = Sets.newHashSet("3");

        when(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(fgName),
            eq(null), any())).thenReturn(afpIds);
        when(userContextJpaRepository
            .findDataItemsPermissions(eq(dataItemsPermissions), eq(afpIds), eq(internalUserId), eq(sa)))
            .thenReturn(Collections.singletonList(new CheckDataItemsPermissions(15L, 2L, 2L)));
        when(combinationConfig.getNotOptionalWhenRequested()).thenReturn(notOptionalWhenRequested);
        when(userContextJpaRepository.checkIfPredefinedTypesAreInCombination(eq(15L), eq(notOptionalWhenRequested)
        )).thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextService
                .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, sa)
        );
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    @Test
    public void testGetUserContextListByDataGroupId() {
        UserContextProjection userContext1 = new UserContextProjection("userId1", "agreementId1");
        UserContextProjection userContext2 = new UserContextProjection("userId2", "agreementId2");
        when(userContextJpaRepository.findAllUserContextsByAssignDataGroupId("dataGroupId"))
                .thenReturn(Arrays.asList(userContext1, userContext2));

        List<UserContextProjection> userContexts = userContextService.getUserContextListByDataGroupId("dataGroupId");

        List<String> userIds = userContexts.stream()
               .map(userContext -> userContext.getUserId()).collect(Collectors.toList());
        Assertions.assertThat(userIds).containsExactlyInAnyOrder("userId1", "userId2");
    }

    @Test
    public void testGetUserContextListByFunctionGroupId() {
        UserContextProjection userContext1 = new UserContextProjection("userId1", "agreementId1");
        UserContextProjection userContext2 = new UserContextProjection("userId2", "agreementId2");
        when(userContextJpaRepository.findAllUserContextsByAssignFunctionGroupId("functionGroupId"))
                .thenReturn(Arrays.asList(userContext1, userContext2));

        List<UserContextProjection> userContexts = userContextService.getUserContextListByFunctionGroupId("functionGroupId");

        List<String> userIds = userContexts.stream()
                .map(userContext -> userContext.getUserId()).collect(Collectors.toList());
        Assertions.assertThat(userIds).containsExactlyInAnyOrder("userId1", "userId2");
    }

    @Test
    public void shouldSearchUserIdsByServiceAgreementIdAndFunctionGroupIdWhenFunctionGroupExists() {
        FunctionGroupsGetResponseBody functionGroup = new FunctionGroupsGetResponseBody().withId(functionGroupId);

        doReturn(Collections.singletonList(functionGroup))
            .when(functionGroupService)
            .getFunctionGroupsByServiceAgreementId(serviceAgreementId);

        userContextService
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(serviceAgreementId, functionGroupId, from, size);

        verify(userContextJpaRepository).findUserIdsByServiceAgreementIdAndFunctionGroupId(
            serviceAgreementId, functionGroupId, PageRequest.of(from, size));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenServiceAgreementDoesNotExist() {
        doThrow(ExceptionUtil.getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()))
            .when(functionGroupService)
            .getFunctionGroupsByServiceAgreementId(serviceAgreementId);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userContextService
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(serviceAgreementId, functionGroupId, from, size));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenFunctionGroupDoesNotExistInServiceAgreement() {
        String requestedFunctionGroupId = "fg_1";
        String foundFunctionGroupId = "fg_2";
        FunctionGroupsGetResponseBody functionsGetResponseBody = new FunctionGroupsGetResponseBody().withId(foundFunctionGroupId);
        doReturn(Collections.singletonList(functionsGetResponseBody))
            .when(functionGroupService)
            .getFunctionGroupsByServiceAgreementId(serviceAgreementId);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userContextService
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(serviceAgreementId, requestedFunctionGroupId, from, size));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    private Page<ServiceAgreement> getServiceAgreements() {

        List<ServiceAgreement> serviceAgreements = new ArrayList<>();
        LegalEntity creatorEntity = getLegalEntity("0", "LE0");
        ServiceAgreement serviceAgreement = getServiceAgreement("1", "saName1", creatorEntity,
            "random_external_id", "Some description");

        LegalEntity legalEntity1 = getLegalEntity("1", "LE1");
        Participant provider1 = getProvider("p1", legalEntity1);
        provider1.addParticipantUser("user1");
        provider1.setServiceAgreement(serviceAgreement);

        serviceAgreement.getParticipants().put(legalEntity1.getId(), provider1);

        LegalEntity legalEntity2 = getLegalEntity("2", "LE2");
        Participant consumer = getConsumer("p2", legalEntity2);
        consumer.setServiceAgreement(serviceAgreement);

        serviceAgreements.add(serviceAgreement);

        return new PageImpl<>(serviceAgreements, getPageableObjWithoutSortingObject(0, 10), 1);
    }

    private LegalEntity getLegalEntity(String id, String name) {
        LegalEntity creatorEntity = new LegalEntity();
        creatorEntity.setId(id);
        creatorEntity.setName(name);
        return creatorEntity;
    }

    private ServiceAgreement getServiceAgreement(String id, String name, LegalEntity creatorEntity, String externalId,
        String description) {
        ServiceAgreement serviceAgreement1 = new ServiceAgreement();
        serviceAgreement1.setId(id);
        serviceAgreement1.setName(name);
        serviceAgreement1.setCreatorLegalEntity(creatorEntity);
        serviceAgreement1.setExternalId(externalId);
        serviceAgreement1.setDescription(description);
        return serviceAgreement1;
    }

    private Participant getConsumer(String consumerid, LegalEntity legalEntity) {
        Participant participant = new Participant();
        participant.setId(consumerid);
        participant.setLegalEntity(legalEntity);
        participant.setShareAccounts(true);
        return participant;
    }

    private Participant getProvider(String id, LegalEntity legalEntity) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setLegalEntity(legalEntity);
        participant.setShareUsers(true);
        return participant;
    }

    private PageRequest getPageableObjWithoutSortingObject(int from, int size) {
        return PageRequest.of(from, size);
    }
}