package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.APS_PERMISSIONS_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_FGS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_027;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_073;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_100;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_101;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_103;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_104;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_107;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_012;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_025;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_062;
import static com.backbase.accesscontrol.util.helpers.ApplicableFunctionPrivilegeUtil.getApplicableFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.BusinessFunctionUtil.getBusinessFunction;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.PermissionUtil.getPermission;
import static com.backbase.accesscontrol.util.helpers.PermissionUtil.getPermissions;
import static com.backbase.accesscontrol.util.helpers.PrivilegeUtil.getPrivilege;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.FunctionGroupTransformer;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup.Type;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.mappers.PersistenceFunctionGroupApprovalDetailsItemMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.SelfApprovalPolicyJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.TimeBoundValidatorService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.PermissionUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.google.common.collect.Sets;

@ExtendWith(MockitoExtension.class)
class FunctionGroupServiceImplTest {

    @InjectMocks
    private FunctionGroupServiceImpl functionGroupServiceImpl;
    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Mock
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Mock
    private ApprovalUserContextAssignFunctionGroupJpaRepository approvalUserContextAssignFunctionGroupJpaRepository;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    @Mock
    private ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private PersistenceFunctionGroupApprovalDetailsItemMapper mapper;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("GMT+2");
    @Mock
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Captor
    private ArgumentCaptor<FunctionGroup> functionGroupCaptor;
    @Mock
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Spy
    private TimeBoundValidatorService timeBoundValidatorService = new TimeBoundValidatorService("UTC");
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private SelfApprovalPolicyJpaRepository selfApprovalPolicyJpaRepository;
    @Mock
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(functionGroupServiceImpl, "functionGroupTransformer",
            new FunctionGroupTransformer(businessFunctionCache));
    }

    @Test
    void saveFunctionGroupApproval() {
        mockApprovalValidation(applicationProperties, true);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        PrivilegeDto privilege = new PrivilegeDto()
            .withPrivilege("approve");
        Permission permission = new Permission()
            .withFunctionId("funcId")
            .withAssignedPrivileges(singletonList(privilege));
        List<Permission> permissions = singletonList(permission);
        FunctionGroupApprovalBase functionGroupApprovalCreate =
            new FunctionGroupApprovalBase()
                .withApprovalId("approvalId")
                .withApprovalTypeId("approvalTypeId")
                .withDescription("desc")
                .withName("name")
                .withPermissions(permissions)
                .withServiceAgreementId("saId")
                .withValidFrom(new Date(10))
                .withValidUntil(new Date(10000));

        ApplicableFunctionPrivilege afp = new ApplicableFunctionPrivilege();
        com.backbase.accesscontrol.domain.Privilege privilege1 =
            new com.backbase.accesscontrol.domain.Privilege();
        privilege1.setName("approve");
        afp.setPrivilege(privilege1);
        List<ApplicableFunctionPrivilege> privilegeList = singletonList(afp);
        when(businessFunctionCache.findAllByBusinessFunctionIdAndPrivilegeNameIn(
            "funcId", singletonList("approve"))).thenReturn(privilegeList);
        mockGetAllApplicableFunctionPrivileges(singletonList(permission.getFunctionId()));
        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(functionGroupApprovalCreate.getName(),
            functionGroupApprovalCreate.getServiceAgreementId(), false);
        mockGetFunctionGroupsByNameAndServiceAgreementId(functionGroupApprovalCreate.getName(),
            functionGroupApprovalCreate.getServiceAgreementId(), false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(
            functionGroupApprovalCreate.getServiceAgreementId(), Optional.of(serviceAgreement));

        String savedIdFromApprovalTables = functionGroupServiceImpl
            .addFunctionGroupApproval(functionGroupApprovalCreate);

        assertNotNull(savedIdFromApprovalTables);
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("saId"));
    }

    @Test
    void updateFunctionGroupApproval() {
        mockApprovalValidation(applicationProperties, true);
        String approveId = "approve";
        String funcId = "funcId";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String saId = "saId";
        serviceAgreement.setId(saId);
        Privilege privilege = new Privilege().withPrivilege(approveId);

        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission permission = new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission()
            .withFunctionId(funcId)
            .withAssignedPrivileges(singletonList(privilege));
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions = singletonList(
            permission);

        FunctionGroupByIdPutRequestBody updateData = getFunctionGroupApprovalUpdate(permissions);

        FunctionGroup fg = new FunctionGroup().withName(updateData.getName())
            .withDescription(updateData.getDescription()).withId("id").withType(FunctionGroupType.DEFAULT)
            .withServiceAgreement(serviceAgreement);

        ApplicableFunctionPrivilege afp = new ApplicableFunctionPrivilege();
        com.backbase.accesscontrol.domain.Privilege privilege1 =
            new com.backbase.accesscontrol.domain.Privilege();
        privilege1.setName(approveId);
        afp.setPrivilege(privilege1);

        List<ApplicableFunctionPrivilege> privilegeList = singletonList(afp);
        when(businessFunctionCache.findAllByBusinessFunctionIdAndPrivilegeNameIn(
            funcId, Collections.singletonList(approveId))).thenReturn(privilegeList);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreement.getId(), Optional.of(serviceAgreement));
        mockGetAllApplicableFunctionPrivileges(singletonList(permission.getFunctionId()));
        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(updateData.getName(),
            updateData.getServiceAgreementId(), false);
        when(functionGroupJpaRepository.findById(eq(funcId)))
            .thenReturn(Optional.of(fg));

        ArgumentCaptor<ApprovalFunctionGroup> captor = ArgumentCaptor.forClass(ApprovalFunctionGroup.class);

        functionGroupServiceImpl.updateFunctionGroupApproval(updateData, funcId, approveId);

        verify(approvalFunctionGroupJpaRepository, times(1)).save(captor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(saId));
        assertEquals(captor.getValue().getApprovalTypeId(), updateData.getApprovalTypeId());
        assertEquals(captor.getValue().getServiceAgreementId(), updateData.getServiceAgreementId());
        assertEquals(funcId, captor.getValue().getFunctionGroupId());
    }

    @ParameterizedTest
    @EnumSource(value = FunctionGroupType.class, names = {"SYSTEM", "TEMPLATE"})
    void updateFunctionGroupApprovalOnly(FunctionGroupType functionGroupType) {
        mockApprovalValidation(applicationProperties, true);
        String approveId = "approve";
        String funcId = "funcId";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String saId = "saId";
        serviceAgreement.setId(saId);
        Privilege privilege = new Privilege().withPrivilege(approveId);
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission permission =
            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission()
                .withFunctionId(funcId).withAssignedPrivileges(singletonList(privilege));
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions =
            singletonList(
                permission);
        FunctionGroupByIdPutRequestBody updateData = getFunctionGroupApprovalUpdate(permissions);
        FunctionGroup fg = new FunctionGroup().withName(updateData.getName())
            .withDescription(updateData.getDescription()).withId(funcId).withType(functionGroupType)
            .withServiceAgreement(serviceAgreement);
        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(updateData.getName(),
            updateData.getServiceAgreementId(), false);
        when(functionGroupJpaRepository.findById(eq(funcId))).thenReturn(Optional.of(fg));
        ArgumentCaptor<ApprovalFunctionGroup> captor = ArgumentCaptor.forClass(ApprovalFunctionGroup.class);
        functionGroupServiceImpl.updateFunctionGroupApproval(updateData, funcId, approveId);
        verify(approvalFunctionGroupJpaRepository, times(1)).save(captor.capture());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(saId));
        assertEquals(captor.getValue().getApprovalTypeId(), updateData.getApprovalTypeId());
        assertEquals(captor.getValue().getServiceAgreementId(), updateData.getServiceAgreementId());
        assertEquals(funcId, captor.getValue().getFunctionGroupId());
        assertTrue(Sets
            .symmetricDifference(captor.getValue().getPrivileges(), fg.getPermissions().stream()
                .map(FunctionGroupItem::getApplicableFunctionPrivilegeId).collect(Collectors.toSet()))
            .isEmpty());
    }
    
    @Test
    void shouldThrowBadRequestWhenPendingUpdateAlreadyExists() {
        mockApprovalValidation(applicationProperties, true);
        String approve = "approve";
        String funcId = "funcId";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");

        Privilege privilege = new Privilege()
            .withPrivilege(approve);
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission permission = new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission()
            .withFunctionId(funcId)
            .withAssignedPrivileges(singletonList(privilege));
        FunctionGroupByIdPutRequestBody updateData = getFunctionGroupApprovalUpdate(singletonList(permission));

        when(approvalFunctionGroupRefJpaRepository.existsByFunctionGroupId(eq(funcId)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupApproval(updateData, funcId, approve));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_101.getErrorMessage(), ERR_ACC_101.getErrorCode()));
        verify(approvalFunctionGroupJpaRepository, times(0)).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenNameAlreadyExistsInPendingTable() {
        mockApprovalValidation(applicationProperties, true);
        String approve = "approve";
        String funcId = "funcId";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        Privilege privilege = new Privilege()
            .withPrivilege(approve);
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission permission = new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission()
            .withFunctionId(funcId)
            .withAssignedPrivileges(singletonList(privilege));
        FunctionGroupByIdPutRequestBody updateData = getFunctionGroupApprovalUpdate(singletonList(permission));

        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(updateData.getName(),
            updateData.getServiceAgreementId(), false);
        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(updateData.getName(),
            serviceAgreement.getId(), true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupApproval(updateData, funcId, approve));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_100.getErrorMessage(), ERR_ACC_100.getErrorCode()));
        verify(approvalFunctionGroupJpaRepository, times(0)).save(any());
    }

    private FunctionGroupByIdPutRequestBody getFunctionGroupApprovalUpdate(
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions) {

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date fgStartDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date fgEndDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        return new FunctionGroupByIdPutRequestBody()
            .withApprovalTypeId("approvalTypeId")
            .withDescription("desc")
            .withName("name")
            .withPermissions(permissions)
            .withServiceAgreementId("saId")
            .withValidFromDate(dateTimeService.getStringDateFromDate(fgStartDate))
            .withValidFromTime(dateTimeService.getStringTimeFromDate(fgStartDate))
            .withValidUntilDate(dateTimeService.getStringDateFromDate(fgEndDate))
            .withValidUntilTime(dateTimeService.getStringTimeFromDate(fgEndDate));
    }

    @Test
    void shouldGetFunctionGroupApprovalDetailsOnCreate() {
        String approvalId = "approvalId";
        String serviceAgreementId = "saId";

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setServiceAgreementId(serviceAgreementId);

        PresentationFunctionGroupApprovalDetailsItem expected = new PresentationFunctionGroupApprovalDetailsItem();
        expected.withApprovalId(approvalId);
        expected.withServiceAgreementId(serviceAgreementId);

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withName("saName");

        when(serviceAgreementJpaRepository.findById(eq(approvalFunctionGroup.getServiceAgreementId()), isNull()))
            .thenReturn(Optional.of(serviceAgreement));
        when(approvalFunctionGroupRefJpaRepository
            .findByApprovalId(approvalId)).thenReturn(Optional.of(approvalFunctionGroup));
        when(mapper
            .getResult(any(), any(), any(String.class)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem().withApprovalId(approvalId)
                .withServiceAgreementId(serviceAgreementId));

        PresentationFunctionGroupApprovalDetailsItem actual = functionGroupServiceImpl
            .getByApprovalId(approvalId);

        assertEquals(expected.getApprovalId(), actual.getApprovalId());
        assertEquals(expected.getServiceAgreementId(), actual.getServiceAgreementId());
    }

    @Test
    void shouldGetFunctionGroupApprovalDetailsOnUpdate() {
        String approvalId = "approvalId";
        String functionGroupId = "functionGroupId";
        String serviceAgreementId = "saId";

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withName("saName");
        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setFunctionGroupId(functionGroupId);
        approvalFunctionGroup.setServiceAgreementId(serviceAgreementId);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        functionGroup.setServiceAgreement(serviceAgreement);
        PresentationFunctionGroupApprovalDetailsItem expected = new PresentationFunctionGroupApprovalDetailsItem();
        expected.withApprovalId(approvalId);
        expected.withFunctionGroupId(functionGroupId);
        expected.withServiceAgreementId(serviceAgreementId);

        when(approvalFunctionGroupRefJpaRepository
            .findByApprovalId(approvalId)).thenReturn(Optional.of(approvalFunctionGroup));

        when(functionGroupJpaRepository
            .findById(functionGroupId)).thenReturn(Optional.of(functionGroup));
        when(mapper
            .getResult(any(FunctionGroup.class), any(), any(String.class)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem().withApprovalId(approvalId)
                .withServiceAgreementId(serviceAgreementId).withFunctionGroupId(functionGroupId));

        PresentationFunctionGroupApprovalDetailsItem actual = functionGroupServiceImpl
            .getByApprovalId(approvalId);

        assertEquals(expected.getApprovalId(), actual.getApprovalId());
        assertEquals(expected.getServiceAgreementId(), actual.getServiceAgreementId());
        assertEquals(expected.getFunctionGroupId(), actual.getFunctionGroupId());
    }

    @Test
    void shouldGetFunctionGroupApprovalDetailsOnDelete() {
        String approvalId = "approvalId";
        String functionGroupId = "functionGroupId";
        String serviceAgreementId = "saId";

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withId(serviceAgreementId)
            .withName("saName");
        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId(approvalId);
        approvalFunctionGroupRef.setFunctionGroupId(functionGroupId);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        functionGroup.setServiceAgreement(serviceAgreement);
        PresentationFunctionGroupApprovalDetailsItem expected = new PresentationFunctionGroupApprovalDetailsItem();
        expected.withApprovalId(approvalId);
        expected.withFunctionGroupId(functionGroupId);
        expected.withServiceAgreementId(serviceAgreementId);

        when(approvalFunctionGroupRefJpaRepository
            .findByApprovalId(approvalId)).thenReturn(Optional.of(approvalFunctionGroupRef));

        when(functionGroupJpaRepository
            .findById(functionGroupId)).thenReturn(Optional.of(functionGroup));

        when(mapper
            .getResult(any(FunctionGroup.class), any(), any(String.class)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem().withApprovalId(approvalId)
                .withServiceAgreementId(serviceAgreementId).withFunctionGroupId(functionGroupId));

        PresentationFunctionGroupApprovalDetailsItem actual = functionGroupServiceImpl
            .getByApprovalId(approvalId);

        assertEquals(expected.getApprovalId(), actual.getApprovalId());
        assertEquals(expected.getServiceAgreementId(), actual.getServiceAgreementId());
        assertEquals(expected.getFunctionGroupId(), actual.getFunctionGroupId());
    }

    @Test
    void shouldThrowBadRequestWhenNameAlreadyPendingInSaveFunctionGroupApproval() {
        mockApprovalValidation(applicationProperties, true);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        PrivilegeDto privilege = new PrivilegeDto()
            .withPrivilege("approve");
        Permission permission = new Permission()
            .withFunctionId("funcId")
            .withAssignedPrivileges(singletonList(privilege));
        List<Permission> permissions = singletonList(permission);
        FunctionGroupApprovalBase functionGroupApprovalCreate =
            new FunctionGroupApprovalBase()
                .withApprovalId("approvalId")
                .withDescription("desc")
                .withName("name")
                .withPermissions(permissions)
                .withServiceAgreementId("saId")
                .withValidFrom(new Date(10))
                .withValidUntil(new Date(10000));

        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(functionGroupApprovalCreate.getName(),
            functionGroupApprovalCreate.getServiceAgreementId(), true);
        mockGetFunctionGroupsByNameAndServiceAgreementId(functionGroupApprovalCreate.getName(),
            functionGroupApprovalCreate.getServiceAgreementId(), false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .addFunctionGroupApproval(functionGroupApprovalCreate));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_100.getErrorMessage(), ERR_ACC_100.getErrorCode()));
    }

    @Test
    void getFunctionGroupById() {
        LegalEntity legalEntity = LegalEntityUtil.createLegalEntity("LE-01", "LE-name",
            "LE-01-EX", null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity(legalEntity);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setServiceAgreement(serviceAgreement);
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);

        when(functionGroupJpaRepository.findById(eq("id")))
            .thenReturn(of(functionGroup));

        FunctionGroupByIdGetResponseBody serviceResponse = functionGroupServiceImpl.getFunctionGroupById("id");

        assertEquals("id", serviceResponse.getId());
        assertEquals(FunctionGroupType.DEFAULT.toString(), serviceResponse.getType().toString());
    }

    @Test
    void getFunctionGroupByIdAndApprovalIdShouldBePresentIfFgInPendingState() {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        ApprovalFunctionGroup functionGroupApproval = new ApprovalFunctionGroup();
        functionGroupApproval.setApprovalId("approvalId");

        when(functionGroupJpaRepository.findById(eq("id")))
            .thenReturn(of(functionGroup));
        when(approvalFunctionGroupRefJpaRepository
            .findByFunctionGroupId(eq(functionGroup.getId())))
            .thenReturn(of(functionGroupApproval));
        FunctionGroupByIdGetResponseBody serviceResponse = functionGroupServiceImpl.getFunctionGroupById("id");

        assertEquals("id", serviceResponse.getId());
        assertEquals("approvalId", serviceResponse.getApprovalId());
        assertEquals(FunctionGroupType.DEFAULT.toString(), serviceResponse.getType().toString());
    }

    @Test
    void getFunctionGroupByIdWithTimeBound() {
        LegalEntity legalEntity = LegalEntityUtil.createLegalEntity("LE-01", "LE-name",
            "LE-01-EX", null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity(legalEntity);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);
        functionGroup.setStartDate(new Date(0));
        functionGroup.setEndDate(new Date(1));

        when(functionGroupJpaRepository.findById(eq("id")))
            .thenReturn(of(functionGroup));

        FunctionGroupByIdGetResponseBody serviceResponse = functionGroupServiceImpl.getFunctionGroupById("id");

        assertEquals("id", serviceResponse.getId());
        assertEquals(FunctionGroupType.DEFAULT.toString(), serviceResponse.getType().toString());
        assertEquals(serviceResponse.getValidFrom(), functionGroup.getStartDate());
        assertEquals(serviceResponse.getValidUntil(), functionGroup.getEndDate());

    }

    @Test
    void shouldThrowExceptionOnGetFunctionGroupById() {
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> functionGroupServiceImpl.getFunctionGroupById("id"));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldGetBulkFunctionGroupsByIds() {
        Set<String> ids = new HashSet<>();
        ids.add("11");
        ids.add("22");

        LegalEntity legalEntity1 = createLegalEntity("le1", "le1.name", "le1.ex-id", null, LegalEntityType.BANK);
        createLegalEntity("le2", "le2.name", "le2.ex-id", legalEntity1,
            LegalEntityType.CUSTOMER);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity(legalEntity1);

        FunctionGroup fg1 = getFunctionGroup("11", "fg1", "desc.fg1", new LinkedHashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement);
        FunctionGroup fg2 = getFunctionGroup("22", "fg2", "desc.fg2", new LinkedHashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement);

        BusinessFunction businessFunction1 = getBusinessFunction("1", "bf1.name", "bf1.code", "bf1.resource",
            "bf1.resource.code");
        com.backbase.accesscontrol.domain.Privilege privilege1 = getPrivilege("001", "pr1.name", "pr1.code");

        int hash = privilege1.hashCode();
        boolean equal = privilege1.equals(businessFunction1);

        Set<GroupedFunctionPrivilege> groupedFunctionPrivileges = new LinkedHashSet<>();
        ApplicableFunctionPrivilege applicableFunctionPrivilege = getApplicableFunctionPrivilege("appFnPrivID",
            businessFunction1, privilege1, true);
        GroupedFunctionPrivilege groupedFunctionPrivilege = getGroupedFunctionPrivilege(null,
            applicableFunctionPrivilege, fg1);
        groupedFunctionPrivileges.add(groupedFunctionPrivilege);

        List<FunctionGroup> dbResponse = new ArrayList<>();
        fg1.setPermissions(groupedFunctionPrivileges);
        dbResponse.add(fg1);
        dbResponse.add(fg2);

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(Collections.singleton("appFnPrivID"))))
            .thenReturn(Collections.singleton(applicableFunctionPrivilege));
        when(functionGroupJpaRepository.findByIdIn(eq(ids))).thenReturn(dbResponse);

        List<BulkFunctionGroupsPostResponseBody> bulkFunctionGroups = functionGroupServiceImpl
            .getBulkFunctionGroups(ids);

        assertFalse(equal);
        assertNotEquals(0, hash);
        assertEquals(ids.size(), bulkFunctionGroups.size());
        assertEquals(1,
            bulkFunctionGroups.stream().filter(fg -> fg.getId().equals("11")).count());
        assertEquals(1,
            bulkFunctionGroups.stream().filter(fg -> fg.getId().equals("22")).count());
        assertEquals(1,
            bulkFunctionGroups.stream().filter(fg -> fg.getId().equals("11")).collect(Collectors.toList()).get(0)
                .getPermissions().size());
    }

    @Test
    void shouldThrowBadRequestOnGetBulkFunctionGroupsWhenInvalidListOfIds() {
        Set<String> ids = new HashSet<>();
        ids.add("11");
        ids.add("");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.getBulkFunctionGroups(ids));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_012.getErrorMessage(), ERR_ACQ_012.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestOnGetBulkFunctionGroupsWhenInvalidLResponse() {
        Set<String> ids = new HashSet<>();
        ids.add("11");
        ids.add("22");

        FunctionGroup fg1 = getFunctionGroup("11", "fg1", "desc.fg1", new LinkedHashSet<>(),
            FunctionGroupType.DEFAULT, null);

        List<FunctionGroup> dbResponse = new ArrayList<>();
        dbResponse.add(fg1);

        when(functionGroupJpaRepository.findByIdIn(eq(ids))).thenReturn(dbResponse);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.getBulkFunctionGroups(ids));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldDeleteFunctionGroupById() {
        String functionGroupId = "id.fg1";
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq("id.fg1"), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(Optional.of(new FunctionGroup()
                .withId("FG-01")));

        functionGroupServiceImpl.deleteFunctionGroup(functionGroupId);
        verify(functionGroupJpaRepository, times(1)).deleteById(eq(functionGroupId));
    }

    @Test
    void shouldThrowBadRequestOnDeleteWhenUsersAreAssigned() {
        String functionGroupId = "id.fg1";
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq("id.fg1"), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(Optional.of(functionGroup));
        when(userAssignedFunctionGroupJpaRepository.findAllByFunctionGroupId(functionGroupId))
            .thenReturn(asList(new UserAssignedFunctionGroup()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.deleteFunctionGroup(functionGroupId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_027.getErrorMessage(), ERR_ACC_027.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestPendingRecordOnDeleteFunctionGroupById() {
        mockApprovalValidation(applicationProperties, true);
        String functionGroupId = "id.fg1";
        when(approvalFunctionGroupRefJpaRepository.existsByFunctionGroupId(eq(functionGroupId)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.deleteFunctionGroup(functionGroupId));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_101.getErrorMessage(), ERR_ACC_101.getErrorCode()));
        verify(functionGroupJpaRepository, times(0)).deleteById(eq(functionGroupId));
    }


    @Test
      void shouldThrowBadRequestOnDeleteWhenThereArePendingPermissions() {
        mockApprovalValidation(applicationProperties, true);
        String functionGroupId = "FG-01";
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(functionGroupId);
        mockExistPendingPermission(functionGroupId, true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.deleteFunctionGroup(functionGroupId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_073.getErrorMessage(), ERR_ACC_073.getErrorCode()));
    }

    @Test
      void shouldThrowsBadRequestForInvalidFunctionGroup() {
        String functionGroupId = "1";
        when(functionGroupJpaRepository.findById(eq(functionGroupId)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(functionGroupId, new FunctionGroupBase()
                .withName("name")));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestRecordAlreadyPresentInPendingTableUpdateFunctionGroup() {
        mockApprovalValidation(applicationProperties, true);
        String functionGroupId = "1";

        when(approvalFunctionGroupRefJpaRepository.existsByFunctionGroupId(eq(functionGroupId)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(functionGroupId, new FunctionGroupBase()
                .withName("name")));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_101.getErrorMessage(), ERR_ACC_101.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestRecordAlreadyPresentInPendingTableOnUpdateFunctionGroupWithoutLegalEntity() {
        mockApprovalValidation(applicationProperties, true);
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement().withId("serviceAgreementId");
        FunctionGroup functionGroup = new FunctionGroup().withServiceAgreement(serviceAgreement)
            .withId(functionGroupId);
        when(functionGroupJpaRepository.findByIdAndTypeNot(functionGroupId, FunctionGroupType.SYSTEM))
            .thenReturn(Optional.of(functionGroup));
        when(approvalFunctionGroupRefJpaRepository.existsByFunctionGroupId(eq(functionGroupId)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroupWithoutLegalEntity(functionGroupId, new FunctionGroupBase().withName("name")));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_101.getErrorMessage(), ERR_ACC_101.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestRecordServiceAgreementOfFunctionGroupIsPending() {
        mockApprovalValidation(applicationProperties, true);
        String serviceAgreementId = "1SA";
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement().withId(serviceAgreementId);
        FunctionGroup functionGroup = new FunctionGroup().withServiceAgreement(serviceAgreement)
            .withId(functionGroupId);
        when(functionGroupJpaRepository.findByIdAndTypeNot(functionGroupId, FunctionGroupType.SYSTEM))
            .thenReturn(Optional.of(functionGroup));
        when(approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroupWithoutLegalEntity(functionGroupId,
                new FunctionGroupBase().withName("name").withServiceAgreementId(serviceAgreementId)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_107.getErrorMessage(), ERR_ACC_107.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestForInvalidFunctionGroupWithoutLegalEntity() {
        String functionGroupId = "1";
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq(functionGroupId), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> functionGroupServiceImpl
            .updateFunctionGroupWithoutLegalEntity(functionGroupId, new FunctionGroupBase().withName("name")));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestForInvalidLegalEntityOfFunctionGroup() {
        String functionGroupId = "1";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(of(
                getFunctionGroup(functionGroupId, null, null, new LinkedHashSet<>(), FunctionGroupType.DEFAULT, null)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(functionGroupId, new FunctionGroupBase()));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_025.getErrorMessage(), ERR_ACQ_025.getErrorCode()));
    }

    @Test
    void shouldThrowsBadRequestForInvalidServiceAgreementOfFunctionGroup() {
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA ID";
        serviceAgreement.setId(serviceAgreementId);
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        null,
                        null,
                        new LinkedHashSet<>(),
                        FunctionGroupType.DEFAULT, serviceAgreement)
                )
            );

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(functionGroupId, new FunctionGroupBase()
                .withServiceAgreementId("ANOTHER ID")));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_025.getErrorMessage(), ERR_ACQ_025.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupNameAlreadyExists() {
        String id = "123";
        String name = "existing name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        when(functionGroupJpaRepository.findById(eq(id)))
            .thenReturn(
                of(
                    getFunctionGroup(id,
                        name,
                        null,
                        new LinkedHashSet<>(),
                        FunctionGroupType.DEFAULT, serviceAgreement)
                )
            );
        String nameToBeUpdated = "new name to be updated";
        when(functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(eq(nameToBeUpdated), eq(serviceAgreement.getId()), eq(id)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(id, new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId).withName(nameToBeUpdated)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupStartDateNotAfterServiceAgreementStartDate() {
        String id = "123";
        String name = "name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setStartDate(new Date(1));
        when(functionGroupJpaRepository.findById(eq(id)))
            .thenReturn(
                of(
                    getFunctionGroup(id,
                        name,
                        null,
                        new LinkedHashSet<>(),
                        FunctionGroupType.DEFAULT, serviceAgreement)
                )
            );
        Date startDateToBeUpdated = new Date(0);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(id, new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId).withName(name).withValidFrom(startDateToBeUpdated)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    void shouldUpdateFunctionGroupWhenFunctionGroupStartDateAfterServiceAgreementStartDate() {
        String id = "123";
        String name = "name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setStartDate(new Date(1));
        when(functionGroupJpaRepository.findById(eq(id)))
            .thenReturn(of(
                getFunctionGroup(id, name, null, new LinkedHashSet<>(), FunctionGroupType.DEFAULT, serviceAgreement)));
        Date startDateToBeUpdated = new Date(1111);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        functionGroupServiceImpl
            .updateFunctionGroup(id, new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId).withName(name).withValidFrom(startDateToBeUpdated));

        ArgumentCaptor<FunctionGroup> captor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(captor.capture());

        FunctionGroup functionGroup = captor.getValue();
        assertEquals(functionGroup.getId(), id);
        assertEquals(functionGroup.getServiceAgreementId(), serviceAgreementId);
        assertEquals(functionGroup.getName(), name);
        assertEquals(functionGroup.getStartDate(), startDateToBeUpdated);
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupEndDateNotBeforeServiceAgreementEndDate() {
        String id = "123";
        String name = "name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setEndDate(new Date(0));
        when(functionGroupJpaRepository.findById(eq(id)))
            .thenReturn(of(
                getFunctionGroup(id, name, null, new LinkedHashSet<>(), FunctionGroupType.DEFAULT, serviceAgreement)));
        Date endDateToBeUpdated = new Date(1);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroup(id, new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId).withName(name).withValidUntil(endDateToBeUpdated)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    void shouldUpdateFunctionGroupWhenEndDateBeforeServiceAgreementEndDate() {
        String id = "123";
        String name = "name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setEndDate(new Date(1111));
        when(functionGroupJpaRepository.findById(eq(id)))
            .thenReturn(of(
                getFunctionGroup(id, name, null, new LinkedHashSet<>(), FunctionGroupType.DEFAULT, serviceAgreement)));
        Date endDateToBeUpdated = new Date(1);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        functionGroupServiceImpl
            .updateFunctionGroup(id, new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId).withName(name).withValidUntil(endDateToBeUpdated));

        ArgumentCaptor<FunctionGroup> captor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(captor.capture());

        FunctionGroup functionGroup = captor.getValue();
        assertEquals(functionGroup.getId(), id);
        assertEquals(functionGroup.getServiceAgreementId(), serviceAgreementId);
        assertEquals(functionGroup.getName(), name);
        assertEquals(captor.getValue().getEndDate(), endDateToBeUpdated);
    }

    @Test
    void shouldRemoveDeletePrivilegeFromFunctionGroupAndAssignViewAndCreatePrivileges() {
        String functionGroupId = "functionGroupId";
        String functionGroupName = "functionGroupName";
        String businessFunctionId = "businessFunctionId";
        String serviceAgreementId = "SA ID";
        String viewPrivilegeName = "view";
        String createPrivilegeName = "create";
        String viewPrivilegeId = "viewId";
        String createPrivilegeId = "createId";

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(Set.of(viewPrivilegeId, createPrivilegeId));
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        PrivilegeDto privilegeView = new PrivilegeDto();
        privilegeView.setPrivilege(viewPrivilegeName);

        PrivilegeDto privilegeCreate = new PrivilegeDto();
        privilegeCreate.setPrivilege(createPrivilegeName);

        Permission permission = new Permission();
        permission.setFunctionId(businessFunctionId);
        permission.setAssignedPrivileges(Arrays.asList(privilegeView, privilegeCreate));

        FunctionGroupBase functionGroupRequestBody = new FunctionGroupBase();
        functionGroupRequestBody.withName(functionGroupName);
        functionGroupRequestBody.setServiceAgreementId(serviceAgreementId);
        functionGroupRequestBody.setPermissions(singletonList(permission));

        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setFunctionGroupId(functionGroupId);
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId("deletePrivilegeId");

        FunctionGroup existedFunctionGroup = new FunctionGroup();
        existedFunctionGroup.setId(functionGroupId);
        existedFunctionGroup.setName(functionGroupName);
        existedFunctionGroup.setPermissions(Set.of(groupedFunctionPrivilege));
        existedFunctionGroup.setType(FunctionGroupType.DEFAULT);
        existedFunctionGroup.setServiceAgreementId(serviceAgreementId);
        existedFunctionGroup.setServiceAgreement(serviceAgreement);

        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setId(businessFunctionId);

        ApplicableFunctionPrivilege afpView = new ApplicableFunctionPrivilege();
        afpView.setBusinessFunction(businessFunction);
        afpView.setPrivilegeName(viewPrivilegeName);
        afpView.setId("viewAfpId");

        ApplicableFunctionPrivilege afpCreate = new ApplicableFunctionPrivilege();
        afpCreate.setBusinessFunction(businessFunction);
        afpCreate.setPrivilegeName(createPrivilegeName);
        afpCreate.setId("createAfpId");

        ApplicableFunctionPrivilege afpDeleteToRemove = new ApplicableFunctionPrivilege();
        afpDeleteToRemove.setBusinessFunction(businessFunction);
        afpDeleteToRemove.setPrivilegeName("delete");
        afpDeleteToRemove.setId("deletePrivilegeId");

        when(functionGroupJpaRepository.findById(functionGroupId)).thenReturn(of(existedFunctionGroup));
        when(businessFunctionCache
            .getByFunctionAndPrivilege(businessFunctionId, Arrays.asList(viewPrivilegeName, createPrivilegeName)))
            .thenReturn(Arrays.asList(viewPrivilegeId, createPrivilegeId));
        when(businessFunctionCache.getAllApplicableFunctionPrivileges())
            .thenReturn(Arrays.asList(afpView, afpCreate, afpDeleteToRemove));
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR))
            .thenReturn(Optional.of(serviceAgreement));
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(businessFunctionId,
                Arrays.asList(viewPrivilegeName, createPrivilegeName)))
            .thenReturn(Arrays.asList(afpView, afpCreate));
        when(businessFunctionCache.getApplicableFunctionPrivileges(Set.of("viewAfpId", "createAfpId")))
            .thenReturn(Set.of(afpView, afpCreate));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId, functionGroupRequestBody);

        ArgumentCaptor<FunctionGroup> functionGroupCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(functionGroupCaptor.capture());

        FunctionGroup updatedFunctionGroup = functionGroupCaptor.getValue();
        List<String> afpIdsFromUpdatedFunctionGroup = updatedFunctionGroup.getPermissions().stream()
            .map(FunctionGroupItem::getApplicableFunctionPrivilegeId).collect(Collectors.toList());
        assertThat(afpIdsFromUpdatedFunctionGroup, hasItems("viewAfpId", "createAfpId"));
    }

    @Test
    void shouldRemoveApprovePrivilegeFromFunctionGroupAndRemoveAssociatedSelfApprovalPoliciesWithUafgCombinations() {
        String functionGroupId = "functionGroupId";
        String functionGroupName = "functionGroupName";
        String businessFunctionId = "businessFunctionId";
        String serviceAgreementId = "SA ID";
        String approveAfpId = "approveAfpId";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        FunctionGroupBase functionGroupRequestBody = new FunctionGroupBase();
        functionGroupRequestBody.withName(functionGroupName);
        functionGroupRequestBody.setServiceAgreementId(serviceAgreementId);

        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setFunctionGroupId(functionGroupId);
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(approveAfpId);

        FunctionGroup existedFunctionGroup = new FunctionGroup();
        existedFunctionGroup.setId(functionGroupId);
        existedFunctionGroup.setName(functionGroupName);
        existedFunctionGroup.setPermissions(Set.of(groupedFunctionPrivilege));
        existedFunctionGroup.setType(FunctionGroupType.DEFAULT);
        existedFunctionGroup.setServiceAgreementId(serviceAgreementId);
        existedFunctionGroup.setServiceAgreement(serviceAgreement);

        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setId(businessFunctionId);

        ApplicableFunctionPrivilege afpApproveToDelete = new ApplicableFunctionPrivilege();
        afpApproveToDelete.setBusinessFunction(businessFunction);
        afpApproveToDelete.setPrivilegeName("approve");
        afpApproveToDelete.setId(approveAfpId);

        FunctionGroupItemEntity functionGroupItemEntity = new FunctionGroupItemEntity();
        functionGroupItemEntity.setApplicableFunctionPrivilege(afpApproveToDelete);

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setFunctionGroupItem(functionGroupItemEntity);

        UserAssignedFunctionGroupCombination uafgCombination = new UserAssignedFunctionGroupCombination();
        uafgCombination.setSelfApprovalPolicies(Sets.newHashSet(selfApprovalPolicy));

        when(functionGroupJpaRepository.findById(functionGroupId)).thenReturn(of(existedFunctionGroup));
        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(Collections.emptyList());
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR))
            .thenReturn(Optional.of(serviceAgreement));
        when(businessFunctionCache.getApplicableFunctionPrivileges(Collections.emptySet()))
            .thenReturn(Collections.emptySet());
        when(userAssignedCombinationRepository.findAllCombinationsByFunctionGroupId(functionGroupId))
            .thenReturn(singletonList(uafgCombination));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId, functionGroupRequestBody);

        verify(userAssignedCombinationRepository, times(1)).delete(uafgCombination);

        ArgumentCaptor<FunctionGroup> functionGroupCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(functionGroupCaptor.capture());

        FunctionGroup updatedFunctionGroup = functionGroupCaptor.getValue();
        assertTrue(updatedFunctionGroup.getPermissions().isEmpty());
    }

    @Test
    void shouldRemoveApprovePrivilegeFromFunctionGroupAndRemoveAssociatedSelfApprovalPoliciesAndRestorePolicies() {
        String functionGroupId = "functionGroupId";
        String functionGroupName = "functionGroupName";
        String businessFunctionIdOne = "businessFunctionIdOne";
        String businessFunctionIdTwo = "businessFunctionIdTwo";
        String serviceAgreementId = "SA ID";
        String approveAfpIdOne = "approveAfpIdOne";
        String approveAfpIdTwo = "approveAfpIdTwo";
        String approvePrivilegeName = "approve";

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(Set.of(approveAfpIdOne));
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        PrivilegeDto privilegeApprove = new PrivilegeDto();
        privilegeApprove.setPrivilege(approvePrivilegeName);

        Permission permission = new Permission();
        permission.setFunctionId(businessFunctionIdOne);
        permission.setAssignedPrivileges(Arrays.asList(privilegeApprove));

        FunctionGroupBase functionGroupRequestBody = new FunctionGroupBase();
        functionGroupRequestBody.withName(functionGroupName);
        functionGroupRequestBody.setServiceAgreementId(serviceAgreementId);
        functionGroupRequestBody.setPermissions(Arrays.asList(permission));

        GroupedFunctionPrivilege groupedFunctionPrivilegeOne = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeOne.setFunctionGroupId(functionGroupId);
        groupedFunctionPrivilegeOne.setApplicableFunctionPrivilegeId(approveAfpIdOne);

        GroupedFunctionPrivilege groupedFunctionPrivilegeTwo = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeTwo.setFunctionGroupId(functionGroupId);
        groupedFunctionPrivilegeTwo.setApplicableFunctionPrivilegeId(approveAfpIdTwo);

        FunctionGroup existedFunctionGroup = new FunctionGroup();
        existedFunctionGroup.setId(functionGroupId);
        existedFunctionGroup.setName(functionGroupName);
        existedFunctionGroup.setType(FunctionGroupType.DEFAULT);
        existedFunctionGroup.setServiceAgreementId(serviceAgreementId);
        existedFunctionGroup.setServiceAgreement(serviceAgreement);
        existedFunctionGroup.setPermissions(Set.of(groupedFunctionPrivilegeOne, groupedFunctionPrivilegeTwo));

        BusinessFunction businessFunctionOne = new BusinessFunction();
        businessFunctionOne.setId(businessFunctionIdOne);

        ApplicableFunctionPrivilege afpApprove = new ApplicableFunctionPrivilege();
        afpApprove.setBusinessFunction(businessFunctionOne);
        afpApprove.setPrivilegeName(approvePrivilegeName);
        afpApprove.setId(approveAfpIdOne);

        FunctionGroupItemEntity functionGroupItemEntityOne = new FunctionGroupItemEntity();
        functionGroupItemEntityOne.setApplicableFunctionPrivilege(afpApprove);

        SelfApprovalPolicy selfApprovalPolicyOne = new SelfApprovalPolicy();
        selfApprovalPolicyOne.setFunctionGroupItem(functionGroupItemEntityOne);

        BusinessFunction businessFunctionTwo = new BusinessFunction();
        businessFunctionTwo.setId(businessFunctionIdTwo);

        ApplicableFunctionPrivilege afpApproveToDelete = new ApplicableFunctionPrivilege();
        afpApproveToDelete.setBusinessFunction(businessFunctionTwo);
        afpApproveToDelete.setPrivilegeName(approvePrivilegeName);
        afpApproveToDelete.setId(approveAfpIdTwo);

        FunctionGroupItemEntity functionGroupItemEntityTwo = new FunctionGroupItemEntity();
        functionGroupItemEntityTwo.setApplicableFunctionPrivilege(afpApproveToDelete);

        SelfApprovalPolicy selfApprovalPolicyTwo = new SelfApprovalPolicy();
        selfApprovalPolicyTwo.setFunctionGroupItem(functionGroupItemEntityTwo);

        UserAssignedFunctionGroupCombination uafgCombination = new UserAssignedFunctionGroupCombination();
        uafgCombination.setSelfApprovalPolicies(Sets.newHashSet(selfApprovalPolicyOne, selfApprovalPolicyTwo));

        when(functionGroupJpaRepository.findById(functionGroupId)).thenReturn(of(existedFunctionGroup));
        when(businessFunctionCache
            .getByFunctionAndPrivilege(businessFunctionIdOne, Arrays.asList(approvePrivilegeName)))
            .thenReturn(Arrays.asList(approveAfpIdOne));
        when(businessFunctionCache.getAllApplicableFunctionPrivileges())
            .thenReturn(Arrays.asList(afpApprove, afpApproveToDelete));
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR))
            .thenReturn(Optional.of(serviceAgreement));
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(businessFunctionIdOne, Arrays.asList(approvePrivilegeName)))
            .thenReturn(Arrays.asList(afpApprove));

        when(businessFunctionCache.getApplicableFunctionPrivileges(Collections.emptySet()))
            .thenReturn(Collections.emptySet());
        when(userAssignedCombinationRepository.findAllCombinationsByFunctionGroupId(functionGroupId))
            .thenReturn(singletonList(uafgCombination));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId, functionGroupRequestBody);

        verify(userAssignedCombinationRepository, never()).delete(uafgCombination);

        ArgumentCaptor<FunctionGroup> functionGroupCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(functionGroupCaptor.capture());

        FunctionGroup updatedFunctionGroup = functionGroupCaptor.getValue();
        assertThat(updatedFunctionGroup.getPermissions().size(), is(1));
        assertThat(updatedFunctionGroup.getPermissions().iterator().next().getApplicableFunctionPrivilegeId(),
            is(approveAfpIdOne));
    }

    @Test
    void shouldThrowBadRequestWhenFunctionGroupNameAlreadyExistsWithoutLegalEntity() {
        String id = "123";
        String name = "existing name";
        String serviceAgreementId = "SA ID";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq(id), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(of(
                getFunctionGroup(id, name, null, new LinkedHashSet<>(), FunctionGroupType.DEFAULT, serviceAgreement)));
        String nameToBeUpdated = "new name to be updated";
        when(functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(eq(nameToBeUpdated), eq(serviceAgreement.getId()), eq(id)))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupServiceImpl
            .updateFunctionGroupWithoutLegalEntity(id,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId).withName(nameToBeUpdated)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestForNotExistingPrivileges() {
        String name = "name";
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        serviceAgreement.setId(serviceAgreementId);
        com.backbase.accesscontrol.domain.Privilege privilege = new com.backbase.accesscontrol.domain.Privilege();
        privilege.setName("edit");
        BusinessFunction businessFunction = new BusinessFunction();
        String functionId = "1001";
        businessFunction.setId(functionId);
        when(functionGroupJpaRepository.findById(eq(functionGroupId)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", businessFunction, privilege, false),
                                null)
                        ),
                        FunctionGroupType.DEFAULT, serviceAgreement)
                )
            );
        mockGetAllApplicableFunctionPrivileges(singletonList(functionId));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withName(name)
                    .withPermissions(getPermissions(
                        getPermission(
                            functionId,
                            getPrivilege("create")
                        )
                    ))
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestForNotExistingPrivilegesWithoutLegalEntity() {
        String name = "name";
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        serviceAgreement.setId(serviceAgreementId);
        com.backbase.accesscontrol.domain.Privilege privilege = new com.backbase.accesscontrol.domain.Privilege();
        privilege.setName("edit");
        BusinessFunction businessFunction = new BusinessFunction();
        String functionId = "1001";
        businessFunction.setId(functionId);
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq(functionGroupId), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", businessFunction, privilege, false),
                                null)
                        ),
                        FunctionGroupType.DEFAULT, serviceAgreement)
                )
            );
        mockGetAllBusinessFunctions(singletonList(functionId));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withName(name)
                    .withPermissions(getPermissions(
                        getPermission(
                            functionId,
                            getPrivilege("create")
                        )
                    ))
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldRemoveAllPermissions() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, false),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
            new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(name)
                .withPermissions(getPermissions())
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(0))
            )
        );
    }

    @Test
    void shouldRemoveAllPermissionsWithoutLegalEntity() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, false),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.of(serviceAgreement));

        functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(name)
                .withPermissions(getPermissions())
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(0))
            )
        );
    }

    @Test
    void shouldUpdatePermissions() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""), true)));
        mockGetAllApplicableFunctionPrivileges(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
            new FunctionGroupBase()
                .withName(name)
                .withServiceAgreementId(serviceAgreementId)
                .withPermissions(PermissionUtil.getPermissions(
                    getPermission("1",
                        getPrivilege("create"))
                ))
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldUpdatePermissionsWithAssignablePermissionSetCheck() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        String name = "name";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""), true)));
        mockGetAllApplicableFunctionPrivileges(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1", asList("create"), asList("1"));

        functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
            new FunctionGroupBase()
                .withName(name)
                .withServiceAgreementId(serviceAgreementId)
                .withPermissions(PermissionUtil.getPermissions(
                    getPermission("1",
                        getPrivilege("create"))
                ))
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldUpdatePermissionsWithoutLegalEntityWithAssignablePermissionSetCheck() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        String name = "name";
        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(of(
                getFunctionGroup(functionGroupId, name, null,
                    getGroupedFunctionPrivileges(
                        getGroupedFunctionPrivilege("grFunPrId",
                            getApplicableFunctionPrivilege("1", null, null, true),
                            null)),
                    FunctionGroupType.DEFAULT, serviceAgreement)));

        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create", "view"))))
            .thenReturn(
                List.of(
                    getApplicableFunctionPrivilege("1", null,
                        getPrivilege(null, "create", ""), true),
                    getApplicableFunctionPrivilege("2", null,
                        getPrivilege(null, "view", ""), true)));
        mockGetAllBusinessFunctions(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1", asList("create", "view"),
            asList("1", "2"));

        functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            new FunctionGroupBase()
                .withName(name)
                .withServiceAgreementId(serviceAgreementId)
                .withPermissions(PermissionUtil.getPermissions(
                    getPermission("1",
                        getPrivilege("create"), getPrivilege("view")))
                )
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldUpdatePermissionsWithoutLegalEntity() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""), true)));
        mockGetAllBusinessFunctions(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            new FunctionGroupBase()
                .withName(name)
                .withServiceAgreementId(serviceAgreementId)
                .withPermissions(PermissionUtil.getPermissions(
                    getPermission("1",
                        getPrivilege("create"))
                ))
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldThrowBadRequestExceptionOnUpdateFunctionGroupWithoutLegalEntityWhenNotAllFunctionsExistsInAps() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(singletonList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        String name = "name";
        when(functionGroupJpaRepository.findByIdAndTypeNot(eq(functionGroupId), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );

        mockGetAllBusinessFunctions(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1", asList("create", "view"),
            asList("1", "2"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withPermissions(PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"), getPrivilege("view")))
                    )
            ));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionOnUpdateFunctionGroupWhenNotAllFunctionsExistsInAps() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);

        String name = "name";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        mockGetAllApplicableFunctionPrivileges(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1", asList("create", "view"),
            asList("1", "2"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withPermissions(PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"),
                            getPrivilege("view")
                        )))));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionOnUpdateFunctionGroupWhenNotAllFunctionsExists() {
        String functionGroupId = "FG-01";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withPermissions(PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"))
                    ))
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionOnUpdateFunctionGroupWhenNotAllFunctionsExistsWithoutLegalEntity() {
        String functionGroupId = "FG-01";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(
                            getGroupedFunctionPrivilege("grFunPrId",
                                getApplicableFunctionPrivilege("1", null, null, true),
                                null)
                        ),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withPermissions(PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"))
                    ))
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    void shouldCreatePermissions() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findById(anyString()))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        ApplicableFunctionPrivilege afp = getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""),
            true);
        when(businessFunctionCache.findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(List.of(afp));
        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(newHashSet("1")))).thenReturn(newHashSet(afp));
        mockGetAllApplicableFunctionPrivileges(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        functionGroupServiceImpl.updateFunctionGroup(functionGroupId,
            new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(name)
                .withPermissions(
                    PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"))
                    )
                )
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldCreatePermissionsWithoutLegalEntity() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";
        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(
                of(
                    getFunctionGroup(functionGroupId,
                        name,
                        null,
                        getGroupedFunctionPrivileges(),
                        FunctionGroupType.DEFAULT,
                        serviceAgreement)
                )
            );
        ApplicableFunctionPrivilege afp = getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""),
            true);
        when(businessFunctionCache.findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(List.of(afp));
        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(newHashSet("1")))).thenReturn(newHashSet(afp));
        mockGetAllBusinessFunctions(asList("1"));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(name)
                .withPermissions(
                    PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"))
                    )
                )
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("description", is("")),
                hasProperty("type", is(FunctionGroupType.DEFAULT)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", hasSize(1)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldUpdateJobRoleTemplate() {
        ApplicableFunctionPrivilege afp = getApplicableFunctionPrivilege("1", null, getPrivilege(null, "create", ""),
            true);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        String functionGroupId = "1";
        serviceAgreement.setId(serviceAgreementId);
        String name = "name";

        FunctionGroup functionGroup = getFunctionGroup(functionGroupId,
            name,
            null,
            getGroupedFunctionPrivileges(),
            FunctionGroupType.TEMPLATE,
            serviceAgreement);
        functionGroup.setAssignablePermissionSetId(assignablePermissionSet.getId());

        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(of(functionGroup));

        when(businessFunctionCache.findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1"), eq(List.of("create"))))
            .thenReturn(List.of(afp));
        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(newHashSet("1")))).thenReturn(newHashSet(afp));
        mockGetAllBusinessFunctions(asList("1"));
        mockGetApsById(1L, APS_PERMISSIONS_EXTENDED, Optional.of(assignablePermissionSet));

        functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            new FunctionGroupBase()
                .withServiceAgreementId(serviceAgreementId)
                .withName(name)
                .withPermissions(
                    PermissionUtil.getPermissions(
                        getPermission("1",
                            getPrivilege("create"))
                    )
                )
        );

        ArgumentCaptor<FunctionGroup> fgCaptor = ArgumentCaptor.forClass(FunctionGroup.class);
        verify(functionGroupJpaRepository).saveAndFlush(fgCaptor.capture());

        FunctionGroup savedFg = fgCaptor.getValue();
        assertThat(savedFg,
            allOf(
                hasProperty("id", is(functionGroupId)),
                hasProperty("name", is(name)),
                hasProperty("type", is(FunctionGroupType.TEMPLATE)),
                hasProperty("serviceAgreement", is(hasProperty("id", is(serviceAgreementId)))),
                hasProperty("serviceAgreementId", is(serviceAgreementId)),
                hasProperty("permissions", containsInAnyOrder(
                    allOf(
                        hasProperty("functionGroupId", is(functionGroupId)),
                        hasProperty("applicableFunctionPrivilegeId", is("1"))
                    )
                ))
            )
        );
    }

    @Test
    void shouldThrowsBadRequestForNotExistingPrivilegesUpdateJrt() {
        String name = "name";
        String functionGroupId = "1";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "sa id";
        serviceAgreement.setId(serviceAgreementId);
        com.backbase.accesscontrol.domain.Privilege privilege = new com.backbase.accesscontrol.domain.Privilege();
        privilege.setName("edit");
        BusinessFunction businessFunction = new BusinessFunction();
        String functionId = "1001";
        businessFunction.setId(functionId);

        ApplicableFunctionPrivilege afp = getApplicableFunctionPrivilege("1", businessFunction,
            getPrivilege(null, "view", ""),
            true);
        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        FunctionGroup functionGroup = getFunctionGroup(functionGroupId,
            name,
            null,
            getGroupedFunctionPrivileges(),
            FunctionGroupType.TEMPLATE, serviceAgreement);
        functionGroup.setAssignablePermissionSetId(assignablePermissionSet.getId());

        when(functionGroupJpaRepository.findByIdAndTypeNot(anyString(), eq(FunctionGroupType.SYSTEM)))
            .thenReturn(of(functionGroup));
        mockGetAllBusinessFunctions(singletonList(functionId));
        mockGetApsById(1L, APS_PERMISSIONS_EXTENDED, Optional.of(assignablePermissionSet));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.updateFunctionGroupWithoutLegalEntity(functionGroupId,
                new FunctionGroupBase()
                    .withServiceAgreementId(serviceAgreementId)
                    .withName(name)
                    .withPermissions(getPermissions(
                        getPermission(
                            functionId,
                            getPrivilege("create")
                        )
                    ))
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldCallCreateFunctionGroupFromFunctionGroupJpaRepository() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        functionGroupServiceImpl.addFunctionGroup(functionGroupBase);

        verify(functionGroupJpaRepository, times(1)).save(any(FunctionGroup.class));
    }

    @Test
    void shouldCreateFunctionGroupOfTypeTemplate() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsId(new BigDecimal(1));

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        mockGetApsById(1L, APS_PERMISSIONS_EXTENDED, Optional.of(assignablePermissionSet));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.TEMPLATE,
            false);

        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1010"), eq(List.of("view", "create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "view", ""), true),
                    getApplicableFunctionPrivilege("2", null, getPrivilege(null, "create", ""), true)));

        functionGroupServiceImpl.addFunctionGroup(functionGroupBase);

        verify(functionGroupJpaRepository).save(functionGroupCaptor.capture());

        FunctionGroup functionGroup = functionGroupCaptor.getValue();

        assertEquals("Function Group 1", functionGroup.getName());
        assertEquals(FunctionGroupType.TEMPLATE, functionGroup.getType());
        assertThat(functionGroup.getPermissions(), containsInAnyOrder(allOf(
            hasProperty("applicableFunctionPrivilegeId", equalTo("1"))), allOf(
            hasProperty("applicableFunctionPrivilegeId", equalTo("2")))
        ));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupOfTypeTemplateWhenAssociatedApsByIdDoesntExist() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsId(new BigDecimal(1));

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.TEMPLATE,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        mockGetApsById(1L, APS_PERMISSIONS_EXTENDED, Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupOfTypeTemplateWhenServiceAgreementIsOfTypeCSA() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(false);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsId(new BigDecimal(1));

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.TEMPLATE,
            false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_103.getErrorMessage(), ERR_ACC_103.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupOfTypeTemplateWhenApsByIdIsNullAndApsNameIsNull() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsId(null)
            .withApsName(null);

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.TEMPLATE,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_104.getErrorMessage(), ERR_ACC_104.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupOfTypeTemplateWhenAssociatedApsByNameDoesntExist() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsName("apsName");

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.TEMPLATE,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        mockGetApsByName("apsName", APS_PERMISSIONS_EXTENDED, Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupOfTypeTemplateWhenPrivilegesAreNotSubsetOfAssociatedAps() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";

        String name = "Function Group 1";

        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setMaster(true);

        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withApsName("apsName");

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        mockGetApsByName("apsName", APS_PERMISSIONS_EXTENDED, Optional.of(assignablePermissionSet));

        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1010"), eq(List.of("view", "create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "view", ""), true)));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupOfTypeTemplateStartDateIsBeforeEndDate() {
        String functionId = "1010";
        String functionGroupName = "Function Group 1";
        String serviceAgreementId = "SA-01";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(functionGroupName)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(Type.TEMPLATE)
            .withPermissions(permissions)
            .withValidFrom(new Date(20000))
            .withValidUntil(new Date(10000));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(functionGroupName, serviceAgreementId,
            FunctionGroupType.TEMPLATE, false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestWhenSavingFunctionGroupIfTheNameExistsInPendingApprovalTables() {
        mockApprovalValidation(applicationProperties, true);
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = singletonList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(singletonList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(name, serviceAgreementId, true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_100.getErrorMessage(), ERR_ACC_100.getErrorCode()));
    }

    @Test
    void shouldCallCreateFunctionGroupWithAssignablePermissionSetCheck() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(asList("1010"));
        when(businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(eq("1010"), eq(List.of("view", "create"))))
            .thenReturn(
                List.of(getApplicableFunctionPrivilege("1", null, getPrivilege(null, "view", ""), true),
                    getApplicableFunctionPrivilege("2", null, getPrivilege(null, "create", ""), true)));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1010", asList("view", "create"),
            asList("1", "2"));

        functionGroupServiceImpl.addFunctionGroup(functionGroupBase);

        verify(functionGroupJpaRepository, times(1)).save(any(FunctionGroup.class));
    }

    @Test
    void shouldThrowBadRequestOnAddFunctionGroupWhenServiceAgreementDoesNotExist() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestOnAddFunctionGroupWhenNotAllFunctionsExists() {
        String functionId = "1010";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        mockGetAllApplicableFunctionPrivileges(new ArrayList<>());
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestOnAddFunctionGroupWhenNotAllFunctionPrivilegesAreApplicable() {
        String functionId = "1010";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        List<PrivilegeDto> privilegeList = asList(new PrivilegeDto().withPrivilege("create"));
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupWithGivenNameAlreadyExists() {
        String functionId = "1010";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(name);

        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupStartDateIsBeforeServiceAgreementStartDate() {
        String functionId = "1010";
        String functionGroupName = "Function Group 1";
        String serviceAgreementId = "SA-01";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(functionGroupName)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions)
            .withValidFrom(new Date(0));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setStartDate(new Date(1111));
        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(functionGroupName, serviceAgreementId,
            FunctionGroupType.DEFAULT, false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFunctionGroupEndDateIsAfterServiceAgreementEndDate() {
        String functionId = "1010";
        String name = "Function Group 1";
        String serviceAgreementId = "SA-01";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions)
            .withValidUntil(new Date(1));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setEndDate(new Date(0));
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(name);
        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenPrivilegesNotInAssignablePermissionSet() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        PrivilegeDto privilege1 = new PrivilegeDto();
        privilege1.setPrivilege("view");
        PrivilegeDto privilege2 = new PrivilegeDto();
        privilege2.setPrivilege("create");
        PrivilegeDto privilege3 = new PrivilegeDto();
        privilege3.setPrivilege("delete");
        List<PrivilegeDto> privilegeList = asList(privilege1, privilege2, privilege3);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        Set<String> applicableFunctionPrivilegeIds = new HashSet<>(asList("1", "2"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setPermissions(applicableFunctionPrivilegeIds);
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        assignablePermissionSets.add(assignablePermissionSet);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSets);
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        ApplicableFunctionPrivilege applicableFunctionPrivilege1 = new ApplicableFunctionPrivilege();
        ApplicableFunctionPrivilege applicableFunctionPrivilege2 = new ApplicableFunctionPrivilege();
        BusinessFunction businessFunction1 = new BusinessFunction();
        businessFunction1.setId("1010");

        applicableFunctionPrivilege1.setId("1");
        applicableFunctionPrivilege1.setBusinessFunction(businessFunction1);
        applicableFunctionPrivilege1.setPrivilegeName("view");
        applicableFunctionPrivilege2.setId("2");
        applicableFunctionPrivilege2.setBusinessFunction(businessFunction1);
        applicableFunctionPrivilege2.setPrivilegeName("create");

        mockGetAllApplicableFunctionPrivileges(asList("1010"));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1010", asList("view", "create", "delete"), asList("1", "2", "3"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));

        verify(functionGroupJpaRepository, times(0)).save(any(FunctionGroup.class));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAssignablePermissionSetItemListIsEmpty() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        privilegeList.add(new PrivilegeDto().withPrivilege("view"));
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withName(name)
            .withDescription("Function Group description")
            .withServiceAgreementId(serviceAgreementId)
            .withType(FunctionGroupBase.Type.DEFAULT)
            .withPermissions(permissions);

        mockGetAllApplicableFunctionPrivileges(asList(functionId));
        mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(name, serviceAgreementId, FunctionGroupType.DEFAULT,
            false);
        mockGetServiceAgreementRelatedWithFunctionGroupById(serviceAgreementId, Optional.ofNullable(serviceAgreement));
        mockAssignablePermissionSets("1010", asList("view"), asList("1"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> functionGroupServiceImpl.addFunctionGroup(functionGroupBase));

        verify(functionGroupJpaRepository, times(0)).save(any(FunctionGroup.class));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode()));
    }

    @Test
    void getFunctionGroupsByServiceAgreementId() {
        LegalEntity legalEntity = LegalEntityUtil.createLegalEntity("LE-01", "LE-name",
            "LE-01-EX", null, LegalEntityType.BANK);
        String serviceAgreementId = "SA-01";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setType(AssignablePermissionType.REGULAR_USER_DEFAULT);
        serviceAgreement.setPermissionSetsRegular(asSet(assignablePermissionSet));
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.SYSTEM);
        functionGroup.setServiceAgreement(serviceAgreement);
        List<FunctionGroup> functionGroups = new ArrayList<>();
        functionGroups.add(functionGroup);

        doReturn(functionGroups).when(functionGroupJpaRepository)
            .findByServiceAgreementId(eq(serviceAgreementId));

        doReturn(Optional.of(serviceAgreement))
            .when(serviceAgreementJpaRepository).findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS));

        doReturn(Optional.of(serviceAgreement))
            .when(serviceAgreementJpaRepository)
            .findByCreatorLegalEntityIdAndIsMaster(eq(legalEntity.getId()), eq(true),
                eq(SERVICE_AGREEMENT_WITH_FGS));

        doReturn(asList(functionGroup))
            .when(functionGroupJpaRepository).findByCreatorLegalEntityIdAndAps(eq(legalEntity.getId()),
            eq(asSet(1L)), eq(FunctionGroupType.TEMPLATE));

        List<FunctionGroupsGetResponseBody> responseFromService = functionGroupServiceImpl
            .getFunctionGroupsByServiceAgreementId(serviceAgreementId);

        assertEquals(functionGroups.size(), responseFromService.size());
        assertEquals(functionGroup.getServiceAgreement().getId(), responseFromService.get(0).getServiceAgreementId());

    }

    @Test
    void getFunctionGroupsByServiceAgreementIdAndApprovalIdPopulatedWhenSomeFgIsInPendingState() {
        String serviceAgreementId = "SA-01";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity(new LegalEntity()
            .withId("creator"));
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1L);
        serviceAgreement.setPermissionSetsRegular(asSet(assignablePermissionSet));
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);
        List<FunctionGroup> functionGroups = new ArrayList<>();
        functionGroups.add(functionGroup);
        ApprovalFunctionGroup functionGroupApproval = new ApprovalFunctionGroup();
        functionGroupApproval.setApprovalId("approvalId");

        when(functionGroupJpaRepository
            .findByServiceAgreementId(eq(serviceAgreementId)))
            .thenReturn(functionGroups);

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId),
            eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS)))
            .thenReturn(Optional.of(serviceAgreement));

        when(serviceAgreementJpaRepository.findByCreatorLegalEntityIdAndIsMaster(eq("creator"), eq(true),
            eq(SERVICE_AGREEMENT_WITH_FGS)))
            .thenReturn(Optional.of(serviceAgreement));

        when(functionGroupJpaRepository.findByCreatorLegalEntityIdAndAps(eq("creator"), eq(asSet(1L)),
            eq(FunctionGroupType.TEMPLATE)))
            .thenReturn(asList(functionGroup));

        when(approvalFunctionGroupRefJpaRepository
            .findByFunctionGroupId(eq(functionGroup.getId())))
            .thenReturn(of(functionGroupApproval));

        List<FunctionGroupsGetResponseBody> responseFromService = functionGroupServiceImpl
            .getFunctionGroupsByServiceAgreementId(serviceAgreementId);

        assertEquals(functionGroups.size(), responseFromService.size());
        assertEquals(functionGroup.getServiceAgreement().getId(), responseFromService.get(0).getServiceAgreementId());
        assertEquals(functionGroupApproval.getApprovalId(), responseFromService.get(0).getApprovalId());
    }

    @Test
    void shouldCallCreateSystemFunctionGroupFromFunctionGroupJpaRepository() {
        String functionId = "1010";
        String serviceAgreementId = "SA-01";
        String name = "Function Group 1";
        String creatorLegalEntity = "le";
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        Permission permission = new Permission()
            .withFunctionId(functionId)
            .withAssignedPrivileges(privilegeList);
        List<Permission> permissions = asList(permission);

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(creatorLegalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setMaster(false);

        functionGroupServiceImpl.addSystemFunctionGroup(serviceAgreement, name, permissions);

        verify(functionGroupJpaRepository, times(1)).save(any(FunctionGroup.class));
    }

    @Test
    void shouldGetFunctionGroupsByNameAndServiceAgreementId() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        String name = "FG-01";
        serviceAgreement.setId(serviceAgreementId);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(name);
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);

        when(functionGroupJpaRepository.findByNameAndServiceAgreementId(eq(name), eq(serviceAgreementId)))
            .thenReturn(Optional.of(functionGroup));

        String responseFromService = functionGroupServiceImpl
            .getFunctionGroupsByNameAndServiceAgreementId(name, serviceAgreementId);

        assertEquals(functionGroup.getId(), responseFromService);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFunctionGroupNotExist() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        String name = "FG-01";
        serviceAgreement.setId(serviceAgreementId);
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(name);
        functionGroup.setDescription("description");
        functionGroup.setId("id");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);

        when(functionGroupJpaRepository.findByNameAndServiceAgreementId(eq(name), eq(serviceAgreementId)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> functionGroupServiceImpl.getFunctionGroupsByNameAndServiceAgreementId(name, serviceAgreementId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void shouldGetBusinessFunctionsByExternalServiceAgreementId() {
        String serviceAgreementId = "SA-01";
        String externalId = "ex-id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setExternalId(externalId);
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("APS 1");
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(asSet("1", "2"));
        serviceAgreement.setPermissionSetsRegular(asSet(assignablePermissionSet));

        BusinessFunction businessFunction1 = new BusinessFunction();
        businessFunction1.setId("1");
        ApplicableFunctionPrivilege afp1 = new ApplicableFunctionPrivilege();
        afp1.setBusinessFunction(businessFunction1);
        afp1.setId("1");
        afp1.setPrivilege(new com.backbase.accesscontrol.domain.Privilege("1", "p1", "p1"));

        ApplicableFunctionPrivilege afp2 = new ApplicableFunctionPrivilege();
        afp2.setId("2");
        afp2.setBusinessFunction(businessFunction1);
        afp2.setPrivilege(new com.backbase.accesscontrol.domain.Privilege("2", "p2", "p2"));

        when(serviceAgreementJpaRepository
            .findByExternalId(eq(externalId), eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)))
            .thenReturn(Optional.of(serviceAgreement));

        when(businessFunctionCache.getApplicableFunctionPrivileges(asSet("1", "2")))
            .thenReturn(
                asSet(afp1, afp2));

        List<FunctionsGetResponseBody> responseFromService = functionGroupServiceImpl
            .findAllBusinessFunctionsByServiceAgreement(externalId, true);

        assertEquals(1, responseFromService.size());
        assertEquals(2, responseFromService.get(0).getPrivileges().size());
    }

    @Test
    void shouldGetBusinessFunctionsByInternalServiceAgreementId() {
        String serviceAgreementId = "SA-01";
        String externalId = "ex-id";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setExternalId(externalId);
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("APS 1");
        assignablePermissionSet.setId(1L);
        assignablePermissionSet.setPermissions(asSet("1", "2"));
        serviceAgreement.setPermissionSetsRegular(asSet(assignablePermissionSet));

        BusinessFunction businessFunction1 = new BusinessFunction();
        businessFunction1.setId("1");
        ApplicableFunctionPrivilege afp1 = new ApplicableFunctionPrivilege();
        afp1.setBusinessFunction(businessFunction1);
        afp1.setId("1");
        afp1.setPrivilege(new com.backbase.accesscontrol.domain.Privilege("1", "p1", "p1"));

        ApplicableFunctionPrivilege afp2 = new ApplicableFunctionPrivilege();
        afp2.setId("2");
        afp2.setBusinessFunction(businessFunction1);
        afp2.setPrivilege(new com.backbase.accesscontrol.domain.Privilege("2", "p2", "p2"));

        when(serviceAgreementJpaRepository
            .findById(eq(serviceAgreementId), eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)))
            .thenReturn(Optional.of(serviceAgreement));

        when(businessFunctionCache.getApplicableFunctionPrivileges(asSet("1", "2")))
            .thenReturn(
                asSet(afp1, afp2));

        List<FunctionsGetResponseBody> responseFromService = functionGroupServiceImpl
            .findAllBusinessFunctionsByServiceAgreement(serviceAgreementId, false);

        assertEquals(1, responseFromService.size());
        assertEquals(2, responseFromService.get(0).getPrivileges().size());
    }

    @Test
    void shouldThrowNotFoundWhenServiceAgreementNotExistOnGetBusinessFunctionsByExternalId() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String externalId = "Some External";
        String serviceAgreementId = "SA-01";
        serviceAgreement.setId(serviceAgreementId);

        when(serviceAgreementJpaRepository
            .findByExternalId(eq(externalId), eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> functionGroupServiceImpl.findAllBusinessFunctionsByServiceAgreement(externalId, true));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    void shouldThrowNotFoundWhenServiceAgreementNotExistOnGetBusinessFunctionsByInternalId() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        String serviceAgreementId = "SA-01";
        serviceAgreement.setId(serviceAgreementId);

        when(serviceAgreementJpaRepository
            .findById(eq("some id"), eq(SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> functionGroupServiceImpl.findAllBusinessFunctionsByServiceAgreement("some id", false));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    private void mockGetServiceAgreementRelatedWithFunctionGroupById(String serviceAgreementId,
        Optional<ServiceAgreement> serviceAgreement) {
        when(serviceAgreementJpaRepository.findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR))
            .thenReturn(serviceAgreement);
    }

    private void mockGetFunctionGroupsInApprovalTmpTableByNameAndServiceAgreementId(String name,
        String serviceAgreementId, boolean response) {
        when(approvalFunctionGroupJpaRepository.existsByNameAndServiceAgreementId(name, serviceAgreementId))
            .thenReturn(response);
    }

    private void mockGetFunctionGroupsByNameAndServiceAgreementId(String name, String serviceAgreementId,
        boolean exists) {
        when(functionGroupJpaRepository
            .existsByNameAndServiceAgreementId(name, serviceAgreementId))
            .thenReturn(exists);
    }

    private void mockGetFunctionGroupsByNameAndServiceAgreementIdAndType(String name, String serviceAgreementId,
        FunctionGroupType type, boolean exists) {
        when(functionGroupJpaRepository.existsByNameAndServiceAgreementId(name, serviceAgreementId))
            .thenReturn(exists);
    }

    private void mockGetApsById(Long id, String entityGraph,
        Optional<AssignablePermissionSet> assignablePermissionSet) {
        when(assignablePermissionSetJpaRepository.findById(id, entityGraph))
            .thenReturn(assignablePermissionSet);
    }

    private void mockGetApsByName(String name, String entityGraph,
        Optional<AssignablePermissionSet> assignablePermissionSet) {
        when(assignablePermissionSetJpaRepository.findByName(name, entityGraph))
            .thenReturn(assignablePermissionSet);
    }

    private void mockGetAllBusinessFunctions(List<String> functionIds) {
        List<ApplicableFunctionPrivilege> applicableFunctionPrivileges = createBusinessFunctions(functionIds).stream()
            .map(businessFunction -> {
                ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
                applicableFunctionPrivilege.setBusinessFunction(businessFunction);
                return applicableFunctionPrivilege;
            }).collect(Collectors.toList());

        when(businessFunctionCache.getAllApplicableFunctionPrivileges())
            .thenReturn(applicableFunctionPrivileges);

    }

    private List<BusinessFunction> createBusinessFunctions(List<String> functionIds) {
        return functionIds.stream()
            .map(functionId -> {
                BusinessFunction businessFunction = new BusinessFunction();
                businessFunction.setId(functionId);
                businessFunction.setFunctionName(functionId);
                return businessFunction;
            }).collect(Collectors.toList());
    }

    private void mockGetAllApplicableFunctionPrivileges(List<String> functionIds) {
        List<ApplicableFunctionPrivilege> applicableFunctionPrivileges = createBusinessFunctions(functionIds).stream()
            .map(businessFunction -> {
                ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
                applicableFunctionPrivilege.setBusinessFunction(businessFunction);
                return applicableFunctionPrivilege;
            }).collect(Collectors.toList());

        when(businessFunctionCache.getAllApplicableFunctionPrivileges())
            .thenReturn(applicableFunctionPrivileges);
    }

    private void mockExistPendingPermission(String functionGroupId, boolean exists) {
        when(approvalUserContextAssignFunctionGroupJpaRepository.existsByFunctionGroupId(functionGroupId))
            .thenReturn(exists);
    }

    private void mockAssignablePermissionSets(String functionId, List<String> privileges,
        List<String> applicableFunctionPrivileges) {
        when(businessFunctionCache
            .getByFunctionAndPrivilege(functionId, privileges)).thenReturn(applicableFunctionPrivileges);
    }
}
