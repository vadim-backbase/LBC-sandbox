package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.ADMIN_USER_DEFAULT;
import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.REGULAR_USER_DEFAULT;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_087;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_088;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_089;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_090;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_091;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_092;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_094;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_102;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.PermissionSetsInServiceAgreements;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.mappers.PermissionSetMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementAssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSetServiceImplTest {

    @Mock
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Mock
    private ServiceAgreementAssignablePermissionSetJpaRepository serviceAgreementAssignablePermissionSetJpaRepository;
    @Mock
    private PermissionSetMapper permissionSetMapper;
    @Mock
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @InjectMocks
    private PermissionSetServiceImpl permissionSetService;

    @Test
    public void getPermissionSetFilteredByName() {
        String name = "name";

        permissionSetService.getPermissionSetFilteredByName(name);

        verify(assignablePermissionSetJpaRepository).findByNameContainingIgnoreCase(eq(name));
    }

    @Test
    public void shouldGetAllPermissionSet() {

        permissionSetService.getPermissionSetFilteredByName(null);

        verify(assignablePermissionSetJpaRepository).findAll();
    }

    public void shouldSavePermissionSet() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription")
            .withPermissions(asList(new PresentationPermissionSetItem().withFunctionId("id1")
                    .withPrivileges(asSet("view", "create", "delete")),
                new PresentationPermissionSetItem().withFunctionId("id2").withPrivileges(asSet("approve"))));

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setPermissions(asSet("afp-id1", "afp-id2", "afp-id3"));

        when(assignablePermissionSetJpaRepository.existsByName("apsName")).thenReturn(false);
        when(businessFunctionCache.haveValidPrivileges(anyString(), anyList())).thenReturn(true);
        when(permissionSetMapper.toDbModel(eq(permissionSet))).thenReturn(assignablePermissionSet);
        when(assignablePermissionSetJpaRepository.save(any(AssignablePermissionSet.class)))
            .thenReturn(assignablePermissionSet);

        BigDecimal persistenceInternalIdResponse = permissionSetService.save(permissionSet);

        verify(assignablePermissionSetJpaRepository, times(1)).save(eq(assignablePermissionSet));
        assertEquals(new BigDecimal(1234L), persistenceInternalIdResponse);
    }

    @Test
    public void shouldThrowBadRequestExceptionIfApsWithSpecifiedNameExist() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription")
            .withPermissions(asList(new PresentationPermissionSetItem().withFunctionId("id1")
                    .withPrivileges(asSet("view", "create", "delete")),
                new PresentationPermissionSetItem().withFunctionId("id2").withPrivileges(asSet("approve"))));

        when(assignablePermissionSetJpaRepository.existsByName("apsName")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.save(permissionSet));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_087.getErrorMessage(), ERR_ACC_087.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionIfApsHaveInvalidPrivilegesForSpecifiedBusinessFunctionId() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription")
            .withPermissions(asList(
                new PresentationPermissionSetItem().withFunctionId("id1").withPrivileges(asSet("create")),
                new PresentationPermissionSetItem().withFunctionId("id2").withPrivileges(asSet("invalid")),
                new PresentationPermissionSetItem().withFunctionId("id3").withPrivileges(asSet("execute"))));

        when(assignablePermissionSetJpaRepository.existsByName("apsName")).thenReturn(false);
        when(businessFunctionCache.haveValidPrivileges(eq(permissionSet.getPermissions().get(0).getFunctionId()),
            eq(permissionSet.getPermissions().get(0).getPrivileges()))).thenReturn(true);
        when(businessFunctionCache.haveValidPrivileges(eq(permissionSet.getPermissions().get(1).getFunctionId()),
            eq(permissionSet.getPermissions().get(1).getPrivileges()))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.save(permissionSet));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_088.getErrorMessage(), ERR_ACC_088.getErrorCode()));
    }

    @Test
    public void shouldDeletePermissionSetById() {
        String identifierType = "id";
        String identifier = "1234";
        Long id = 1234L;

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);

        Optional<AssignablePermissionSet> optionalAssignablePermissionSet = Optional.of(assignablePermissionSet);

        when(assignablePermissionSetJpaRepository.findById(eq(id)))
            .thenReturn(optionalAssignablePermissionSet);

        when(serviceAgreementAssignablePermissionSetJpaRepository
            .existsByAssignablePermissionSetId(eq(id))).thenReturn(false);

        Long returnId = permissionSetService.delete(identifierType, identifier);

        assertEquals(id, returnId);

    }

    @Test
    public void shouldDeletePermissionSetByName() {
        String identifierType = "name";
        String identifier = "apsName";
        Long id = 1234L;

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);

        Optional<AssignablePermissionSet> optionalAssignablePermissionSet = Optional.of(assignablePermissionSet);

        when(assignablePermissionSetJpaRepository.findByName(eq(identifier)))
            .thenReturn(optionalAssignablePermissionSet);

        when(serviceAgreementAssignablePermissionSetJpaRepository
            .existsByAssignablePermissionSetId(eq(id))).thenReturn(false);

        Long returnId = permissionSetService.delete(identifierType, identifier);

        assertEquals(id, returnId);

    }


    @Test
    public void shouldThrowErrorDeletePermissionIdNotExists() {

        String identifierType = "id";
        String identifier = "1234";
        Long id = 1234L;

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);

        Optional<AssignablePermissionSet> optionalAssignablePermissionSet = Optional.empty();

        when(assignablePermissionSetJpaRepository.findById(eq(id)))
            .thenReturn(optionalAssignablePermissionSet);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> permissionSetService.delete(identifierType, identifier));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_090.getErrorMessage(), ERR_ACC_090.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorDeletePermissionInvalidType() {

        String identifierType = "id";
        String identifier = "1234";
        Long id = 1234L;

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.ADMIN_USER_DEFAULT);

        Optional<AssignablePermissionSet> optionalAssignablePermissionSet = Optional.of(assignablePermissionSet);

        when(assignablePermissionSetJpaRepository.findById(eq(id)))
            .thenReturn(optionalAssignablePermissionSet);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.delete(identifierType, identifier));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_091.getErrorMessage(), ERR_ACC_091.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorDeletePermissionAssignedInServiceAgreement() {
        String identifierType = "id";
        String identifier = "1234";
        Long id = 1234L;

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setId(1234L);
        assignablePermissionSet.setName("apsName");
        assignablePermissionSet.setDescription("apsDescription");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);

        Optional<AssignablePermissionSet> optionalAssignablePermissionSet = Optional.of(assignablePermissionSet);

        when(assignablePermissionSetJpaRepository.findById(eq(id)))
            .thenReturn(optionalAssignablePermissionSet);

        when(serviceAgreementAssignablePermissionSetJpaRepository
            .existsByAssignablePermissionSetId(eq(id))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.delete(identifierType, identifier));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_092.getErrorMessage(), ERR_ACC_092.getErrorCode()));
    }

    @Test
    public void shouldReturnAssignablePermissionSetsByName() {
        when(assignablePermissionSetJpaRepository.findAllByNameIn(ArgumentMatchers.anySet()))
            .thenReturn(Sets.newHashSet(new AssignablePermissionSet()));

        permissionSetService.getAssignablePermissionSetsByName(Sets.newHashSet("name"), true);
        verify(assignablePermissionSetJpaRepository).findAllByNameIn(eq(Sets.newHashSet("name")));
    }

    @Test
    public void shouldReturnAssignablePermissionSetsById() {
        when(assignablePermissionSetJpaRepository.findAllByIdIn(ArgumentMatchers.anySet()))
            .thenReturn(Sets.newHashSet(new AssignablePermissionSet()));

        permissionSetService.getAssignablePermissionSetsById(Sets.newHashSet(1L), false);
        verify(assignablePermissionSetJpaRepository).findAllByIdIn(eq(Sets.newHashSet(1L)));
    }

    @Test
    public void shouldReturnDefaultRegularUserAssignablePermissionSetsById() {

        when(assignablePermissionSetJpaRepository.findFirstByType(anyInt()))
            .thenReturn(Optional.of(new AssignablePermissionSet()));

        permissionSetService.getAssignablePermissionSetsById(new HashSet<>(), false);
        verify(assignablePermissionSetJpaRepository).findFirstByType(eq(ADMIN_USER_DEFAULT.getValue()));
    }

    @Test
    public void shouldReturnDefaultAdminUserAssignablePermissionSetsById() {

        when(assignablePermissionSetJpaRepository.findFirstByType(anyInt()))
            .thenReturn(Optional.of(new AssignablePermissionSet()));

        permissionSetService.getAssignablePermissionSetsById(new HashSet<>(), true);
        verify(assignablePermissionSetJpaRepository).findFirstByType(eq(REGULAR_USER_DEFAULT.getValue()));
    }

    @Test
    public void shouldThrowErrorWhenNoDefaultAssignablePermissionSetExists() {

        when(assignablePermissionSetJpaRepository.findFirstByType(anyInt())).thenReturn(Optional.empty());

        assertThrows(InternalServerErrorException.class,
            () -> permissionSetService.getAssignablePermissionSetsById(new HashSet<>(), true));
    }

    @Test
    public void shouldThrowBadRequestForNotExistingAssignablePermissionSetByName() {
        when(assignablePermissionSetJpaRepository.findAllByNameIn(ArgumentMatchers.anySet()))
            .thenReturn(new HashSet<>());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.getAssignablePermissionSetsByName(Sets.newHashSet("name"), true));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_094.getErrorMessage(), ERR_ACC_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenValidateIfFgPrivilegesFromApsMatchFgPrivilegesInPendingIfExists() {

        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, BigDecimal.valueOf(7L))));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);
        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser1 = new AssignablePermissionSet();
        apsUser1.setId(14L);
        apsUser1.setName("name1");
        apsUser1.setPermissions(Sets.newHashSet(asList("id5", "id2")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(17L);
        apsUser2.setName("name1");
        apsUser2.setPermissions(Sets.newHashSet(asList("id1", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser1, apsUser2));
        Privilege p = new Privilege();
        p.setCode("code");
        p.setName("pname");
        p.setId("idp");
        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setId("bf1");
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName(p.getName());
        af1.setPrivilege(p);
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setId("bf2");
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName(p.getName());
        af2.setPrivilege(p);
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setId("bf3");
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName(p.getName());
        af3.setPrivilege(p);
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setId("bf4");
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName(p.getName());
        af4.setPrivilege(p);
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setId("bf5");
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName(p.getName());
        af5.setPrivilege(p);
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withType(FunctionGroupType.DEFAULT);

        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);

        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsAdmin2, apsUser1, apsUser2, randomAps);
        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1));
        serviceAgreement.setFunctionGroups(functionGroups);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        when(functionGroupJpaRepository
            .findByServiceAgreementAndType(serviceAgreement, FunctionGroupType.DEFAULT))
            .thenReturn(asList(defaultFG1, defaultFg2));

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setPrivileges(Sets.newHashSet(af2.getId()));
        approvalFunctionGroup.setFunctionGroupId("defaultFgId1");

        List<ApprovalFunctionGroup> fgList = new ArrayList<>();
        fgList.add(approvalFunctionGroup);

        when(approvalFunctionGroupJpaRepository
            .findByServiceAgreementId(any())).thenReturn(Optional.of(fgList));

        when(businessFunctionCache
            .getApplicableFunctionPrivilegeById(any())).thenReturn(af5);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.update(requestData));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_102.getErrorMessage(), ERR_ACC_102.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestForNotExistingAssignablePermissionSetById() {
        when(assignablePermissionSetJpaRepository.findAllByIdIn(ArgumentMatchers.anySet()))
            .thenReturn(new HashSet<>());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.getAssignablePermissionSetsById(Sets.newHashSet(1L), false));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_094.getErrorMessage(), ERR_ACC_094.getErrorCode()));
    }

    @Test
    public void shouldUpdatePermissionSet() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, BigDecimal.valueOf(7L))));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);

        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser1 = new AssignablePermissionSet();
        apsUser1.setId(14L);
        apsUser1.setName("name1");
        apsUser1.setPermissions(Sets.newHashSet(asList("id5", "id2")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(17L);
        apsUser2.setName("name3");
        apsUser2.setPermissions(Sets.newHashSet(asList("id3", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser1, apsUser2));
        Privilege p = new Privilege();
        p.setCode("code");
        p.setName("pname");
        p.setId("idp");
        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName(p.getName());
        af1.setPrivilege(p);
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName(p.getName());
        af2.setPrivilege(p);
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName(p.getName());
        af3.setPrivilege(p);
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName(p.getName());
        af4.setPrivilege(p);
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName(p.getName());
        af5.setPrivilege(p);
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");
        GroupedFunctionPrivilege gfItem1 = new GroupedFunctionPrivilege();
        gfItem1.setApplicableFunctionPrivilegeId(af1.getId());
        GroupedFunctionPrivilege gfItem2 = new GroupedFunctionPrivilege();
        gfItem2.setApplicableFunctionPrivilegeId(af2.getId());
        GroupedFunctionPrivilege gfItem3 = new GroupedFunctionPrivilege();
        gfItem3.setApplicableFunctionPrivilegeId(af3.getId());
        GroupedFunctionPrivilege gfItem4 = new GroupedFunctionPrivilege();
        gfItem4.setApplicableFunctionPrivilegeId(af4.getId());
        GroupedFunctionPrivilege gfItem5 = new GroupedFunctionPrivilege();
        gfItem5.setApplicableFunctionPrivilegeId(af5.getId());
        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(asList(gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(asList(gfItem1, gfItem4, gfItem5));

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withPermissions(gfpSet1)
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withPermissions(gfpSet2)
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withPermissions(gfpSet3)
            .withType(FunctionGroupType.DEFAULT);

        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        serviceAgreement.setFunctionGroups(functionGroups);
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);
        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsAdmin2, apsUser1, apsUser2, randomAps);

        PermissionSetsInServiceAgreements userAssignablePermissionSet17 = new PermissionSetsInServiceAgreements();
        userAssignablePermissionSet17.setServiceAgreementId(serviceAgreement.getId());
        userAssignablePermissionSet17
            .setAssignedPermissionUserType(AssignablePermissionType.CUSTOM.getValue());
        userAssignablePermissionSet17.setAssignablePermissionSetId(17L);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        when(serviceAgreementJpaRepository.save(eq(serviceAgreement)))
            .thenReturn(serviceAgreement);

        permissionSetService.update(requestData);

        verify(serviceAgreementJpaRepository, times(1))
            .findByExternalId(requestData.getExternalServiceAgreementId(),
                SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS);
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidNameIdentifierProvidedOnUpdatePermissionSet() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, BigDecimal.valueOf(17))));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);

        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsInvalid = new AssignablePermissionSet();
        apsInvalid.setName("INVALID");
        apsInvalid.setId(7L);
        apsInvalid.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(17L);
        apsUser2.setName("name3");
        apsUser2.setPermissions(Sets.newHashSet(asList("id3", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(singletonList(apsAdmin1)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser2));

        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName("p1");
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName("p2");
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName("p3");
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName("p4");
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName("p5");
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");
        GroupedFunctionPrivilege gfItem1 = new GroupedFunctionPrivilege();
        gfItem1.setApplicableFunctionPrivilegeId(af1.getId());
        GroupedFunctionPrivilege gfItem2 = new GroupedFunctionPrivilege();
        gfItem2.setApplicableFunctionPrivilegeId(af2.getId());
        GroupedFunctionPrivilege gfItem3 = new GroupedFunctionPrivilege();
        gfItem3.setApplicableFunctionPrivilegeId(af3.getId());
        GroupedFunctionPrivilege gfItem4 = new GroupedFunctionPrivilege();
        gfItem4.setApplicableFunctionPrivilegeId(af4.getId());
        GroupedFunctionPrivilege gfItem5 = new GroupedFunctionPrivilege();
        gfItem5.setApplicableFunctionPrivilegeId(af5.getId());
        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(asList(gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(asList(gfItem1, gfItem4, gfItem5));

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withPermissions(gfpSet1)
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withPermissions(gfpSet2)
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withPermissions(gfpSet3)
            .withType(FunctionGroupType.DEFAULT);

        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        serviceAgreement.setFunctionGroups(functionGroups);
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);
        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsUser2, randomAps);

        PermissionSetsInServiceAgreements userAssignablePermissionSet17 = new PermissionSetsInServiceAgreements();
        userAssignablePermissionSet17.setServiceAgreementId(serviceAgreement.getId());
        userAssignablePermissionSet17
            .setAssignedPermissionUserType(AssignablePermissionType.CUSTOM.getValue());
        userAssignablePermissionSet17.setAssignablePermissionSetId(17L);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.update(requestData));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidIdIdentifierProvidedOnUpdatePermissionSet() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, null)));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);

        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsInvalid = new AssignablePermissionSet();
        apsInvalid.setName("INVALID");
        apsInvalid.setId(7L);
        apsInvalid.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser1 = new AssignablePermissionSet();
        apsUser1.setId(14L);
        apsUser1.setName("name1");
        apsUser1.setPermissions(Sets.newHashSet(asList("id5", "id2")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(7L);
        apsUser2.setName("name3");
        apsUser2.setPermissions(Sets.newHashSet(asList("id3", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(singletonList(apsAdmin1)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser1, apsUser2));

        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName("p1");
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName("p2");
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName("p3");
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName("p4");
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName("p5");
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");
        GroupedFunctionPrivilege gfItem1 = new GroupedFunctionPrivilege();
        gfItem1.setApplicableFunctionPrivilegeId(af1.getId());
        GroupedFunctionPrivilege gfItem2 = new GroupedFunctionPrivilege();
        gfItem2.setApplicableFunctionPrivilegeId(af2.getId());
        GroupedFunctionPrivilege gfItem3 = new GroupedFunctionPrivilege();
        gfItem3.setApplicableFunctionPrivilegeId(af3.getId());
        GroupedFunctionPrivilege gfItem4 = new GroupedFunctionPrivilege();
        gfItem4.setApplicableFunctionPrivilegeId(af4.getId());
        GroupedFunctionPrivilege gfItem5 = new GroupedFunctionPrivilege();
        gfItem5.setApplicableFunctionPrivilegeId(af5.getId());
        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(asList(gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(asList(gfItem1, gfItem4, gfItem5));

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withPermissions(gfpSet1)
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withPermissions(gfpSet2)
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withPermissions(gfpSet3)
            .withType(FunctionGroupType.DEFAULT);

        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        serviceAgreement.setFunctionGroups(functionGroups);
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);
        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsUser1, apsUser2, randomAps);

        PermissionSetsInServiceAgreements userAssignablePermissionSet17 = new PermissionSetsInServiceAgreements();
        userAssignablePermissionSet17.setServiceAgreementId(serviceAgreement.getId());
        userAssignablePermissionSet17
            .setAssignedPermissionUserType(AssignablePermissionType.CUSTOM.getValue());
        userAssignablePermissionSet17.setAssignablePermissionSetId(17L);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionSetService.update(requestData));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode()));
    }

    @Test
    public void shouldUpdateRegularApsAndNotAdminApsWhenAdminApsNotProvided() {
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withRegularUserAps(regularUserAps);

        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser1 = new AssignablePermissionSet();
        apsUser1.setId(14L);
        apsUser1.setName("name1");
        apsUser1.setPermissions(Sets.newHashSet(asList("id5", "id2")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(17L);
        apsUser2.setName("name3");
        apsUser2.setPermissions(Sets.newHashSet(asList("id3", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser1, apsUser2));
        Privilege p = new Privilege();
        p.setCode("code");
        p.setName("pname");
        p.setId("idp");
        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName(p.getName());
        af1.setPrivilege(p);
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName(p.getName());
        af2.setPrivilege(p);
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName(p.getName());
        af3.setPrivilege(p);
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName(p.getName());
        af4.setPrivilege(p);
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName(p.getName());
        af5.setPrivilege(p);
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");
        GroupedFunctionPrivilege gfItem1 = new GroupedFunctionPrivilege();
        gfItem1.setApplicableFunctionPrivilegeId(af1.getId());
        GroupedFunctionPrivilege gfItem2 = new GroupedFunctionPrivilege();
        gfItem2.setApplicableFunctionPrivilegeId(af2.getId());
        GroupedFunctionPrivilege gfItem3 = new GroupedFunctionPrivilege();
        gfItem3.setApplicableFunctionPrivilegeId(af3.getId());
        GroupedFunctionPrivilege gfItem4 = new GroupedFunctionPrivilege();
        gfItem4.setApplicableFunctionPrivilegeId(af4.getId());
        GroupedFunctionPrivilege gfItem5 = new GroupedFunctionPrivilege();
        gfItem5.setApplicableFunctionPrivilegeId(af5.getId());
        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(asList(gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(asList(gfItem1, gfItem4, gfItem5));

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withPermissions(gfpSet1)
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withPermissions(gfpSet2)
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withPermissions(gfpSet3)
            .withType(FunctionGroupType.DEFAULT);

        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        serviceAgreement.setFunctionGroups(functionGroups);
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);
        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsAdmin2, apsUser1, apsUser2, randomAps);

        PermissionSetsInServiceAgreements userAssignablePermissionSet17 = new PermissionSetsInServiceAgreements();
        userAssignablePermissionSet17.setServiceAgreementId(serviceAgreement.getId());
        userAssignablePermissionSet17
            .setAssignedPermissionUserType(AssignablePermissionType.CUSTOM.getValue());
        userAssignablePermissionSet17.setAssignablePermissionSetId(17L);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        when(serviceAgreementJpaRepository.save(eq(serviceAgreement)))
            .thenReturn(serviceAgreement);

        permissionSetService.update(requestData);

        ArgumentCaptor<ServiceAgreement> serviceAgreementArgumentCaptor = ArgumentCaptor
            .forClass(ServiceAgreement.class);
        verify(serviceAgreementJpaRepository, times(1))
            .findByExternalId(requestData.getExternalServiceAgreementId(),
                SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS);
        verify(serviceAgreementJpaRepository).save(eq(serviceAgreement));
        verify(serviceAgreementJpaRepository).save(serviceAgreementArgumentCaptor.capture());
        Set<AssignablePermissionSet> permissionSetsRegular = serviceAgreementArgumentCaptor.getValue()
            .getPermissionSetsRegular();
        assertThat(permissionSetsRegular, hasSize(1));
        assertThat(permissionSetsRegular, contains(
            allOf(
                hasProperty("id", is(apsUser1.getId())),
                hasProperty("name", is(apsUser1.getName())),
                hasProperty("description", is(apsUser1.getDescription())),
                hasProperty("type", is(apsUser1.getType()))
            )
        ));

        Set<AssignablePermissionSet> permissionSetsAdmin = serviceAgreementArgumentCaptor.getValue()
            .getPermissionSetsAdmin();
        assertThat(permissionSetsAdmin, hasSize(2));
        assertThat(permissionSetsAdmin, contains(
            allOf(
                hasProperty("id", is(apsAdmin1.getId())),
                hasProperty("name", is(apsAdmin1.getName())),
                hasProperty("description", is(apsAdmin1.getDescription())),
                hasProperty("type", is(apsAdmin1.getType()))
            ),
            allOf(
                hasProperty("id", is(apsAdmin2.getId())),
                hasProperty("name", is(apsAdmin2.getName())),
                hasProperty("description", is(apsAdmin2.getDescription())),
                hasProperty("type", is(apsAdmin2.getType()))
            )
        ));
    }

    @Test
    public void shouldUpdateAdminApsAndNotRegularApsWhenRegularApsNotProvided() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(singletonList(BigDecimal.valueOf(7L))));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps);

        LegalEntity creatorLegalEntity = new LegalEntity();

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("id1", "id2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("id3", "id3")));
        AssignablePermissionSet apsUser1 = new AssignablePermissionSet();
        apsUser1.setId(14L);
        apsUser1.setName("name1");
        apsUser1.setPermissions(Sets.newHashSet(asList("id5", "id2")));
        AssignablePermissionSet apsUser2 = new AssignablePermissionSet();
        apsUser2.setId(17L);
        apsUser2.setName("name3");
        apsUser2.setPermissions(Sets.newHashSet(asList("id3", "id4")));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("Service Agreement")
            .withDescription("Sa desc")
            .withExternalId(requestData.getExternalServiceAgreementId())
            .withCreatorLegalEntity(creatorLegalEntity)
            .withPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)))
            .withPermissionSetsRegular(Sets.newHashSet(apsUser1, apsUser2));
        Privilege p = new Privilege();
        p.setCode("code");
        p.setName("pname");
        p.setId("idp");
        ApplicableFunctionPrivilege af1 = new ApplicableFunctionPrivilege();
        BusinessFunction bf1 = new BusinessFunction();
        bf1.setFunctionCode("fc1");
        bf1.setFunctionName("fn1");
        bf1.setResourceCode("rc1");
        bf1.setResourceName("rn1");
        af1.setId("id1");
        af1.setPrivilegeName(p.getName());
        af1.setPrivilege(p);
        af1.setBusinessFunctionName("bf1");
        af1.setBusinessFunctionResourceName("r1");
        af1.setBusinessFunction(bf1);
        ApplicableFunctionPrivilege af2 = new ApplicableFunctionPrivilege();
        BusinessFunction bf2 = new BusinessFunction();
        bf2.setFunctionCode("fc2");
        bf2.setFunctionName("fn2");
        bf2.setResourceCode("rc2");
        bf2.setResourceName("rn2");
        af2.setId("id2");
        af2.setPrivilegeName(p.getName());
        af2.setPrivilege(p);
        af2.setBusinessFunction(bf2);
        af2.setBusinessFunctionResourceName("r2");
        af2.setBusinessFunctionName("bf2");
        ApplicableFunctionPrivilege af3 = new ApplicableFunctionPrivilege();
        BusinessFunction bf3 = new BusinessFunction();
        bf3.setFunctionCode("fc3");
        bf3.setFunctionName("fn3");
        bf3.setResourceCode("rc3");
        bf3.setResourceName("rn3");
        af3.setId("id3");
        af3.setPrivilegeName(p.getName());
        af3.setPrivilege(p);
        af3.setBusinessFunction(bf3);
        af3.setBusinessFunctionResourceName("b3");
        af3.setBusinessFunctionName("bf3");
        ApplicableFunctionPrivilege af4 = new ApplicableFunctionPrivilege();
        BusinessFunction bf4 = new BusinessFunction();
        bf4.setFunctionCode("fc4");
        bf4.setFunctionName("fn4");
        bf4.setResourceCode("rc4");
        bf4.setResourceName("rn4");
        af4.setId("id4");
        af4.setBusinessFunction(bf4);
        af4.setBusinessFunctionResourceName("f4");
        af4.setBusinessFunctionName("b4");
        af4.setPrivilegeName(p.getName());
        af4.setPrivilege(p);
        ApplicableFunctionPrivilege af5 = new ApplicableFunctionPrivilege();
        BusinessFunction bf5 = new BusinessFunction();
        bf5.setFunctionCode("fc5");
        bf5.setFunctionName("fn5");
        bf5.setResourceCode("rc5");
        bf5.setResourceName("rn5");
        af5.setId("id5");
        af5.setPrivilegeName(p.getName());
        af5.setPrivilege(p);
        af5.setBusinessFunction(bf5);
        af5.setBusinessFunctionResourceName("bn5");
        af5.setBusinessFunctionName("bf5");
        GroupedFunctionPrivilege gfItem1 = new GroupedFunctionPrivilege();
        gfItem1.setApplicableFunctionPrivilegeId(af1.getId());
        GroupedFunctionPrivilege gfItem2 = new GroupedFunctionPrivilege();
        gfItem2.setApplicableFunctionPrivilegeId(af2.getId());
        GroupedFunctionPrivilege gfItem3 = new GroupedFunctionPrivilege();
        gfItem3.setApplicableFunctionPrivilegeId(af3.getId());
        GroupedFunctionPrivilege gfItem4 = new GroupedFunctionPrivilege();
        gfItem4.setApplicableFunctionPrivilegeId(af4.getId());
        GroupedFunctionPrivilege gfItem5 = new GroupedFunctionPrivilege();
        gfItem5.setApplicableFunctionPrivilegeId(af5.getId());
        Set<GroupedFunctionPrivilege> gfpSet1 = Sets.newHashSet(asList(gfItem1, gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet2 = Sets.newHashSet(asList(gfItem2, gfItem5));
        Set<GroupedFunctionPrivilege> gfpSet3 = Sets.newHashSet(asList(gfItem1, gfItem4, gfItem5));

        FunctionGroup systemFG = new FunctionGroup()
            .withId("systemFgId")
            .withDescription("desc system fg")
            .withName("system fg name")
            .withPermissions(gfpSet1)
            .withType(FunctionGroupType.SYSTEM);
        FunctionGroup defaultFG1 = new FunctionGroup()
            .withId("defaultFgId1")
            .withDescription("desc default fg1")
            .withName("default fg1 name")
            .withPermissions(gfpSet2)
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup defaultFg2 = new FunctionGroup()
            .withId("defaultFgId2")
            .withDescription("desc default fg2")
            .withName("default fg2 name")
            .withPermissions(gfpSet3)
            .withType(FunctionGroupType.DEFAULT);

        Set<FunctionGroup> functionGroups = Sets.newHashSet(asList(systemFG, defaultFG1, defaultFg2));
        serviceAgreement.setFunctionGroups(functionGroups);
        AssignablePermissionSet randomAps = new AssignablePermissionSet();
        randomAps.setName("name12");
        randomAps.setPermissions(Sets.newHashSet(singletonList("p12")));
        randomAps.setId(12L);
        List<AssignablePermissionSet> allAsp = List.of(apsAdmin1, apsAdmin2, apsUser1, apsUser2, randomAps);

        PermissionSetsInServiceAgreements userAssignablePermissionSet17 = new PermissionSetsInServiceAgreements();
        userAssignablePermissionSet17.setServiceAgreementId(serviceAgreement.getId());
        userAssignablePermissionSet17
            .setAssignedPermissionUserType(AssignablePermissionType.CUSTOM.getValue());
        userAssignablePermissionSet17.setAssignablePermissionSetId(17L);

        mockServiceAgreement(requestData, serviceAgreement);
        mockGetAllAPSs(allAsp);
        mockFunctionGroups(serviceAgreement, systemFG);

        when(serviceAgreementJpaRepository.save(eq(serviceAgreement)))
            .thenReturn(serviceAgreement);
        
        permissionSetService.update(requestData);

        ArgumentCaptor<ServiceAgreement> serviceAgreementArgumentCaptor = ArgumentCaptor
            .forClass(ServiceAgreement.class);

        verify(serviceAgreementJpaRepository, times(1))
            .findByExternalId(requestData.getExternalServiceAgreementId(),
                SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS);
        verify(serviceAgreementJpaRepository).save(serviceAgreementArgumentCaptor.capture());
        Set<AssignablePermissionSet> permissionSetsAdmin = serviceAgreementArgumentCaptor.getValue()
            .getPermissionSetsAdmin();
        assertThat(permissionSetsAdmin, hasSize(1));
        assertThat(permissionSetsAdmin, contains(
            allOf(
                hasProperty("id", is(apsAdmin2.getId())),
                hasProperty("name", is(apsAdmin2.getName())),
                hasProperty("description", is(apsAdmin2.getDescription())),
                hasProperty("type", is(apsAdmin2.getType()))
            )
        ));

        Set<AssignablePermissionSet> permissionSetsRegular = serviceAgreementArgumentCaptor.getValue()
            .getPermissionSetsRegular();
        assertThat(permissionSetsRegular, hasSize(2));
        assertThat(permissionSetsRegular, contains(
            allOf(
                hasProperty("id", is(apsUser1.getId())),
                hasProperty("name", is(apsUser1.getName())),
                hasProperty("description", is(apsUser1.getDescription())),
                hasProperty("type", is(apsUser1.getType()))
            ),
            allOf(
                hasProperty("id", is(apsUser2.getId())),
                hasProperty("name", is(apsUser2.getName())),
                hasProperty("description", is(apsUser2.getDescription())),
                hasProperty("type", is(apsUser2.getType()))
            )
        ));
    }

    private void mockFunctionGroups(ServiceAgreement serviceAgreement, FunctionGroup systemFG) {
        when(functionGroupJpaRepository
            .findByServiceAgreementAndType(serviceAgreement, FunctionGroupType.SYSTEM))
            .thenReturn(singletonList(systemFG));
    }

    private void mockGetAllAPSs(List<AssignablePermissionSet> allAsp) {
        when(assignablePermissionSetJpaRepository.findAll()).thenReturn(allAsp);
    }

    private void mockServiceAgreement(PresentationPermissionSetItemPut requestData, ServiceAgreement serviceAgreement) {
        when(serviceAgreementJpaRepository.findByExternalId(eq(requestData.getExternalServiceAgreementId()),
            eq(SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)))
            .thenReturn(Optional.of(serviceAgreement));
    }
}
