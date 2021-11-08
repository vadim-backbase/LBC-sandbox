package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.FUNCTION_GROUP_WITH_SA;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_111;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_001;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_002;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_033;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_071;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_072;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_084;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_049;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_061;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_068;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_069;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_076;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.mappers.ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.SelfApprovalPolicyMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantUserJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.ApprovalSelfApprovalPolicyFactory;
import com.backbase.accesscontrol.util.SelfApprovalPolicyFactory;
import com.backbase.accesscontrol.util.UserContextPermissionsFactory;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.validation.UserContextPermissionsSelfApprovalPolicyValidator;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionsServiceImplTest {

    @InjectMocks
    private PermissionsServiceImpl permissionsService;

    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Mock
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;

    @Mock
    private UserContextJpaRepository userContextJpaRepository;

    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Mock
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;

    @Mock
    private ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    @Mock
    private EntityManager entityManager;

    @Mock
    private ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    @Mock
    private ParticipantUserJpaRepository participantUserJpaRepository;
    @Mock
    private ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    @Mock
    private UserContextPermissionsSelfApprovalPolicyValidator selfApprovalPolicyValidator;
    @Mock
    private SelfApprovalPolicyFactory selfApprovalPolicyFactory;
    @Mock
    private ApprovalSelfApprovalPolicyFactory approvalSelfApprovalPolicyFactory;
    @Mock
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Spy
    private SelfApprovalPolicyMapper selfApprovalPolicyMapper = Mappers.getMapper(SelfApprovalPolicyMapper.class);
    @Spy
    private ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper userContextPermissionsMapper =
            Mappers.getMapper(ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper.class);
    @Spy
    private UserContextPermissionsFactory userContextPermissionsFactory = new UserContextPermissionsFactory(selfApprovalPolicyMapper);

    @Captor
    private ArgumentCaptor<List<UserAssignedFunctionGroup>> uafgListCaptor = ArgumentCaptor.forClass(List.class);

    @Captor
    private ArgumentCaptor<Set<UserAssignedFunctionGroup>> uafgSetCaptor = ArgumentCaptor.forClass(Set.class);

    @Captor
    private ArgumentCaptor<ApprovalUserContext> approvalUserContextCaptor = ArgumentCaptor
        .forClass(ApprovalUserContext.class);

    private Random userAssignedFunctionGroupIdGen = new Random(Long.MAX_VALUE);

    @Test
    void shouldUpdateUserPermissionsUnderMasterServiceAgreement() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1, functionGroup2));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(new ArrayList<>());
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        UserContext userContext1 = new UserContext(1L, userId, serviceAgreement.getId(), Sets.newHashSet());

        when(userContextJpaRepository.findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId())))
            .thenReturn(Optional.of(userContext1));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));
        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .saveAll(uafgListCaptor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreement.getId()));

        assertEquals(userId, userContext1.getUserId());
        assertEquals(serviceAgreement.getId(), userContext1.getServiceAgreementId());

        List<UserAssignedFunctionGroup> userAssignedFunctionGroups = uafgListCaptor.getValue();
        assertEquals(2, userAssignedFunctionGroups.size());
        assertTrue(containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext1, functionGroup1,
            asList(dataGroup1, dataGroup2)));
        assertTrue(containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext1, functionGroup2,
            singletonList(dataGroup3)));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupIsMixedWitAndWithoutCombinationOfDataGroups() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setId("id.dg1");
        serviceAgreement.getFunctionGroups().add(functionGroup);
        serviceAgreement.getDataGroups().add(dataGroup);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup.getId(), null,
            singletonList(dataGroup.getId()), new ArrayList<>());

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup.getName(),
            new ArrayList<>(), new ArrayList<>()).withDataGroupIdentifiers(null);

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertEquals(ERR_AG_111.getErrorMessage(), exception.getErrors().get(0).getMessage());
        assertEquals(ERR_AG_111.getErrorCode(), exception.getErrors().get(0).getKey());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupTemplatesIsMixedWithAndWithoutCombinationOfDataGroups() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.TEMPLATE,
                null);
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setId("id.dg1");
        serviceAgreement.getDataGroups().add(dataGroup);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            "saForTemplate",
            null, functionGroup.getName(),
            singletonList(dataGroup.getId()), new ArrayList<>());

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            "saForTemplate",
            functionGroup.getId(), null,
            new ArrayList<>(), new ArrayList<>()).withDataGroupIdentifiers(null);

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        when(functionGroupJpaRepository
            .findByServiceAgreementExternalIdAndName(eq("saForTemplate"), eq(functionGroup.getName())))
                .thenReturn(Optional.of(functionGroup));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertEquals(ERR_AG_111.getErrorMessage(), exception.getErrors().get(0).getMessage());
        assertEquals(ERR_AG_111.getErrorCode(), exception.getErrors().get(0).getKey());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateUserPermissionsUserNotBelongInServiceAgreement() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity11", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            "12",
            serviceAgreement.getExternalId(),
            "legalEntity123",
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception,
            new BadRequestErrorMatcher(ERR_ACC_033.getErrorMessage().replace("?", "12"), ERR_ACC_033.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenUpdateUserPermissionsUserNotBelongInServiceAgreementCustom() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity11", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(false);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            "12",
            serviceAgreement.getExternalId(),
            "legalEntity123",
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception,
            new BadRequestErrorMatcher(ERR_ACC_033.getErrorMessage().replace("?", "12"), ERR_ACC_033.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenUpdateUserPermissionsWhenNonExistingFunctionGroup() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");

        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");

        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, "non-existing",
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));

    }

    @Test
    void shouldThrowBadRequestWhenUpdateUserPermissionsWhenServiceAgreementMissMatch() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");

        ServiceAgreement serviceAgreementNew = createServiceAgreement("name.sa1", "id.external.sa1", "desc1",
            legalEntity, null, null);
        serviceAgreementNew.setMaster(true);
        serviceAgreementNew.setId("SA-011");

        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreementNew);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        serviceAgreement.setFunctionGroups(singleton(functionGroup2));
        serviceAgreementNew.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        mockGetFunctionGroupTemplateById(functionGroup1.getId(), Optional.of(functionGroup1));

        UserContext userContext1 = new UserContext(1L, userId, serviceAgreement.getId(), Sets.newHashSet());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_069.getErrorMessage(), ERR_ACQ_069.getErrorCode()));

    }

    @Test
    void shouldThrowErrorWhenUpdatingUserPermissionsOfTemplateJobRoleWithWrongApsType() {
        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.TEMPLATE,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.setDataGroups(Sets.newHashSet(dataGroup1, dataGroup2, dataGroup3));

        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null,
            functionGroup1.getName(),
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_068.getErrorMessage(), ERR_ACQ_068.getErrorCode()));
    }

    @Test
    void shouldVerifyThatValidationHierarchyServiceIsCalledWhenUpdatingPermissionsOnJobRoleTemplate() {
        Set<AssignablePermissionSet> aps = new HashSet<>();
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("Test1");
        assignablePermissionSet.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
        assignablePermissionSet.setId(5362L);
        aps.add(assignablePermissionSet);
        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity1", "name1", "id.external1", null,
            LegalEntityType.BANK);
        LegalEntity legalEntity2 = createLegalEntity("id.legalentity2", "name2", "id.external2", legalEntity,
            LegalEntityType.CUSTOMER);
        ServiceAgreement serviceAgreement1 =
            createServiceAgreement("name.sa1", "id.external.sa1", "desc", legalEntity,
                null, null);
        serviceAgreement1.setMaster(true);
        serviceAgreement1.setId("SA-01");
        serviceAgreement1.setCreatorLegalEntity(legalEntity);
        serviceAgreement1.setPermissionSetsRegular(aps);
        ServiceAgreement serviceAgreement2 =
            createServiceAgreement("name.sa2", "id.external.sa2", "desc", legalEntity2,
                null, null);
        serviceAgreement2.setMaster(true);
        serviceAgreement2.setId("SA-02");
        serviceAgreement2.setCreatorLegalEntity(legalEntity2);
        serviceAgreement2.setPermissionSetsRegular(aps);
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(),
                FunctionGroupType.TEMPLATE,
                serviceAgreement2);
        functionGroup1.setAssignablePermissionSet(assignablePermissionSet);
        serviceAgreement1.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement1);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement1);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement1);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement1.getDataGroups().add(dataGroup1);
        serviceAgreement1.getDataGroups().add(dataGroup2);
        serviceAgreement1.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement1.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement1.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement1);

        UserContext userContext1 = new UserContext(1L, userId, serviceAgreement1.getId(), Sets.newHashSet());

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement1.getId()))).thenReturn(
                Optional.of(userContext1));

        permissionsService.updateUserPermission(permissionsPutRequestBody);
        verify(validateLegalEntityHierarchyService)
            .validateLegalEntityAncestorHierarchy(eq(legalEntity.getId()), eq(newHashSet(legalEntity2.getId())));

    }


    @Test
    void shouldThrowErrorWhenUpdatingUserPermissionsWhenFgDoesNotExist() {
        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.TEMPLATE,
                serviceAgreement);

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");

        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldUpdateUserPermissionsWithEmptyDataGroupIdentifiers() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1, functionGroup2));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.setDataGroups(Sets.newHashSet(dataGroup1, dataGroup2, dataGroup3));

        PresentationIdentifier functionGroupIdentifier = createEntityIdentifier(functionGroup1.getId(),
            functionGroup1.getName(),
            functionGroup1.getServiceAgreement().getExternalId());
        PresentationFunctionGroupDataGroup functionDataPair1 = new PresentationFunctionGroupDataGroup()
            .withFunctionGroupIdentifier(functionGroupIdentifier);

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        UserContext userContext1 = new UserContext(1L, userId, serviceAgreement.getId(), Sets.newHashSet());

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()))).thenReturn(
                Optional.of(userContext1));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));

        verify(userAssignedFunctionGroupJpaRepository).saveAll(uafgListCaptor.capture());

        assertEquals(userId, userContext1.getUserId());
        assertEquals(serviceAgreement.getId(), userContext1.getServiceAgreementId());

        List<UserAssignedFunctionGroup> userAssignedFunctionGroups = uafgListCaptor.getValue();

        assertEquals(1, userAssignedFunctionGroups.size());
        assertTrue(
            containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext1, functionGroup1, emptyList()));
    }

    @Test
    void shouldUpdateUserPermissionsWithEmptyFunctionGroupIdentifiers() {

        String userId = "user_id";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(new ArrayList<>());
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            emptyList());

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);
        mockGetUserContext(userId, serviceAgreement.getId());

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));
    }

    @Test
    void shouldUpdateUserPermissionsUnderMasterServiceAgreementRemoveAlreadyAssignedFunctionGroup() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1, functionGroup2));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);

        UserContext userContext = new UserContext(1L, userId, serviceAgreement.getId(), Sets.newHashSet());

        UserAssignedFunctionGroup userAssignedFunctionGroupToRemove = new UserAssignedFunctionGroup(functionGroup2,
            userContext);
        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup(functionGroup1,
            userContext);
        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination =
            new UserAssignedFunctionGroupCombination(
                Sets.newHashSet(dataGroup1.getId()), userAssignedFunctionGroup1);
        userAssignedFunctionGroup1
            .setUserAssignedFunctionGroupCombinations(newHashSet(userAssignedFunctionGroupCombination));

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(new ArrayList<>());

        when(userAssignedFunctionGroupJpaRepository.findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
            eq(userId),
            eq(serviceAgreement.getId()),
            eq(asList(FunctionGroupType.DEFAULT, FunctionGroupType.TEMPLATE))))
                .thenReturn(new ArrayList<UserAssignedFunctionGroup>() {
                    {
                        add(userAssignedFunctionGroupToRemove);
                        add(userAssignedFunctionGroup1);
                    }
                });

        when(userContextJpaRepository.findByUserIdAndServiceAgreementId(
            eq(userId), eq(serviceAgreement.getId()))).thenReturn(Optional.of(userContext));

        when(approvalFunctionGroupRefJpaRepository
            .findByFunctionGroupIdIn(any())).thenReturn(Optional.empty());
        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));
        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .deleteAll(uafgSetCaptor.capture());

        Set<UserAssignedFunctionGroup> userAssignedFunctionGroups = uafgSetCaptor.getValue();
        assertEquals(1, userAssignedFunctionGroups.size());
        assertTrue(containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext, functionGroup2,
            singletonList(dataGroup3)));
    }

    @Test
    void shouldThrowBadRequestWhenServiceAgreementDoesNotExist() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            new ArrayList<>(),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        when(serviceAgreementJpaRepository.findByExternalId(eq(serviceAgreement.getExternalId()), anyString()))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenUserDoesNotBelongToServiceAgreement() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(false);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            new ArrayList<>(),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(serviceAgreement.getExternalId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception,
            new BadRequestErrorMatcher("User with id " + userId + " does not belong in service agreement",
                ERR_ACC_033.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenServiceAgreementIsMasterAndCreatorIsNotValid() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        LegalEntity creatorLegalEntity = createLegalEntity("id.legalentity.creator", "name le 1", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc",
            creatorLegalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            new ArrayList<>(),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(serviceAgreement.getExternalId(), serviceAgreement);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception,
            new BadRequestErrorMatcher("User with id " + userId + " does not belong in service agreement",
                ERR_ACC_033.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestOnUpdatePermissionsWithNonExistingDataGroup() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroupToRemove = DataGroupUtil
            .createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroupToStayAssigned = DataGroupUtil
            .createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroupToRemove.setId("id.dg1");
        dataGroupToStayAssigned.setId("id.dg2");

        serviceAgreement.setDataGroups(Sets.newHashSet(dataGroupToRemove, dataGroupToStayAssigned));

        UserContext userContext = new UserContext(userId, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup(functionGroup1,
            userContext);
        UserAssignedFunctionGroupCombination assignedFunctionGroupDataGroup1 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroupToRemove.getId()), userAssignedFunctionGroup1);
        UserAssignedFunctionGroupCombination assignedFunctionGroupDataGroup2 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroupToStayAssigned.getId()), userAssignedFunctionGroup1);
        userAssignedFunctionGroup1.setUserAssignedFunctionGroupCombinations(
            newHashSet(assignedFunctionGroupDataGroup1, assignedFunctionGroupDataGroup2));

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            asList(dataGroupToStayAssigned.getId(), "non-existing"),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    void shouldUpdateUserPermissionsUnderMasterServiceAgreementRemoveDataGroups() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(singleton(functionGroup1));

        DataGroup dataGroupToRemove = DataGroupUtil
            .createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroupToStayAssigned = DataGroupUtil
            .createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroupToAssign = DataGroupUtil
            .createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroupToRemove.setId("id.dg1");
        dataGroupToStayAssigned.setId("id.dg2");
        dataGroupToAssign.setId("id.dg3");

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(new ArrayList<>());
        serviceAgreement.getDataGroups().add(dataGroupToRemove);
        serviceAgreement.getDataGroups().add(dataGroupToAssign);
        serviceAgreement.getDataGroups().add(dataGroupToStayAssigned);

        UserContext userContext = new UserContext(userId, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup(functionGroup1,
            userContext);

        UserAssignedFunctionGroupCombination assignedFunctionGroupDataGroup1 =
            new UserAssignedFunctionGroupCombination();
        assignedFunctionGroupDataGroup1.setDataGroupIds(Sets.newHashSet(dataGroupToRemove.getId()));
        assignedFunctionGroupDataGroup1.setUserAssignedFunctionGroup(userAssignedFunctionGroup1);
        UserAssignedFunctionGroupCombination assignedFunctionGroupDataGroup2 =
            new UserAssignedFunctionGroupCombination();
        assignedFunctionGroupDataGroup1.setDataGroupIds(Sets.newHashSet(dataGroupToStayAssigned.getId()));
        assignedFunctionGroupDataGroup1.setUserAssignedFunctionGroup(userAssignedFunctionGroup1);

        userAssignedFunctionGroup1.setUserAssignedFunctionGroupCombinations(
            newHashSet(assignedFunctionGroupDataGroup1, assignedFunctionGroupDataGroup2));

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afp_id");

        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setFunctionGroup(functionGroup1);
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(applicableFunctionPrivilege.getId());
        functionGroup1.setPermissions(new HashSet<>(singletonList(groupedFunctionPrivilege)));

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            asList(dataGroupToStayAssigned.getId(), dataGroupToAssign.getId()),
            new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPair1));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        when(userAssignedFunctionGroupJpaRepository.findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
            eq(userId),
            eq(serviceAgreement.getId()),
            eq(asList(FunctionGroupType.DEFAULT, FunctionGroupType.TEMPLATE))))
                .thenReturn(new ArrayList<>(singletonList(userAssignedFunctionGroup1)));

        when(userContextJpaRepository.findByUserIdAndServiceAgreementId(
            eq(userId), eq(serviceAgreement.getId()))).thenReturn(Optional.of(userContext));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));
        verify(userAssignedFunctionGroupJpaRepository, times(0)).delete(any(UserAssignedFunctionGroup.class));
        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .saveAll(uafgListCaptor.capture());

        List<UserAssignedFunctionGroup> changedFunctionGroup = uafgListCaptor.getValue();
        List<String> assignedDgsAfterChange = changedFunctionGroup.get(0).getUserAssignedFunctionGroupCombinations()
            .stream()
            .flatMap(item -> item.getDataGroups().stream())
            .map(DataGroup::getId)
            .collect(Collectors.toList());
        assertFalse(assignedDgsAfterChange.contains(dataGroupToRemove.getId()));
    }

    @Test
    void shouldUpdatePermissionsAndRemoveOnlyRelatedSelfApprovalPoliciesFromCombinationWhenDataGroupsAreAssigned() {
        String userId = "userId";
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId("leId");
        legalEntity.setName("leName");
        legalEntity.setExternalId("leExternalId");
        legalEntity.setType(LegalEntityType.BANK);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("saExternalId");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId("fgId");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);

        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("dg1Name", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("dg2Name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("dg1");
        dataGroup2.setId("dg2");

        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(Collections.emptyList());

        UserContext userContext = new UserContext(userId, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        UserAssignedFunctionGroupCombination assignedFunctionGroupCombination = new UserAssignedFunctionGroupCombination();
        assignedFunctionGroupCombination.setDataGroupIds(Sets.newHashSet(dataGroup1.getId(), dataGroup2.getId()));
        assignedFunctionGroupCombination.setUserAssignedFunctionGroup(userAssignedFunctionGroup);
        com.backbase.accesscontrol.domain.SelfApprovalPolicy policy = new com.backbase.accesscontrol.domain.SelfApprovalPolicy();
        assignedFunctionGroupCombination.getSelfApprovalPolicies().add(policy);

        userAssignedFunctionGroup.setUserAssignedFunctionGroupCombinations(Sets.newHashSet(assignedFunctionGroupCombination));

        PresentationFunctionGroupDataGroup functionDataPairs = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup.getId(), null,
            List.of(dataGroup1.getId(), dataGroup2.getId()),
            Collections.emptyList());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            Collections.singletonList(functionDataPairs));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        when(userAssignedFunctionGroupJpaRepository.findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
            userId, serviceAgreement.getId(), List.of(FunctionGroupType.DEFAULT, FunctionGroupType.TEMPLATE)))
            .thenReturn(Collections.singletonList(userAssignedFunctionGroup));

        when(userContextJpaRepository.findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId()))
            .thenReturn(Optional.of(userContext));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1)).findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId());
        verify(userAssignedFunctionGroupJpaRepository, times(0)).delete(any(UserAssignedFunctionGroup.class));
        verify(userAssignedFunctionGroupJpaRepository, times(1)).saveAll(List.of(userAssignedFunctionGroup));

        assertThat(assignedFunctionGroupCombination.getSelfApprovalPolicies(), empty());
        assertThat(assignedFunctionGroupCombination.getDataGroupIds(), hasSize(2));
        assertThat(assignedFunctionGroupCombination.getDataGroupIds(), containsInAnyOrder("dg1", "dg2"));
    }

    @Test
    void shouldUpdatePermissionsAndRemoveCombinationWithSelfApprovalPoliciesWhenNoDataGroupsWerePreviouslyAssigned() {
        String userId = "userId";
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId("leId");
        legalEntity.setName("leName");
        legalEntity.setExternalId("leExternalId");
        legalEntity.setType(LegalEntityType.BANK);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("saExternalId");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId("fgId");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);

        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup));

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet())).thenReturn(new ArrayList<>());

        UserContext userContext = new UserContext(userId, serviceAgreement.getId());
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        UserAssignedFunctionGroupCombination assignedFunctionGroupCombination = new UserAssignedFunctionGroupCombination();
        assignedFunctionGroupCombination.setUserAssignedFunctionGroup(userAssignedFunctionGroup);
        com.backbase.accesscontrol.domain.SelfApprovalPolicy policy = new com.backbase.accesscontrol.domain.SelfApprovalPolicy();
        assignedFunctionGroupCombination.getSelfApprovalPolicies().add(policy);

        userAssignedFunctionGroup.setUserAssignedFunctionGroupCombinations(Sets.newHashSet(assignedFunctionGroupCombination));

        PresentationFunctionGroupDataGroup functionDataPairs = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup.getId(), null,
            Collections.emptyList(),
            Collections.emptyList());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            singletonList(functionDataPairs));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        when(userAssignedFunctionGroupJpaRepository.findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
            userId, serviceAgreement.getId(), List.of(FunctionGroupType.DEFAULT, FunctionGroupType.TEMPLATE)))
            .thenReturn(Collections.singletonList(userAssignedFunctionGroup));

        when(userContextJpaRepository.findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId()))
            .thenReturn(Optional.of(userContext));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1)).findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId());
        verify(userAssignedFunctionGroupJpaRepository, times(0)).delete(any(UserAssignedFunctionGroup.class));
        verify(userAssignedFunctionGroupJpaRepository, times(1)).saveAll(List.of(userAssignedFunctionGroup));

        assertThat(userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations(), empty());
    }

    @Test
    void shouldUpdateUserPermissionsUnderCustomServiceAgreement() {
        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            legalEntity.getId(), legalEntity.getId());
        serviceAgreement.setMaster(false);
        serviceAgreement.setId("SA-01");
        serviceAgreement.getParticipants().get(legalEntity.getId()).setShareUsers(true);
        serviceAgreement.getParticipants().get(legalEntity.getId()).addParticipantUser(userId);
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1, functionGroup2));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.setDataGroups(Sets.newHashSet(dataGroup1, dataGroup2, dataGroup3));

        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));

        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        UserContext userContext1 = new UserContext(1L, userId, serviceAgreement.getId(), Sets.newHashSet());

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()))).thenReturn(
                Optional.of(userContext1));

        permissionsService.updateUserPermission(permissionsPutRequestBody);

        verify(userContextJpaRepository, times(1))
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreement.getId()));
        verify(userAssignedFunctionGroupJpaRepository, times(1))
            .saveAll(uafgListCaptor.capture());

        assertEquals(userId, userContext1.getUserId());
        assertEquals(serviceAgreement.getId(), userContext1.getServiceAgreementId());

        List<UserAssignedFunctionGroup> userAssignedFunctionGroups = uafgListCaptor.getValue();
        assertEquals(2, userAssignedFunctionGroups.size());
        assertTrue(containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext1, functionGroup1,
            asList(dataGroup1, dataGroup2)));
        assertTrue(containsUserAssignedFunctionGroups(userAssignedFunctionGroups, userContext1, functionGroup2,
            singletonList(dataGroup3)));
    }

    @Test
    void shouldThrowBBBadRequestForInvalidFunctionGroup() {

        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);
        ServiceAgreement sa1 = new ServiceAgreement();
        sa.setId("sa_id1");
        sa.setMaster(false);
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        FunctionGroup functionGroupOther = FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa1);
        sa.setFunctionGroups(newHashSet(functionGroup));
        mockGetServiceAgreementById("sa_id", Optional.of(sa));

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId("other_fg_id");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "le",
                newHashSet(item)));

        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa_id"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_076.getErrorMessage(), ERR_ACQ_076.getErrorCode()));
    }

    @Test
    void shouldThrowBBBadRequestForInvalidFunctionGroupType() {

        String otherFgId = "other_fg_id";
        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.setFunctionGroups(newHashSet(functionGroup));
        mockGetServiceAgreementById("sa_id", Optional.of(sa));

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(functionGroupJpaRepository.readByIdIn(anyCollection(), anyString()))
            .thenReturn(singletonList(new FunctionGroup().withType(FunctionGroupType.DEFAULT)));

        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId(otherFgId);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "le",
                newHashSet(item)));

        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa_id"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_001.getErrorMessage(), ERR_ACC_001.getErrorCode()));
    }

    @Test
    void shouldThrowBBBadRequestForNotExposedUser() {
        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);

        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.setFunctionGroups(newHashSet(functionGroup));

        mockGetServiceAgreementById("sa_id", Optional.of(sa));

        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq("user_id"), eq(sa)))
            .thenReturn(false);

        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId("fg_id");
        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "le",
                newHashSet(item)));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode()));
    }

    @Test
    void shouldThrowBBBadRequestForUserIsNotInLegalEntityForMasterServiceAgreement() {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity("le", "le name", "le ext", null, LegalEntityType.BANK);

        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(true);
        sa.setCreatorLegalEntity(legalEntity);

        sa.setFunctionGroups(newHashSet(FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa)));

        mockGetServiceAgreementById("sa_id", Optional.of(sa));

        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId("fg_id");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "other_le",
                newHashSet(item)));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode()));
    }

    @Test
    void shouldThrowBBBadRequestForInvalidDataGroup() {

        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);

        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup("fg_id", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.setFunctionGroups(newHashSet(functionGroup));

        DataGroup dg = createDataGroup("dg_id", "dg1");
        sa.setDataGroups(newHashSet(dg));

        mockGetServiceAgreementById("sa_id", Optional.of(sa));
        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq("user_id"), eq(sa)))
            .thenReturn(true);

        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId("fg_id");
        item.setDataGroupIds(Sets.newHashSet("other_dg_id"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "le",
                newHashSet(item)));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_002.getErrorMessage(), ERR_ACC_002.getErrorCode()));
    }

    @Test
    void shouldRemovePermissionWhenUserIsNotAdmin() {
        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);
        sa.getFunctionGroups().add(new FunctionGroup().withId("fgId"));

        mockGetServiceAgreementById("sa_id", Optional.of(sa));
        UserContext userContext = new UserContext()
            .withUserAssignedFunctionGroups(newHashSet(new UserAssignedFunctionGroup()
                .withFunctionGroupId("fgId")
                .withFunctionGroup(new FunctionGroup().withId("fgId").withType(FunctionGroupType.DEFAULT))));

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(eq("user_id"), eq("sa_id")))
                .thenReturn(Optional.of(userContext));

        permissionsService
            .assignUserContextPermissions("sa_id", "user_id", "le",
                new HashSet<>());

        verify(userAssignedFunctionGroupJpaRepository).deleteAll(MockitoHamcrest
            .argThat(contains(hasProperty("functionGroupId", is("fgId")))));
        verify(userContextJpaRepository).delete(eq(userContext));
    }

    @Test
    void shouldUpdatePermissions() {

        AssignablePermissionSet defaultSet = new AssignablePermissionSet();
        defaultSet.setId(1l);

        ServiceAgreement sa = new ServiceAgreement();
        sa.setId("sa_id");
        sa.setMaster(false);
        sa.setCreatorLegalEntity(new LegalEntity().withId("le1"));
        sa.getPermissionSetsRegular().add(defaultSet);

        FunctionGroup functionGroupNew = FunctionGroupUtil
            .getFunctionGroup("fg_id_new", "fg_name", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.getFunctionGroups().add(functionGroupNew);

        FunctionGroup functionGroupModified = FunctionGroupUtil
            .getFunctionGroup("fg_id_modified", "fg_id_modified", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.getFunctionGroups().add(functionGroupModified);

        FunctionGroup functionGroupUnmodified = FunctionGroupUtil
            .getFunctionGroup("fg_id_unmodified", "fg_id_unmodified", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.getFunctionGroups().add(functionGroupUnmodified);

        FunctionGroup functionGroupRemoved = FunctionGroupUtil
            .getFunctionGroup("fg_id_removed", "fg_id_removed", null,
                new HashSet<>(), FunctionGroupType.DEFAULT, sa);
        sa.getFunctionGroups().add(functionGroupUnmodified);

        DataGroup dgNew = createDataGroup("dg_id_new", "dg_id_new");
        sa.getDataGroups().add(dgNew);

        DataGroup dgRemoved = createDataGroup("dg_id_removed", "dg_id_removed");
        sa.getDataGroups().add(dgRemoved);

        DataGroup dgUnmodified = createDataGroup("dg_id_unmodified", "dg_id_unmodified");
        sa.getDataGroups().add(dgUnmodified);

        UserContext userContext = new UserContext()
            .withUserId("user")
            .withServiceAgreementId("sa_id");

        UserAssignedFunctionGroup unmodified = new UserAssignedFunctionGroup()
            .withId(10l)
            .withFunctionGroupId("fg_id_unmodified");

        unmodified.getUserAssignedFunctionGroupCombinations().add(new UserAssignedFunctionGroupCombination(
            Sets.newHashSet("dg_id_unmodified"), unmodified));

        userContext.getUserAssignedFunctionGroups().add(unmodified);

        UserAssignedFunctionGroup modified = new UserAssignedFunctionGroup()
            .withFunctionGroupId("fg_id_modified");

        modified.getUserAssignedFunctionGroupCombinations().add(new UserAssignedFunctionGroupCombination(
            Sets.newHashSet("dg_id_removed"), modified));

        userContext.getUserAssignedFunctionGroups().add(modified);

        UserAssignedFunctionGroup removed = new UserAssignedFunctionGroup()
            .withFunctionGroupId("fg_id_removed");

        userContext.getUserAssignedFunctionGroups().add(removed);

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(
                eq(userContext.getUserId()), eq(userContext.getServiceAgreementId())))
                    .thenReturn(Optional.of(userContext));

        String fgTemplateId = "fg_add_template";
        Set<UserContextPermissions> newPermissionsState = Sets.newHashSet(
            new UserContextPermissions(
                "fg_id_unmodified", Sets.newHashSet("dg_id_unmodified"), Collections.emptySet()),
            new UserContextPermissions("fg_id_new", Sets.newHashSet("dg_id_new"), Collections.emptySet()),
            new UserContextPermissions(fgTemplateId, Collections.emptySet(), Collections.emptySet()),
            new UserContextPermissions("fg_id_modified", Sets.newHashSet("dg_id_new"), Collections.emptySet()));

        mockGetServiceAgreementById("sa_id", Optional.of(sa));
        when(participantUserJpaRepository
            .existsByUserIdAndParticipantServiceAgreement(eq(userContext.getUserId()), eq(sa))).thenReturn(true);
        when(functionGroupJpaRepository
            .readByIdIn(eq(Sets.newHashSet(fgTemplateId)), anyString()))
                .thenReturn(singletonList(new FunctionGroup()
                    .withAssignablePermissionSet(defaultSet)
                    .withId(fgTemplateId)
                    .withType(FunctionGroupType.TEMPLATE)
                    .withServiceAgreement(new ServiceAgreement()
                        .withCreatorLegalEntity(new LegalEntity().withId("bank"))
                        .withId("bankMSA"))));

        permissionsService.assignUserContextPermissions(sa.getId(), userContext.getUserId(), "le", newPermissionsState);

        verify(validateLegalEntityHierarchyService).validateLegalEntityAncestorHierarchy(eq("le1"),
            eq(Sets.newHashSet("bank")));

        UserAssignedFunctionGroup newModified = new UserAssignedFunctionGroup()
            .withFunctionGroupId("fg_id_modified");

        newModified.getUserAssignedFunctionGroupCombinations().add(new UserAssignedFunctionGroupCombination(
            Sets.newHashSet("dg_id_new"), modified));

        UserAssignedFunctionGroup added = new UserAssignedFunctionGroup()
            .withFunctionGroupId("fg_id_new");

        added.getUserAssignedFunctionGroupCombinations().add(new UserAssignedFunctionGroupCombination(
            Sets.newHashSet("dg_id_new"), modified));

        UserAssignedFunctionGroup template = new UserAssignedFunctionGroup()
            .withFunctionGroupId(fgTemplateId);

        verify(userContextJpaRepository).save(MockitoHamcrest.argThat(
            allOf(
                hasProperty("userId", is(userContext.getUserId())),
                hasProperty("userAssignedFunctionGroups", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(newModified.getFunctionGroupId())),
                        hasProperty("userAssignedFunctionGroupCombinations",
                            contains(hasProperty("dataGroupIds", contains(dgNew.getId()))))),
                    allOf(
                        hasProperty("functionGroupId", is(added.getFunctionGroupId())),
                        hasProperty("userAssignedFunctionGroupCombinations",
                            contains(hasProperty("dataGroupIds", contains(dgNew.getId()))))),
                    allOf(
                        hasProperty("functionGroupId", is(unmodified.getFunctionGroupId())),
                        hasProperty("userAssignedFunctionGroupCombinations",
                            contains(hasProperty("dataGroupIds", contains(dgUnmodified.getId())))),
                        hasProperty("id", is(unmodified.getId()))),
                    allOf(hasProperty("functionGroupId", is(template.getFunctionGroupId()))))),
                hasProperty("serviceAgreementId", is(userContext.getServiceAgreementId())))));
    }

    @Test
    void shouldAssignUserPermissionsApprovalUserUnderMasterServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroup(functionGroupId);
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.empty());

        permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
            userId, userLegalEntityId, approvalId, permissions);

        verify(approvalUserContextJpaRepository).save(approvalUserContextCaptor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));

        ApprovalUserContext value = approvalUserContextCaptor.getValue();

        assertThat(value, allOf(
            hasProperty("userId", is(userId)),
            hasProperty("serviceAgreementId", is(serviceAgreementId)),
            hasProperty("legalEntityId", is(userLegalEntityId)),
            hasProperty("approvalId", is(approvalId)),
            hasProperty("approvalUserContextAssignFunctionGroups", containsInAnyOrder(
                getApprovalUserContextAssignFunctionGroupMatcher(is(assignUserContextPermissions1.getFunctionGroupId()),
                    hasItems(
                        dataGroupId1,
                        dataGroupId2))))));
    }

    @Test
    void shouldAssignUserPermissionsApprovalUserUnderCustomServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        FunctionGroup functionGroup = createFunctionGroup(functionGroupId);
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(
            assignUserContextPermissions1);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq(userId),
            eq(serviceAgreement))).thenReturn(true);
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.empty());

        permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
            userId, userLegalEntityId, approvalId, permissions);

        verify(approvalUserContextJpaRepository).save(approvalUserContextCaptor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));

        ApprovalUserContext value = approvalUserContextCaptor.getValue();

        assertThat(value, allOf(
            hasProperty("userId", is(userId)),
            hasProperty("serviceAgreementId", is(serviceAgreementId)),
            hasProperty("legalEntityId", is(userLegalEntityId)),
            hasProperty("approvalId", is(approvalId)),
            hasProperty("approvalUserContextAssignFunctionGroups", containsInAnyOrder(
                getApprovalUserContextAssignFunctionGroupMatcher(is(assignUserContextPermissions1.getFunctionGroupId()),
                    hasItems(
                        dataGroupId1,
                        dataGroupId2))))));
    }

    @Test
    void shouldAssignUserPermissionsApprovalUserJobRoleTemplateUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        Set<AssignablePermissionSet> aps = new HashSet<>();
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("Test1");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
        assignablePermissionSet.setId(5362L);
        aps.add(assignablePermissionSet);
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroupWithAPS(functionGroupId, FunctionGroupType.TEMPLATE,
            assignablePermissionSet);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(aps);
        functionGroup.setServiceAgreement(serviceAgreement);
        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.empty());

        permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
            userId, userLegalEntityId, approvalId, permissions);

        verify(approvalUserContextJpaRepository).save(approvalUserContextCaptor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));

        ApprovalUserContext value = approvalUserContextCaptor.getValue();

        assertThat(value, allOf(
            hasProperty("userId", is(userId)),
            hasProperty("serviceAgreementId", is(serviceAgreementId)),
            hasProperty("legalEntityId", is(userLegalEntityId)),
            hasProperty("approvalId", is(approvalId)),
            hasProperty("approvalUserContextAssignFunctionGroups", containsInAnyOrder(
                getApprovalUserContextAssignFunctionGroupMatcher(is(assignUserContextPermissions1.getFunctionGroupId()),
                    hasItems(
                        dataGroupId1,
                        dataGroupId2))))));
    }

    @Test
    void shouldAssignUserPermissionsApprovalUserJobRoleTemplateUnderCSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        Set<AssignablePermissionSet> aps = new HashSet<>();
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("Test1");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
        assignablePermissionSet.setId(5362L);
        aps.add(assignablePermissionSet);
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroupWithAPS(functionGroupId, FunctionGroupType.TEMPLATE,
            assignablePermissionSet);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(false);
        serviceAgreement.setPermissionSetsRegular(aps);
        functionGroup.setServiceAgreement(serviceAgreement);
        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(
            assignUserContextPermissions1);

        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq(userId),
            eq(serviceAgreement))).thenReturn(true);
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.empty());

        permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
            userId, userLegalEntityId, approvalId, permissions);

        verify(approvalUserContextJpaRepository).save(approvalUserContextCaptor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));

        ApprovalUserContext value = approvalUserContextCaptor.getValue();

        assertThat(value, allOf(
            hasProperty("userId", is(userId)),
            hasProperty("serviceAgreementId", is(serviceAgreementId)),
            hasProperty("legalEntityId", is(userLegalEntityId)),
            hasProperty("approvalId", is(approvalId)),
            hasProperty("approvalUserContextAssignFunctionGroups", containsInAnyOrder(
                getApprovalUserContextAssignFunctionGroupMatcher(is(assignUserContextPermissions1.getFunctionGroupId()),
                    hasItems(
                        dataGroupId1,
                        dataGroupId2))))));
    }

    @Test
    void shouldGetBadRequestAssignUserPermissionsApprovalUserJobRoleTemplateWrongAps() {
        String serviceAgreementId = "SA-01";
        String serviceAgreementId1 = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        Set<AssignablePermissionSet> aps = new HashSet<>();
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("Test1");
        assignablePermissionSet.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
        assignablePermissionSet.setId(5362L);
        AssignablePermissionSet assignablePermissionSet2 = new AssignablePermissionSet();
        assignablePermissionSet2.setName("Test2");
        assignablePermissionSet2.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);
        assignablePermissionSet2.setId(1234L);
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroupWithAPS(functionGroupId, FunctionGroupType.TEMPLATE,
            assignablePermissionSet);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement1 = new ServiceAgreement();
        serviceAgreement1.setFunctionGroups(newHashSet(functionGroup));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);
        aps.add(assignablePermissionSet2);
        serviceAgreement.setPermissionSetsRegular(aps);
        functionGroup.setServiceAgreement(serviceAgreement1);
        when(functionGroupJpaRepository
            .readByIdIn(eq(Sets.newHashSet(functionGroupId)), eq(FUNCTION_GROUP_WITH_SA)))
                .thenReturn(singletonList(functionGroup));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId1,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_068.getErrorMessage(), ERR_ACQ_068.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void shouldThrowBadRequestWhenAlreadyPendingAssignmentForUserPermissionsApprovalUserUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        FunctionGroup functionGroup = createFunctionGroup(functionGroupId);
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq(userId),
            eq(serviceAgreement))).thenReturn(true);
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.of(new ApprovalUserContext()));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_072.getErrorMessage(), ERR_ACC_072.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    private void mockCheckIfApprovalAlreadyExists(String serviceAgreementId, String userId,
        Optional<ApprovalUserContext> value) {
        when(approvalUserContextJpaRepository.findByUserIdAndServiceAgreementId(userId, serviceAgreementId))
            .thenReturn(value);
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupDoesNotBelongOnSaAssignUserPermissionsApprovalUserUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroup(functionGroupId);
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_002.getErrorMessage(), ERR_ACC_002.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void shouldThrowBadRequestWhenDataGroupsDoesNotBelongOnSaAssignUserPermissionsApprovalUserUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_076.getErrorMessage(), ERR_ACQ_076.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void shouldThrowBadRequestWhenUserIsNotInMSASaAssignUserPermissionsApprovalUserUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        LegalEntity legalEntity = createLegalEntityWithId("LE-02");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void shouldThrowBadRequestWhenUserIsNotExposedInCSASaAssignUserPermissionsApprovalUserUnderMSA() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setMaster(false);
        serviceAgreement.addParticipant(new Participant(), userLegalEntityId, true, true);

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(
            assignUserContextPermissions1);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(participantUserJpaRepository.existsByUserIdAndParticipantServiceAgreement(eq(userId),
            eq(serviceAgreement))).thenReturn(false);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void shouldThrowBadRequestWhenServiceAgreementDoesNotExistAssignUserPermissionsApprovalUser() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";

        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(
            assignUserContextPermissions1);
        mockGetServiceAgreementById(serviceAgreementId, Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenTryingToAssignDataGroupThatHasPendingDelete() {
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String userLegalEntityId = "LE-01";
        String approvalId = "AP-01";
        String functionGroupId = "FG-01";
        String dataGroupId1 = "DG-01";
        String dataGroupId2 = "DG-02";
        LegalEntity legalEntity = createLegalEntityWithId(userLegalEntityId);
        FunctionGroup functionGroup = createFunctionGroup(functionGroupId);
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup(dataGroupId1, "dg1");
        DataGroup dataGroup2 = createDataGroup(dataGroupId2, "dg2");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);
        UserContextPermissions assignUserContextPermissions1 = new UserContextPermissions();
        assignUserContextPermissions1.setFunctionGroupId(functionGroupId);
        assignUserContextPermissions1.setDataGroupIds(Sets.newHashSet(dataGroupId1, dataGroupId2));
        Set<UserContextPermissions> permissions = newHashSet(assignUserContextPermissions1);

        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        mockCheckIfApprovalAlreadyExists(serviceAgreementId, userId, Optional.empty());

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet()))
            .thenReturn(singletonList(new ApprovalDataGroup()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.assignUserContextPermissionsApproval(serviceAgreementId,
                userId, userLegalEntityId, approvalId, permissions));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_084.getErrorMessage(), ERR_ACC_084.getErrorCode()));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    void getUserPermissionApprovalDetailsWithExistingPermissionsTest() {
        String approvalId = "approvalId";
        String userId = "userId";
        String legalEntityId = "legalEntityId";
        String serviceAgreementId = "serviceAgreementId";
        String fgId = "fgId";
        String dgId = "dgId";
        String dgId2 = "dgId2";
        String removedFgId = "removedFgId";
        String newFgId = "newFgId";
        String newFgTemplateId = "newFgTemplateId";
        String removedFgTemplateId = "removedFgTemplateId";

        FunctionGroup modifiedFunctionGroup = createFunctionGroup(fgId, "FgName1", "FgDesc1",
            FunctionGroupType.DEFAULT);
        FunctionGroup removedFunctionGroup = createFunctionGroup(removedFgId, "FgName2", "FgDesc2",
            FunctionGroupType.DEFAULT);
        FunctionGroup newFunctionGroup = createFunctionGroup(newFgId, "FgName3", "FgDesc3", FunctionGroupType.DEFAULT);
        FunctionGroup unModifiedFunctionGroup = createFunctionGroup("unModifiedId", "FgName4", "FgDesc4",
            FunctionGroupType.SYSTEM);

        FunctionGroup newFgTemplate = createFunctionGroup(newFgTemplateId, "FgName4", "FgDesc4",
            FunctionGroupType.TEMPLATE);
        FunctionGroup removedFgTemplate = createFunctionGroup(removedFgTemplateId, "FgName5", "FgDesc5",
            FunctionGroupType.TEMPLATE);

        DataGroup dataGroup1 = createDataGroup(dgId, "DgName1", "DgDesc1");
        dataGroup1.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup2 = createDataGroup(dgId2, "DgName2", "DgDesc2");
        dataGroup2.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup3 = createDataGroup("dgId3", "DgName3", "DgDesc3");
        dataGroup3.setDataItemType("ARRANGEMENTS");

        Set<ApprovalUserContextAssignFunctionGroup> newFgs = new HashSet<>();
        ApprovalUserContext approvalContext = new ApprovalUserContext()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withApprovalUserContextAssignFunctionGroups(newFgs);

        Set<String> appFgDg = newHashSet(dataGroup1.getId());

        Set<String> appFgDgModified = Sets.newHashSet(dataGroup1.getId(), dataGroup3.getId());

        ApprovalUserContextAssignFunctionGroup approvalFgUnmodified = getApprovalAssignedFgObject(
            unModifiedFunctionGroup, approvalContext, appFgDg);

        ApprovalUserContextAssignFunctionGroup approvalFgNewlyAdded = getApprovalAssignedFgObject(newFunctionGroup,
            approvalContext, appFgDg);

        ApprovalUserContextAssignFunctionGroup approvalFgTemplateNewlyAdded = getApprovalAssignedFgObject(newFgTemplate,
            approvalContext, emptySet());

        ApprovalUserContextAssignFunctionGroup approvalFgModified = getApprovalAssignedFgObject(modifiedFunctionGroup,
            approvalContext, appFgDgModified);

        ServiceAgreement serviceAgreement = getServiceAgreement(serviceAgreementId, modifiedFunctionGroup,
            removedFunctionGroup, newFunctionGroup,
            unModifiedFunctionGroup, dataGroup1, dataGroup2, dataGroup3);

        Set<UserAssignedFunctionGroup> oldFgs = new HashSet<>();

        UserContext userAcc = new UserContext()
            .withId(1L)
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withUserAssignedFunctionGroups(oldFgs);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup()
            .withFunctionGroup(createFunctionGroup(fgId, "FgName", "FgDesc", FunctionGroupType.DEFAULT))
            .withUserContext(userAcc);
        List<UserAssignedFunctionGroupCombination> userAssignedFgDgRemoved = getUserAssignedFunctionGroupDataGroups(
            dataGroup1, userAssignedFunctionGroup);
        List<UserAssignedFunctionGroupCombination> userAssignedFgDgUnmodified = getUserAssignedFunctionGroupDataGroups(
            dataGroup1, userAssignedFunctionGroup);

        List<UserAssignedFunctionGroupCombination> userAssignedFgDgModified = getUserAssignedFunctionGroupDataGroups(
            dataGroup1, dataGroup2, userAssignedFunctionGroup);

        UserAssignedFunctionGroup unmodified = getUserAssignedFunctionGroup(unModifiedFunctionGroup,
            userAssignedFgDgUnmodified, userAcc);

        UserAssignedFunctionGroup removed = getUserAssignedFunctionGroup(removedFunctionGroup, userAssignedFgDgRemoved,
            userAcc);

        UserAssignedFunctionGroup uaFgRemovedFgTemplate = getUserAssignedFunctionGroup(removedFgTemplate, emptyList(),
            userAcc);

        UserAssignedFunctionGroup modified = getUserAssignedFunctionGroup(modifiedFunctionGroup,
            userAssignedFgDgModified, userAcc);

        oldFgs.add(modified);
        oldFgs.add(removed);
        oldFgs.add(uaFgRemovedFgTemplate);
        oldFgs.add(unmodified);

        newFgs.add(approvalFgModified);
        newFgs.add(approvalFgNewlyAdded);
        newFgs.add(approvalFgTemplateNewlyAdded);
        newFgs.add(approvalFgUnmodified);

        Optional<UserContext> userContext = Optional.of(userAcc);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        when(approvalUserContextJpaRepository.findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId))
            .thenReturn(Optional.of(approvalContext));

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(approvalContext.getUserId(),
                serviceAgreementId)).thenReturn(userContext);

        when(functionGroupJpaRepository.readByIdIn(eq(newHashSet(newFgTemplateId)), eq(FUNCTION_GROUP_WITH_SA)))
            .thenReturn(singletonList(newFgTemplate));
        when(functionGroupJpaRepository.readByIdIn(eq(newHashSet(removedFgTemplateId)), eq(FUNCTION_GROUP_WITH_SA)))
            .thenReturn(singletonList(removedFgTemplate));

        PresentationPermissionsApprovalDetailsItem received = permissionsService
            .getUserPermissionApprovalDetails(approvalId);

        PresentationFunctionGroupsDataGroupsPair newlyAddedFgs = getFunctionGroupsDataGroupsPairs(
            newFunctionGroup, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair newlyAddedFgTemplates = getFunctionGroupsDataGroupsPairs(
            newFgTemplate, null);
        PresentationFunctionGroupsDataGroupsPair modifiedAsNewFgs = getFunctionGroupsDataGroupsPairsTwoDataGroups(
            modifiedFunctionGroup, dataGroup3, dataGroup1);

        PresentationFunctionGroupsDataGroupsPair modifiedAsRemoved1 = getFunctionGroupsDataGroupsPairs(
            modifiedFunctionGroup, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair modifiedAsRemoved2 = getFunctionGroupsDataGroupsPairs(
            modifiedFunctionGroup, dataGroup2);
        PresentationFunctionGroupsDataGroupsPair removedFgs = getFunctionGroupsDataGroupsPairs(
            removedFunctionGroup, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair removedFgTemplates = getFunctionGroupsDataGroupsPairs(
            removedFgTemplate, null);

        PresentationPermissionsApprovalDetailsItem expected = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withUserId(userId)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription(serviceAgreement.getDescription())
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName(serviceAgreement.getName())
            .withUnmodifiedFunctionGroups(
                singletonList(getFunctionGroupsDataGroupsPairs(unModifiedFunctionGroup, dataGroup1)))
            .withModifiedFunctionGroups(EMPTY_LIST)
            .withNewFunctionGroups(asList(newlyAddedFgs, modifiedAsNewFgs, newlyAddedFgTemplates))
            .withRemovedFunctionGroups(asList(modifiedAsRemoved1, modifiedAsRemoved2, removedFgTemplates, removedFgs));

        assertPermissionsApprovalDetailsItems(expected, received);
    }

    @Test
    void getUserPermissionApprovalDetailsWithExistingPermissionTest() {
        String approvalId = "approvalId";
        String userId = "userId";
        String legalEntityId = "legalEntityId";
        String serviceAgreementId = "serviceAgreementId";

        String fgId1 = "fgId1";
        String fgId2 = "fgId2";
        String fgId3 = "fgId3";
        String dgId1 = "dgId1";
        String dgId2 = "dgId2";
        String dgId3 = "dgId3";
        String dgId4 = "dgId4";

        FunctionGroup functionGroup1 = createFunctionGroup(fgId1, "FgName1", "FgDesc1",
            FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = createFunctionGroup(fgId2, "FgName2", "FgDesc2",
            FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup3 = createFunctionGroup(fgId3, "FgName3", "FgDesc3",
            FunctionGroupType.DEFAULT);

        DataGroup dataGroup1 = createDataGroup(dgId1, "DgName1", "DgDesc1");
        dataGroup1.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup2 = createDataGroup(dgId2, "DgName2", "DgDesc2");
        dataGroup2.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup3 = createDataGroup(dgId3, "DgName3", "DgDesc3");
        dataGroup3.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup4 = createDataGroup(dgId4, "DgName4", "DgDesc4");
        dataGroup4.setDataItemType("ARRANGEMENTS");

        Set<ApprovalUserContextAssignFunctionGroup> newFgs = new HashSet<>();
        ApprovalUserContext approvalContext = new ApprovalUserContext()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withApprovalUserContextAssignFunctionGroups(newFgs);

        Set<String> appFgDg1 = newHashSet(dataGroup1.getId());
        Set<String> appFgDg2 = Sets.newHashSet(dataGroup3.getId());
        Set<String> appFgDg3 = Sets.newHashSet();
        Set<String> appFgDg4 = Sets.newHashSet(dataGroup4.getId());
        ApprovalUserContextAssignFunctionGroup approvalFg1 = getApprovalAssignedFgObject(functionGroup1,
            approvalContext, appFgDg1);

        ApprovalUserContextAssignFunctionGroup approvalFg2 = getApprovalAssignedFgObject(functionGroup1,
            approvalContext, appFgDg2);

        ApprovalUserContextAssignFunctionGroup approvalFg3 = getApprovalAssignedFgObject(functionGroup2,
            approvalContext, appFgDg3);

        ApprovalUserContextAssignFunctionGroup approvalFg4 = getApprovalAssignedFgObject(functionGroup3,
            approvalContext, appFgDg4);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SaName");
        serviceAgreement.setDescription("SaDescription");
        serviceAgreement.setFunctionGroups(
            newHashSet(functionGroup1, functionGroup2, functionGroup3));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2, dataGroup3, dataGroup4));
        serviceAgreement.setMaster(true);

        Set<UserAssignedFunctionGroup> oldFgs = new HashSet<>();

        UserContext userAcc = new UserContext()
            .withId(1L)
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withUserAssignedFunctionGroups(oldFgs);

        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup()
            .withFunctionGroup(functionGroup1)
            .withUserContext(userAcc);

        UserAssignedFunctionGroup userAssignedFunctionGroup2 = new UserAssignedFunctionGroup()
            .withFunctionGroup(functionGroup2)
            .withUserContext(userAcc);

        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination2 =
            new UserAssignedFunctionGroupCombination();
        userAssignedFunctionGroupCombination2.setDataGroupIds(Sets.newHashSet());
        userAssignedFunctionGroupCombination2.setUserAssignedFunctionGroup(userAssignedFunctionGroup2);

        UserAssignedFunctionGroupCombination functionGroupCombinations1 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroup1.getId(), dataGroup2.getId()), userAssignedFunctionGroup1);

        UserAssignedFunctionGroupCombination functionGroupCombinations2 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroup3.getId()), userAssignedFunctionGroup1);

        List<UserAssignedFunctionGroupCombination> functionGroupCombinations3 =
            singletonList(userAssignedFunctionGroupCombination2);

        UserAssignedFunctionGroup uafgOld1 = getUserAssignedFunctionGroup(functionGroup1,
            asList(functionGroupCombinations1, functionGroupCombinations2), userAcc);

        UserAssignedFunctionGroup uafgOld3 = getUserAssignedFunctionGroup(functionGroup2,
            functionGroupCombinations3, userAcc);

        oldFgs.add(uafgOld1);
        oldFgs.add(uafgOld3);

        newFgs.add(approvalFg1);
        newFgs.add(approvalFg2);
        newFgs.add(approvalFg3);
        newFgs.add(approvalFg4);

        Optional<UserContext> userContext = Optional.of(userAcc);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(approvalUserContextJpaRepository.findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId))
            .thenReturn(Optional.of(approvalContext));

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(approvalContext.getUserId(),
                serviceAgreementId)).thenReturn(userContext);
        PresentationPermissionsApprovalDetailsItem received = permissionsService
            .getUserPermissionApprovalDetails(approvalId);

        PresentationFunctionGroupsDataGroupsPair removedFgs1 = getFunctionGroupsDataGroupsPairsTwoDataGroups(
            functionGroup1, dataGroup2, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair unmodifiedFgs1 = getFunctionGroupsDataGroupsPairs(
            functionGroup1, dataGroup3);
        PresentationFunctionGroupsDataGroupsPair unmodifiedFgs2 = getFunctionGroupsNoDataGroupsPairs(
            functionGroup2);
        List<PresentationFunctionGroupsDataGroupsPair> unmodifiedList1 = asList(unmodifiedFgs1, unmodifiedFgs2);

        PresentationFunctionGroupsDataGroupsPair newFg1 = getFunctionGroupsDataGroupsPairs(functionGroup3, dataGroup4);
        PresentationFunctionGroupsDataGroupsPair newFg2 = getFunctionGroupsDataGroupsPairs(functionGroup1, dataGroup1);

        List<PresentationFunctionGroupsDataGroupsPair> newList1 = asList(newFg2, newFg1);

        PresentationPermissionsApprovalDetailsItem expected = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withUserId(userId)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription(serviceAgreement.getDescription())
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName(serviceAgreement.getName())
            .withUnmodifiedFunctionGroups(unmodifiedList1)
            .withModifiedFunctionGroups(EMPTY_LIST)
            .withNewFunctionGroups(newList1)
            .withRemovedFunctionGroups(asList(removedFgs1));

        assertPermissionsApprovalDetailsItems(expected, received);
    }

    @Test
    void getUserPermissionApprovalDetailsWithOnlyNew() {
        String approvalId = "approvalId";
        String userId = "userId";
        String legalEntityId = "legalEntityId";
        String serviceAgreementId = "serviceAgreementId";

        String fgId1 = "fgId1";
        String fgId2 = "fgId2";

        FunctionGroup functionGroup1 = createFunctionGroup(fgId1, "FgName1", "FgDesc1",
            FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = createFunctionGroup(fgId2, "FgName2", "FgDesc2",
            FunctionGroupType.DEFAULT);

        DataGroup dataGroup1 = createDataGroup("dgId1", "DgName1", "DgDesc1");
        dataGroup1.setDataItemType("ARRANGEMENTS");
        DataGroup dataGroup2 = createDataGroup("dgId2", "DgName2", "DgDesc2");
        dataGroup2.setDataItemType("PAYEES");
        Set<ApprovalUserContextAssignFunctionGroup> newFgs = new HashSet<>();
        ApprovalUserContext approvalContext = new ApprovalUserContext()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withApprovalUserContextAssignFunctionGroups(newFgs);

        Set<String> appFgDg1 = newHashSet(dataGroup1.getId(), dataGroup2.getId());
        Set<String> appFgDg2 = newHashSet(dataGroup1.getId());
        Set<String> appFgDg3 = newHashSet();
        ApprovalUserContextAssignFunctionGroup approvalFg1 = getApprovalAssignedFgObject(functionGroup1,
            approvalContext, appFgDg1);

        ApprovalUserContextAssignFunctionGroup approvalFg2 = getApprovalAssignedFgObject(functionGroup1,
            approvalContext, appFgDg2);

        ApprovalUserContextAssignFunctionGroup approvalFg3 = getApprovalAssignedFgObject(functionGroup2,
            approvalContext, appFgDg3);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SaName");
        serviceAgreement.setDescription("SaDescription");
        serviceAgreement.setFunctionGroups(
            newHashSet(functionGroup1, functionGroup2));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2));
        serviceAgreement.setMaster(true);

        Set<UserAssignedFunctionGroup> oldFgs = new HashSet<>();

        UserContext userAcc = new UserContext()
            .withId(1L)
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withUserAssignedFunctionGroups(oldFgs);

        newFgs.add(approvalFg1);
        newFgs.add(approvalFg2);
        newFgs.add(approvalFg3);

        Optional<UserContext> userContext = Optional.of(userAcc);
        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));
        when(approvalUserContextJpaRepository.findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId))
            .thenReturn(Optional.of(approvalContext));

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(approvalContext.getUserId(),
                serviceAgreementId)).thenReturn(userContext);
        PresentationPermissionsApprovalDetailsItem received = permissionsService
            .getUserPermissionApprovalDetails(approvalId);

        PresentationFunctionGroupsDataGroupsPair newFgs1 = getFunctionGroupsDataGroupsPairs(
            functionGroup1, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair newFgs2 = getFunctionGroupsDataGroupsPairsTwoDataGroups(
            functionGroup1, dataGroup2, dataGroup1);
        PresentationFunctionGroupsDataGroupsPair newFgs3 = getFunctionGroupsNoDataGroupsPairs(
            functionGroup2);

        List<PresentationFunctionGroupsDataGroupsPair> newList1 = asList(newFgs1, newFgs2, newFgs3);

        PresentationPermissionsApprovalDetailsItem expected = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withUserId(userId)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription(serviceAgreement.getDescription())
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName(serviceAgreement.getName())
            .withUnmodifiedFunctionGroups(EMPTY_LIST)
            .withModifiedFunctionGroups(EMPTY_LIST)
            .withNewFunctionGroups(newList1)
            .withRemovedFunctionGroups(EMPTY_LIST);

        assertPermissionsApprovalDetailsItems(expected, received);
    }

    @Test
    void getUserPermissionApprovalDetailsWithoutExistingPermissionsTest() {

        String userId = "userId";
        String serviceAgreementId = "serviceAgreementId";
        String legalEntityId = "legalEntityId";
        String approvalId = "approvalId";
        String newFgId = "newFgId";
        String dgId = "dgId";

        FunctionGroup newFunctionGroup = createFunctionGroup(newFgId, "FgName3", "FgDesc3", FunctionGroupType.DEFAULT);

        DataGroup dataGroup1 = createDataGroup(dgId, "DgName1", "DgDesc1");
        dataGroup1.setDataItemType("ARRANGEMENTS");
        Set<String> appFgDg = newHashSet(dataGroup1.getId());
        Set<ApprovalUserContextAssignFunctionGroup> newFgs = new HashSet<>();

        ApprovalUserContext approvalContext = new ApprovalUserContext()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withApprovalUserContextAssignFunctionGroups(newFgs);

        ApprovalUserContextAssignFunctionGroup approvalFgNewlyAdded = getApprovalAssignedFgObject(newFunctionGroup,
            approvalContext, appFgDg);
        newFgs.add(approvalFgNewlyAdded);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SaName");
        serviceAgreement.setDescription("SaDescription");
        serviceAgreement.setFunctionGroups(newHashSet(newFunctionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1));
        serviceAgreement.setMaster(true);

        mockGetServiceAgreementById(serviceAgreementId, Optional.of(serviceAgreement));

        List<PresentationFunctionGroupsDataGroupsPair> newlyAddedFgs = getFunctionGroupsDataGroupsPairsList(
            newFunctionGroup, dataGroup1);

        when(approvalUserContextJpaRepository.findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId))
            .thenReturn(Optional.of(approvalContext));

        PresentationPermissionsApprovalDetailsItem received = permissionsService
            .getUserPermissionApprovalDetails(approvalId);

        PresentationPermissionsApprovalDetailsItem expected = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withUserId(userId)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription(serviceAgreement.getDescription())
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName(serviceAgreement.getName())
            .withNewFunctionGroups(newlyAddedFgs)
            .withUnmodifiedFunctionGroups(EMPTY_LIST)
            .withModifiedFunctionGroups(EMPTY_LIST)
            .withRemovedFunctionGroups(EMPTY_LIST);

        assertEquals(expected, received);

    }

    @Test
    void shouldThrowNotFoundOnGetUserPermissionApprovalDetailsTest() {
        String approvalId = "approvalId";
        when(approvalUserContextJpaRepository.findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId))
            .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionsService.getUserPermissionApprovalDetails(approvalId));
        assertEquals(exception,
            getNotFoundException(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode()));
    }
    
    @Test
    void shouldThrowBadRequestWhenDataGroupIsInPendingState() {

        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1, functionGroup2));
        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);
        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));
        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        ApprovalDataGroup pendingDeleteDataGroup = new ApprovalDataGroup();
        pendingDeleteDataGroup.setDataGroupId(dataGroup2.getId());

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(anySet()))
            .thenReturn(Lists.newArrayList(pendingDeleteDataGroup));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_061.getErrorMessage(), ERR_ACQ_061.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupIsNotPresent() {

        String userId = "userId";
        LegalEntity legalEntity = createLegalEntity("id.legalentity", "name", "id.external", null,
            LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name.sa", "id.external.sa", "desc", legalEntity,
            null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setId("SA-01");
        FunctionGroup functionGroup1 = FunctionGroupUtil
            .getFunctionGroup("id.fg1", "name.fg1", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        FunctionGroup functionGroup2 = FunctionGroupUtil
            .getFunctionGroup("id.fg2", "name.fg2", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        serviceAgreement.setFunctionGroups(Sets.newHashSet(functionGroup1));

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("name.dg1", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name.dg2", "ARRANGEMENTS", "desc", serviceAgreement);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("name.dg3", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setId("id.dg1");
        dataGroup2.setId("id.dg2");
        dataGroup3.setId("id.dg3");
        serviceAgreement.getDataGroups().add(dataGroup1);
        serviceAgreement.getDataGroups().add(dataGroup2);
        serviceAgreement.getDataGroups().add(dataGroup3);
        PresentationFunctionGroupDataGroup functionDataPair1 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            functionGroup1.getId(), null,
            singletonList(dataGroup1.getId()),
            singletonList(dataGroup2.getName()));

        PresentationFunctionGroupDataGroup functionDataPair2 = createFunctionDataPair(
            serviceAgreement.getExternalId(),
            null, functionGroup2.getName(),
            singletonList(dataGroup3.getId()), new ArrayList<>());

        AssignUserPermissionsData permissionsPutRequestBody = createPermissionsPutRequestBody(
            userId,
            serviceAgreement.getExternalId(),
            legalEntity.getId(),
            asList(functionDataPair1, functionDataPair2));
        mockGetServiceAgreementByExternalId(
            permissionsPutRequestBody.getAssignUserPermissions().getExternalServiceAgreementId(), serviceAgreement);

        ApprovalDataGroup pendingDeleteDataGroup = new ApprovalDataGroup();
        pendingDeleteDataGroup.setDataGroupId(dataGroup2.getId());

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionsService.updateUserPermission(permissionsPutRequestBody));

        assertEquals(exception.getErrors().get(0).getMessage(), ERR_ACQ_003.getErrorMessage());
    }

    @Test
    void shouldValidateSelfApprovalPoliciesWhenAssigningUserContextPermissionsApproval() {
        LegalEntity legalEntity = createLegalEntityWithId("legalEntityId");
        FunctionGroup functionGroup = createFunctionGroup("functionGroupId");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup = createDataGroup("dgId1", "dg1");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setFunctionGroupId("functionGroupId");
        userContextPermissions.setDataGroupIds(Sets.newHashSet("dgId1"));
        Set<UserContextPermissions> permissions = newHashSet(userContextPermissions);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById("serviceAgreementId", Optional.of(serviceAgreement));
        mockCheckIfApprovalAlreadyExists("serviceAgreementId", "userId", Optional.empty());

        permissionsService.assignUserContextPermissionsApproval("serviceAgreementId",
            "userId", "legalEntityId", "approvalId", permissions);

        verify(selfApprovalPolicyValidator).validateSelfApprovalPolicies(Sets.newHashSet(userContextPermissions));
    }


    @Test
    void shouldSaveApprovalSelfApprovalPolicies() {
        LegalEntity legalEntity = createLegalEntityWithId("legalEntityId");
        FunctionGroup functionGroup = createFunctionGroup("functionGroupId");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        DataGroup dataGroup1 = createDataGroup("dgId1", "dg1");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(true);

        ApprovalSelfApprovalPolicy approvalSelfApprovalPolicy = new ApprovalSelfApprovalPolicy();

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");

        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setFunctionGroupId("functionGroupId");
        userContextPermissions.setDataGroupIds(Sets.newHashSet("dgId1"));
        userContextPermissions.setSelfApprovalPolicies(Sets.newHashSet(selfApprovalPolicy));
        Set<UserContextPermissions> permissions = newHashSet(userContextPermissions);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        mockGetServiceAgreementById("serviceAgreementId", Optional.of(serviceAgreement));
        mockCheckIfApprovalAlreadyExists("serviceAgreementId", "userId", Optional.empty());
        when(approvalSelfApprovalPolicyFactory.createPolicy("functionGroupId", selfApprovalPolicy))
            .thenReturn(approvalSelfApprovalPolicy);

        permissionsService.assignUserContextPermissionsApproval("serviceAgreementId",
            "userId", "legalEntityId", "approvalId", permissions);

        ArgumentCaptor<ApprovalUserContext> userContextCaptor = ArgumentCaptor.forClass(ApprovalUserContext.class);

        verify(approvalUserContextJpaRepository).save(userContextCaptor.capture());

        ApprovalUserContext approvalUserContext = userContextCaptor.getValue();
        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup = approvalUserContext
            .getApprovalUserContextAssignFunctionGroups().iterator().next();

        Set<ApprovalSelfApprovalPolicy> approvalSelfApprovalPolicies = approvalUserContextAssignFunctionGroup
            .getApprovalSelfApprovalPolicies();

        assertThat(approvalSelfApprovalPolicies, hasSize(1));
        assertThat(approvalSelfApprovalPolicies.iterator().next(), equalTo(approvalSelfApprovalPolicy));
    }

    private void assertPermissionsApprovalDetailsItems(PresentationPermissionsApprovalDetailsItem expected,
        PresentationPermissionsApprovalDetailsItem actual) {
        Assertions.assertThat(actual.getAction()).isEqualTo(expected.getAction());
        Assertions.assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
        Assertions.assertThat(actual.getCategory()).isEqualTo(expected.getCategory());
        Assertions.assertThat(actual.getServiceAgreementDescription()).isEqualTo(expected.getServiceAgreementDescription());
        Assertions.assertThat(actual.getServiceAgreementId()).isEqualTo(expected.getServiceAgreementId());
        Assertions.assertThat(actual.getServiceAgreementName()).isEqualTo(expected.getServiceAgreementName());

        Assertions.assertThat(actual.getUnmodifiedFunctionGroups()).containsExactlyInAnyOrderElementsOf(expected.getUnmodifiedFunctionGroups());
        Assertions.assertThat(actual.getModifiedFunctionGroups()).containsExactlyInAnyOrderElementsOf(expected.getModifiedFunctionGroups());
        Assertions.assertThat(actual.getNewFunctionGroups()).containsExactlyInAnyOrderElementsOf(expected.getNewFunctionGroups());
        Assertions.assertThat(actual.getRemovedFunctionGroups()).containsExactlyInAnyOrderElementsOf(expected.getRemovedFunctionGroups());
    }

    private List<PresentationFunctionGroupsDataGroupsPair> getFunctionGroupsDataGroupsPairsList(
        FunctionGroup unModifiedFunctionGroup,
        DataGroup dataGroup1) {
        return singletonList(
            new PresentationFunctionGroupsDataGroupsPair()
                .withId(unModifiedFunctionGroup.getId())
                .withName(unModifiedFunctionGroup.getName())
                .withType(PresentationFunctionGroupsDataGroupsPair.Type.REGULAR)
                .withDescription(unModifiedFunctionGroup.getDescription())
                .withDataGroups(singletonList(
                    new PresentationDataGroupApprovalItem()
                        .withId(dataGroup1.getId())
                        .withName(dataGroup1.getName())
                        .withDescription(dataGroup1.getDescription())
                        .withType("ARRANGEMENTS"))));
    }

    private PresentationFunctionGroupsDataGroupsPair getFunctionGroupsDataGroupsPairs(FunctionGroup functionGroup,
       DataGroup dataGroup1) {

        List<PresentationDataGroupApprovalItem> dataGroups = new ArrayList<>();

        if (nonNull(dataGroup1)) {
            dataGroups.add(
                new PresentationDataGroupApprovalItem()
                    .withId(dataGroup1.getId())
                    .withName(dataGroup1.getName())
                    .withDescription(dataGroup1.getDescription())
                    .withType("ARRANGEMENTS"));
        }

        return new PresentationFunctionGroupsDataGroupsPair()
            .withId(functionGroup.getId())
            .withName(functionGroup.getName())
            .withType(getType(functionGroup.getType()))
            .withDescription(functionGroup.getDescription())
            .withDataGroups(dataGroups);
    }

    private Type getType(FunctionGroupType type) {// !!!!! enum
        if (type == FunctionGroupType.TEMPLATE) {
            return Type.TEMPLATE;
        }
        if (type == FunctionGroupType.SYSTEM) {
            return Type.SYSTEM;
        }
        return Type.REGULAR;
    }

    private PresentationFunctionGroupsDataGroupsPair getFunctionGroupsNoDataGroupsPairs(
        FunctionGroup unModifiedFunctionGroup) {
        return new PresentationFunctionGroupsDataGroupsPair()
            .withId(unModifiedFunctionGroup.getId())
            .withName(unModifiedFunctionGroup.getName())
            .withType(PresentationFunctionGroupsDataGroupsPair.Type.REGULAR)
            .withDescription(unModifiedFunctionGroup.getDescription());
    }

    private PresentationFunctionGroupsDataGroupsPair getFunctionGroupsDataGroupsPairsTwoDataGroups(
        FunctionGroup unModifiedFunctionGroup,
        DataGroup dataGroup1, DataGroup dataGroup2) {

        ArrayList<PresentationDataGroupApprovalItem> presentationDataGroupApprovalItems =
            new ArrayList<>();
        presentationDataGroupApprovalItems.add(new PresentationDataGroupApprovalItem()
            .withId(dataGroup1.getId())
            .withName(dataGroup1.getName())
            .withDescription(dataGroup1.getDescription())
            .withType(dataGroup1.getDataItemType()));
        presentationDataGroupApprovalItems.add(new PresentationDataGroupApprovalItem()
            .withId(dataGroup2.getId())
            .withName(dataGroup2.getName())
            .withDescription(dataGroup2.getDescription())
            .withType(dataGroup2.getDataItemType()));

        return new PresentationFunctionGroupsDataGroupsPair()
            .withId(unModifiedFunctionGroup.getId())
            .withName(unModifiedFunctionGroup.getName())
            .withType(PresentationFunctionGroupsDataGroupsPair.Type.REGULAR)
            .withDescription(unModifiedFunctionGroup.getDescription())
            .withDataGroups(presentationDataGroupApprovalItems);
    }

    private UserAssignedFunctionGroup getUserAssignedFunctionGroup(FunctionGroup unModifiedFunctionGroup,
        List<UserAssignedFunctionGroupCombination> userAssignedFgDgUnmodified, UserContext userContext) {
        return new UserAssignedFunctionGroup()
            .withFunctionGroup(unModifiedFunctionGroup)
            .withId(userAssignedFunctionGroupIdGen.nextLong())
            .withUserContext(userContext)
            .withUserContextId(userContext.getId())
            .withFunctionGroupId(unModifiedFunctionGroup.getId())
            .withUserAssignedFunctionGroupCombinations(userAssignedFgDgUnmodified);
    }

    private List<UserAssignedFunctionGroupCombination> getUserAssignedFunctionGroupDataGroups(DataGroup dataGroup1,
        UserAssignedFunctionGroup userAssignedFunctionGroup) {
        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination =
            new UserAssignedFunctionGroupCombination();
        userAssignedFunctionGroupCombination.setDataGroupIds(Sets.newHashSet(dataGroup1.getId()));
        userAssignedFunctionGroupCombination.setDataGroups(Sets.newHashSet(dataGroup1));
        userAssignedFunctionGroupCombination.setUserAssignedFunctionGroup(userAssignedFunctionGroup);
        return singletonList(
            userAssignedFunctionGroupCombination);
    }

    private ServiceAgreement getServiceAgreement(String serviceAgreementId, FunctionGroup modifiedFunctionGroup,
        FunctionGroup removedFunctionGroup, FunctionGroup newFunctionGroup, FunctionGroup unModifiedFunctionGroup,
        DataGroup dataGroup1, DataGroup dataGroup2, DataGroup dataGroup3) {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SaName");
        serviceAgreement.setDescription("SaDescription");
        serviceAgreement.setFunctionGroups(
            newHashSet(modifiedFunctionGroup, removedFunctionGroup, unModifiedFunctionGroup, newFunctionGroup));
        serviceAgreement.setDataGroups(newHashSet(dataGroup1, dataGroup2, dataGroup3));
        serviceAgreement.setMaster(true);
        return serviceAgreement;
    }

    private ApprovalUserContextAssignFunctionGroup getApprovalAssignedFgObject(FunctionGroup functionGroup,
        ApprovalUserContext approvalContext, Set<String> appFgDgModified) {

        return new ApprovalUserContextAssignFunctionGroup()
            .withApprovalUserContext(approvalContext)
            .withFunctionGroupId(functionGroup.getId())
            .withDataGroups(appFgDgModified);
    }

    private DataGroup createDataGroup(String dgId, String dgName, String dgDesc) {
        DataGroup dataGroup1 = new DataGroup();
        dataGroup1.setId(dgId);
        dataGroup1.setName(dgName);
        dataGroup1.setDescription(dgDesc);
        return dataGroup1;
    }

    private FunctionGroup createFunctionGroup(String fgId, String fgName, String fgDesc, FunctionGroupType type) {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(fgId);
        functionGroup.setName(fgName);
        functionGroup.setDescription(fgDesc);
        functionGroup.setType(type);
        return functionGroup;
    }

    private DataGroup createDataGroup(String dataGroupId1, String dgName) {
        DataGroup dataGroup1 = new DataGroup();
        dataGroup1.setId(dataGroupId1);
        dataGroup1.setName(dgName);
        return dataGroup1;
    }

    private FunctionGroup createFunctionGroup(String functionGroupId) {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        return functionGroup;
    }

    private FunctionGroup createFunctionGroupWithAPS(String functionGroupId, FunctionGroupType type,
        AssignablePermissionSet assignablePermissionSet) {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        functionGroup.setName("fgId");
        functionGroup.setType(type);
        functionGroup.setAssignablePermissionSet(assignablePermissionSet);
        return functionGroup;
    }

    private LegalEntity createLegalEntityWithId(String userLegalEntityId) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(userLegalEntityId);
        return legalEntity;
    }

    private void mockGetServiceAgreementById(String serviceAgreementId, Optional<ServiceAgreement> serviceAgreement) {
        when(serviceAgreementJpaRepository
            .findById(serviceAgreementId,
                GraphConstants.SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS))
                    .thenReturn(serviceAgreement);
    }

    private boolean containsUserAssignedFunctionGroups(Collection<UserAssignedFunctionGroup> userAssignedFunctionGroups,
        UserContext userContext, FunctionGroup functionGroup, List<DataGroup> dataGroups) {
        return userAssignedFunctionGroups.stream()
            .anyMatch(uaFg -> uaFg.getUserContextId().equals(userContext.getId())
                && uaFg.getFunctionGroupId().equals(functionGroup.getId())
                && containsAllDataGroups(uaFg.getFunctionGroupId(), uaFg.getUserAssignedFunctionGroupCombinations(),
                    dataGroups));
    }

    private boolean containsAllDataGroups(String functionGroupId,
        Collection<UserAssignedFunctionGroupCombination> userAssignedFunctionGroupDataGroups,
        List<DataGroup> dataGroups) {
        for (UserAssignedFunctionGroupCombination item : userAssignedFunctionGroupDataGroups) {
            for (DataGroup dgItem : item.getDataGroups()) {
                for (DataGroup dg : dataGroups) {
                    if (!item.getUserAssignedFunctionGroup().getFunctionGroup().getId().equals(functionGroupId)
                        && !dg.getId().equals(dgItem.getId())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void mockGetFunctionGroupTemplateById(String id, Optional<FunctionGroup> functionGroup) {
        when(functionGroupJpaRepository
            .findById(id))
                .thenReturn(functionGroup);
    }

    private void mockGetUserContext(String userId, String serviceAgreementId) {
        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementId(eq(userId), eq(serviceAgreementId))).thenReturn(
                Optional.of(new UserContext(userId, serviceAgreementId)));
    }

    private void mockGetServiceAgreementByExternalId(String externalServiceAgreementId,
        ServiceAgreement serviceAgreement) {
        when(serviceAgreementJpaRepository.findByExternalId(eq(externalServiceAgreementId), anyString()))
            .thenReturn(Optional.of(serviceAgreement));
    }


    private PresentationFunctionGroupDataGroup createFunctionDataPair(String externalServiceAgreementId,
        String functionGroupId, String functionGroupName, List<String> dataGroupIds, List<String> dataGroupNames) {
        PresentationIdentifier functionGroupIdentifier = createEntityIdentifier(functionGroupId, functionGroupName,
            externalServiceAgreementId);
        List<PresentationIdentifier> dataGroupIdentifiersList =
            createDataGroupIdentifiersList(externalServiceAgreementId, dataGroupIds,
                dataGroupNames);

        return new PresentationFunctionGroupDataGroup()
            .withFunctionGroupIdentifier(functionGroupIdentifier)
            .withDataGroupIdentifiers(dataGroupIdentifiersList);
    }

    private List<PresentationIdentifier> createDataGroupIdentifiersList(String externalServiceAgreementId,
        List<String> dataGroupIds, List<String> dataGroupNames) {
        Stream<PresentationIdentifier> dataGroupIdIdentifiers = dataGroupIds.stream()
            .map(dataGroupId -> new PresentationIdentifier()
                .withIdIdentifier(dataGroupId));

        Stream<PresentationIdentifier> dataGroupNameIdentifiers = dataGroupNames.stream()
            .map(dataGroupName -> new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName(dataGroupName)
                    .withExternalServiceAgreementId(externalServiceAgreementId)));

        return Stream.concat(dataGroupIdIdentifiers, dataGroupNameIdentifiers)
            .collect(Collectors.toList());
    }

    private PresentationIdentifier createEntityIdentifier(String functionGroupId, String functionGroupName,
        String externalServiceAgreementId) {
        return Optional.ofNullable(functionGroupId)
            .map(id -> new PresentationIdentifier().withIdIdentifier(id))
            .orElseGet(() -> new PresentationIdentifier().withNameIdentifier(
                new NameIdentifier().withName(functionGroupName)
                    .withExternalServiceAgreementId(externalServiceAgreementId)));
    }

    private List<UserAssignedFunctionGroupCombination> getUserAssignedFunctionGroupDataGroups(DataGroup dataGroup1,
        DataGroup dataGroup2, UserAssignedFunctionGroup userAssignedFunctionGroup) {
        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination =
            new UserAssignedFunctionGroupCombination(
                Sets.newHashSet(dataGroup1.getId()),
                userAssignedFunctionGroup);
        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination1 =
            new UserAssignedFunctionGroupCombination(
                Sets.newHashSet(dataGroup2.getId()),
                userAssignedFunctionGroup);
        return asList(
            userAssignedFunctionGroupCombination,
            userAssignedFunctionGroupCombination1);
    }

    private AssignUserPermissionsData createPermissionsPutRequestBody(String userId,
        String externalServiceAgreementId, String legalEntityId,
        List<PresentationFunctionGroupDataGroup> functionDataPairs) {

        String externalUserId = "externalUserId";

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 =
            new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setExternalId(externalUserId);
        user1.setLegalEntityId(legalEntityId);
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> externalIdToUserMap = new HashMap<>();
        externalIdToUserMap.put(externalUserId, user1);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(externalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withFunctionGroupDataGroups(functionDataPairs);

        return new AssignUserPermissionsData(assignUserPermissions, externalIdToUserMap);
    }

    private Matcher<ApprovalUserContextAssignFunctionGroup> getApprovalUserContextAssignFunctionGroupMatcher(
        Matcher<?> functionGroupIdMatcher, Matcher<?> dataGroupMatcher) {
        return Matchers.allOf(
            hasProperty("functionGroupId", functionGroupIdMatcher),
            hasProperty("dataGroups", dataGroupMatcher));
    }
}

