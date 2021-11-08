package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_WITH_SA_CREATOR;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_013;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_015;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_031;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_050;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_051;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_053;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_074;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_079;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_081;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_082;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_083;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_085;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_097;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_099;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_060;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.mappers.DataGroupDomainMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.verification.Times;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupServiceImplTest {

    @Mock
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Spy
    private DataGroupDomainMapper mapper = Mappers.getMapper(DataGroupDomainMapper.class);
    @Mock
    private DataGroupItemJpaRepository dataGroupItemJpaRepository;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Mock
    private UserAssignedCombinationRepository userAssignedFunctionGroupDataGroupRepository;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private ApprovalUserContextAssignFunctionGroupJpaRepository
        approvalUserContextAssignFunctionGroupJpaRepository;
    @Mock
    private ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    @Mock
    private ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    @Mock
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private DataGroupServiceImpl dataGroupService;

    @Test
    public void testGetById() {
        String id = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_EXTENDED))).thenReturn(Optional.of(dataGroup));
        DataGroup returnedDataGroup = dataGroupService.getByIdWithExtendedData(id);
        assertEquals(dataGroup, returnedDataGroup);
    }

    @Test
    public void testGetByIdWithoutItems() {
        String id = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup();
        when(dataGroupJpaRepository.findById(eq(id))).thenReturn(Optional.of(dataGroup));
        DataGroup returnedDataGroup = dataGroupService.getById(id);
        assertEquals(dataGroup, returnedDataGroup);
    }

    @Test
    public void shouldTestIfExistByServiceAgreementIdReturningTrue() {
        String serviceAgreementId = "saId";
        when(approvalDataGroupDetailsJpaRepository.existsByServiceAgreementId(eq(serviceAgreementId)))
            .thenReturn(true);
        assertTrue(dataGroupService.checkIfExistsPendingDataGroupByServiceAgreementId(serviceAgreementId, true));
    }

    @Test
    public void shouldTestIfExistByServiceAgreementIdReturningFalse() {
        String serviceAgreementId = "saId";
        when(approvalDataGroupDetailsJpaRepository.existsByServiceAgreementId(eq(serviceAgreementId)))
            .thenReturn(false);
        assertFalse(dataGroupService.checkIfExistsPendingDataGroupByServiceAgreementId(serviceAgreementId, true));
    }

    @Test
    public void shouldTestIfExistByServiceAgreementIdReturningFalseApprovalOff() {
        String serviceAgreementId = "saId";
        assertFalse(dataGroupService.checkIfExistsPendingDataGroupByServiceAgreementId(serviceAgreementId, false));
    }

    @Test
    public void testGetByIdThrowNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_EXTENDED))).thenReturn(empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.getByIdWithExtendedData(id));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldSaveDataGroupApproval() {

        DataGroupBase dataGroup = new DataGroupBase();
        dataGroup.setType("type");
        dataGroup.setServiceAgreementId("saId");
        dataGroup.setDescription("desc");
        dataGroup.setName("name");
        dataGroup.setItems(asList("1", "2"));

        ApprovalDataGroupDetails dataGroupSave = getApprovalDataGroupDetails("saId");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(serviceAgreementJpaRepository.existsById(eq(dataGroup.getServiceAgreementId()))).thenReturn(true);
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementId(eq(dataGroup.getName()), eq(dataGroup.getServiceAgreementId())))
            .thenReturn(new ArrayList<>());
        when(approvalDataGroupDetailsJpaRepository
            .existsByNameAndServiceAgreementId(dataGroup.getName(), dataGroup.getServiceAgreementId()))
            .thenReturn(false);
        when(approvalDataGroupDetailsJpaRepository.save(any(ApprovalDataGroupDetails.class))).thenReturn(dataGroupSave);
        String approvalId = dataGroupService.saveDataGroupApproval(dataGroup, "approvalId");
        assertEquals(dataGroupSave.getApprovalId(), approvalId);
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("saId"));
    }

    @Test
    public void shouldSaveDataGroupApprovalWithoutDataGroupItems() {

        DataGroupBase dataGroup = new DataGroupBase();
        dataGroup.setType("type");
        dataGroup.setServiceAgreementId("saId");
        dataGroup.setDescription("desc");
        dataGroup.setName("name");
        dataGroup.setItems(emptyList());

        ApprovalDataGroupDetails dataGroupSave = getApprovalDataGroupDetails("saId");
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(serviceAgreementJpaRepository.existsById(eq(dataGroup.getServiceAgreementId()))).thenReturn(true);
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementId(eq(dataGroup.getName()), eq(dataGroup.getServiceAgreementId())))
            .thenReturn(new ArrayList<>());
        when(approvalDataGroupDetailsJpaRepository
            .existsByNameAndServiceAgreementId(dataGroup.getName(), dataGroup.getServiceAgreementId()))
            .thenReturn(false);
        when(approvalDataGroupDetailsJpaRepository.save(any(ApprovalDataGroupDetails.class))).thenReturn(dataGroupSave);
        String approvalId = dataGroupService.saveDataGroupApproval(dataGroup, "approvalId");
        assertEquals(dataGroupSave.getApprovalId(), approvalId);
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("saId"));
    }

    @Test
    public void shouldThrowBadRequestWhenDataGroupApprovalAlreadyPending() {
        DataGroupBase dataGroup = new DataGroupBase();
        dataGroup.setType("type");
        dataGroup.setServiceAgreementId("saId");
        dataGroup.setDescription("desc");
        dataGroup.setName("name");
        dataGroup.setItems(asList("1", "2"));

        when(serviceAgreementJpaRepository.existsById(eq(dataGroup.getServiceAgreementId())))
            .thenReturn(true);
        when(approvalDataGroupDetailsJpaRepository
            .existsByNameAndServiceAgreementId(dataGroup.getName(), dataGroup.getServiceAgreementId()))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.saveDataGroupApproval(dataGroup, "approvalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_082.getErrorMessage(), ERR_ACC_082.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeDataGroupIsAssignedToCustomSA() {
        DataGroupBase dataGroup = new DataGroupBase();
        dataGroup.setType("CUSTOMERS");
        dataGroup.setServiceAgreementId("saId");
        dataGroup.setDescription("desc");
        dataGroup.setName("name");
        dataGroup.setItems(asList("1", "2"));

        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementId(anyString(), anyString()))
            .thenReturn(new ArrayList<>());
        when(serviceAgreementJpaRepository.findById(anyString(), anyString()))
            .thenReturn(Optional.of(new ServiceAgreement().withMaster(false)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.save(dataGroup));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_097.getErrorMessage(), ERR_ACC_097.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeDataGroupWhenItemsAreNotInHierarchy() {
        DataGroupBase dataGroup = new DataGroupBase();
        dataGroup.setType("CUSTOMERS");
        dataGroup.setServiceAgreementId("saId");
        dataGroup.setDescription("desc");
        dataGroup.setName("name");
        dataGroup.setItems(asList("1", "2"));

        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementId(anyString(), anyString()))
            .thenReturn(new ArrayList<>());
        when(serviceAgreementJpaRepository.findById(anyString(), anyString()))
            .thenReturn(Optional.of(new ServiceAgreement()
                .withMaster(true)
                .withCreatorLegalEntity(new LegalEntity().withId("leid"))));
        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(anyString(), anyCollection()))
            .thenReturn(singletonList(() -> "1"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.save(dataGroup));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_099.getErrorMessage(), ERR_ACC_099.getErrorCode()));
    }

    @Test
    public void shouldUpdateDataGroupApproval() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_SERVICE_AGREEMENT)))
            .thenReturn(Optional.of(new DataGroup().withId(id).withServiceAgreementId(serviceAgreementId)
                .withServiceAgreement(new ServiceAgreement().withId(serviceAgreementId))));
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(emptyList());
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withApprovalId("approvalId")
            .withName("dg-name");
        dataGroupService.updateDataGroupApproval(body);

        ArgumentCaptor<ApprovalDataGroupDetails> captor = ArgumentCaptor.forClass(ApprovalDataGroupDetails.class);

        verify(approvalDataGroupDetailsJpaRepository).save(captor.capture());

        ApprovalDataGroupDetails value = captor.getValue();
        assertEquals(id, value.getDataGroupId());
        assertEquals(serviceAgreementId, value.getServiceAgreementId());
        assertEquals("approvalId", value.getApprovalId());
        assertEquals("dg-name", value.getName());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    public void shouldUpdateDataGroupApprovalWithoutDataGroupItems() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        Set<String> dataItemIds = Sets.newHashSet();
        dataItemIds.add("item1");
        DataGroup dataGroupForUpdate = new DataGroup()
            .withId(id)
            .withDataItemIds(dataItemIds)
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreement(new ServiceAgreement().withId(serviceAgreementId));
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_SERVICE_AGREEMENT)))
            .thenReturn(Optional.of(dataGroupForUpdate));
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(emptyList());
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withItems(Collections.singletonList("item1"))
            .withApprovalId("approvalId")
            .withName("dg-name");
        dataGroupService.updateDataGroupApproval(body);

        ArgumentCaptor<ApprovalDataGroupDetails> captor = ArgumentCaptor.forClass(ApprovalDataGroupDetails.class);

        verify(approvalDataGroupDetailsJpaRepository).save(captor.capture());

        ApprovalDataGroupDetails value = captor.getValue();
        assertEquals(id, value.getDataGroupId());
        assertEquals(serviceAgreementId, value.getServiceAgreementId());
        assertEquals("approvalId", value.getApprovalId());
        assertEquals("dg-name", value.getName());
        assertEquals("dg-name", value.getName());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreementId));
    }

    @Test
    public void shouldThrowErrorIfDataGroupByIdDoesNotExist() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();

        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withApprovalId("approvalId")
            .withName("dg-name");

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.updateDataGroupApproval(body));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorIfServiceAgreementIsChanged() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_SERVICE_AGREEMENT)))
            .thenReturn(Optional.of(new DataGroup().withId(id)
                .withServiceAgreement(new ServiceAgreement().withId("new-id"))));

        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withApprovalId("approvalId")
            .withName("dg-name");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupApproval(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_031.getErrorMessage(), ERR_ACC_031.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorIfNameIsNotUnique() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_SERVICE_AGREEMENT)))
            .thenReturn(Optional.of(new DataGroup().withId(id)
                .withServiceAgreement(new ServiceAgreement().withId(serviceAgreementId))));
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(singletonList(new DataGroup()));

        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withApprovalId("approvalId")
            .withName("dg-name");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupApproval(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorIfThereIsAlreadyPendingRequest() {
        String id = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        when(dataGroupJpaRepository.findById(eq(id), eq(DATA_GROUP_SERVICE_AGREEMENT)))
            .thenReturn(Optional.of(new DataGroup().withId(id)
                .withServiceAgreement(new ServiceAgreement().withId(serviceAgreementId))));
        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(emptyList());
        when(approvalDataGroupJpaRepository.existsByDataGroupId(eq(id))).thenReturn(true);

        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withId(id)
            .withApprovalId("approvalId")
            .withName("dg-name");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupApproval(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode()));
    }

    @Test
    public void testGetDataGroupsByExternalServiceAgreementIds() {
        LegalEntity legalEntity = createLegalEntity("le1", "le", null);
        ServiceAgreement serviceAgreement1 = createServiceAgreement("sa1", "sa1", "desc", legalEntity, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("sa2", "sa2", "desc", legalEntity, null, null);
        DataGroup dataGroup1 = createDataGroup("dataGroup5", "ARRANGEMENTS", "arrangment5", serviceAgreement1);
        DataGroup dataGroup2 = createDataGroup("dataGroup6", "CONTACTS", "arrangment6", serviceAgreement2);

        Set<String> ids = Sets.newHashSet("sa1", "sa2");

        List<DataGroup> dataGroups = Lists.newArrayList(dataGroup1, dataGroup2);

        when(dataGroupJpaRepository
            .findAllDataGroupsWithExternalServiceAgreementIdsIn(ids, DATA_GROUP_EXTENDED))
            .thenReturn(dataGroups);

        List<PersistenceDataGroupExtendedItemDto> returnedDataGroups = dataGroupService
            .getDataGroupsByExternalServiceAgreementIds(ids, false);
        assertEquals(dataGroups.get(0).getServiceAgreement().getExternalId(),
            returnedDataGroups.get(0).getExternalServiceAgreementId());
        assertEquals(dataGroups.get(1).getServiceAgreement().getExternalId(),
            returnedDataGroups.get(1).getExternalServiceAgreementId());
        verify(serviceAgreementJpaRepository, new Times(0))
            .findAllByExternalIdIn(any(Collection.class));
        verify(approvalDataGroupDetailsJpaRepository, new Times(0))
            .findAllByServiceAgreementIdIn(any(Collection.class));
    }

    @Test
    public void testGetPendingDataGroupsByExternalServiceAgreementIds() {
        LegalEntity legalEntity = createLegalEntity("le1", "le", null);
        ServiceAgreement serviceAgreement1 = createServiceAgreement("sa1", "ex_sa1", "desc", legalEntity, null, null);
        serviceAgreement1.setId("sa1");
        ServiceAgreement serviceAgreement2 = createServiceAgreement("sa2", "ex_sa2", "desc", legalEntity, null, null);
        serviceAgreement2.setId("sa2");
        DataGroup dataGroup1 = createDataGroup("dataGroup5", "ARRANGEMENTS", "arrangment5", serviceAgreement1);
        DataGroup dataGroup2 = createDataGroup("dataGroup6", "CONTACTS", "arrangment6", serviceAgreement2);

        ApprovalDataGroupDetails approvalDetails1 = getApprovalDataGroupDetails("sa1");
        ApprovalDataGroupDetails approvalDetails2 = getApprovalDataGroupDetails("sa2");

        Set<String> ids = Sets.newHashSet("sa1", "sa2");

        List<ServiceAgreement> listServiceAgreements = Lists.newArrayList(
            serviceAgreement1, serviceAgreement2);

        when(serviceAgreementJpaRepository
            .findAllByExternalIdIn(ids))
            .thenReturn(listServiceAgreements);

        List<DataGroup> dataGroups = Lists.newArrayList(
            dataGroup1, dataGroup2);

        when(dataGroupJpaRepository
            .findAllDataGroupsWithExternalServiceAgreementIdsIn(ids, DATA_GROUP_EXTENDED))
            .thenReturn(dataGroups);

        List<ApprovalDataGroupDetails> approvalDataGroupDetails = Lists.newArrayList(
            approvalDetails1, approvalDetails2);

        Set<String> keys = Sets.newHashSet(
            serviceAgreement1.getId(), serviceAgreement2.getId());

        when(approvalDataGroupDetailsJpaRepository
            .findAllByServiceAgreementIdIn(keys))
            .thenReturn(approvalDataGroupDetails);

        List<PersistenceDataGroupExtendedItemDto> returnedDataGroups = dataGroupService
            .getDataGroupsByExternalServiceAgreementIds(ids, true);

        assertEquals(dataGroups.get(0).getServiceAgreement().getExternalId(),
            returnedDataGroups.get(0).getExternalServiceAgreementId());
        assertEquals(dataGroups.get(1).getServiceAgreement().getExternalId(),
            returnedDataGroups.get(1).getExternalServiceAgreementId());
        assertEquals(listServiceAgreements.get(0).getExternalId(),
            returnedDataGroups.get(2).getExternalServiceAgreementId());
        assertEquals(listServiceAgreements.get(1).getExternalId(),
            returnedDataGroups.get(3).getExternalServiceAgreementId());
        verify(serviceAgreementJpaRepository)
            .findAllByExternalIdIn(any(Collection.class));
        verify(approvalDataGroupDetailsJpaRepository)
            .findAllByServiceAgreementIdIn(any(Collection.class));
    }

    private ApprovalDataGroupDetails getApprovalDataGroupDetails(String sa1) {
        ApprovalDataGroupDetails approvalDetails1 = new ApprovalDataGroupDetails();
        approvalDetails1.setId(1L);
        approvalDetails1.setApprovalId("approvalId");
        approvalDetails1.setType("type");
        approvalDetails1.setServiceAgreementId(sa1);
        approvalDetails1.setItems(Sets.newHashSet(asList("1", "2")));
        approvalDetails1.setName("name");
        approvalDetails1.setDescription("desc");
        return approvalDetails1;
    }

    @Test
    public void testGetBulkDataGroupsByIds() {

        DataGroup dataGroup1 = createDataGroup("dataGroup5", "ARRANGEMENTS", "arrangment5", null);
        DataGroup dataGroup2 = createDataGroup("dataGroup6", "CONTACTS", "arrangment6", null);

        String id1String = "123e4567-e89b-12d3-a456-111111111111";
        String id2String = "123e4567-e89b-12d3-a456-222222222222";

        Set<String> ids = new HashSet<>();
        ids.add(id1String);
        ids.add(id2String);

        List<DataGroup> dataGroups = new ArrayList<>();
        dataGroups.add(dataGroup1);
        dataGroup1.setId(id1String);
        dataGroups.add(dataGroup2);
        dataGroup2.setId(id2String);

        when(dataGroupJpaRepository.findAllDataGroupsWithIdsIn(ids, DATA_GROUP_EXTENDED))
            .thenReturn(dataGroups);

        List<DataGroup> returnedDataGroups = dataGroupService.getBulkDataGroups(ids);
        assertEquals(dataGroups, returnedDataGroups);
    }

    @Test
    public void addDataGroupTest() {
        String dataGroupName = "name";
        String serviceAgreementId = "0955e686-d31e-4216-b3dd-5d66161d536d";
        String type = "ARRANGEMENTS";
        String description = "des";
        List<String> dataItemIds = singletonList("00001");

        DataGroupBase dataGroupBase = new DataGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withType(type)
            .withName(dataGroupName)
            .withDescription(description)
            .withItems(dataItemIds);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setDataItemType("ARRANGEMENTS");
        dataGroup.setId(UUID.randomUUID().toString());

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            eq(dataGroupBase.getName()),
            eq(dataGroupBase.getServiceAgreementId())
        )).thenReturn(new ArrayList<>());

        when(serviceAgreementJpaRepository.findById(anyString(), anyString()))
            .thenReturn(Optional.of(new ServiceAgreement().withMaster(false)));

        when(dataGroupJpaRepository.save(any(DataGroup.class)))
            .thenReturn(dataGroup);

        when(dataGroupItemJpaRepository.findAllByDataGroupId(anyString()))
            .thenReturn(new ArrayList<>());
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        String dataGroupId = dataGroupService.save(dataGroupBase);

        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        assertEquals(dataGroupName, commandArgumentCaptor.getValue().getName());
        assertEquals(description, commandArgumentCaptor.getValue().getDescription());
        assertEquals(type, commandArgumentCaptor.getValue().getDataItemType());
        assertEquals(0, commandArgumentCaptor.getValue().getDataItemIds().size());
        assertEquals(dataGroup.getId(), dataGroupId);
        verify(dataGroupItemJpaRepository).saveAll(Lists.newArrayList(new DataGroupItem()
            .withDataGroupId(dataGroupId)
            .withDataItemId("00001")));
        verify(dataGroupItemJpaRepository).findAllByDataGroupId(eq(dataGroupId));
        verify(approvalServiceAgreementJpaRepository, times(0)).existsByServiceAgreementId(anyString());
    }

    @Test
    public void addDataGroupWithEmptyDataItems() {
        String dataGroupName = "name";
        String serviceAgreementId = "0955e686-d31e-4216-b3dd-5d66161d536d";
        String type = "ARRANGEMENTS";
        String description = "des";

        DataGroupBase dataGroupBase = new DataGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withType(type)
            .withName(dataGroupName)
            .withDescription(description);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(UUID.randomUUID().toString());

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            eq(dataGroupBase.getName()),
            eq(dataGroupBase.getServiceAgreementId())
        )).thenReturn(new ArrayList<>());

        when(serviceAgreementJpaRepository.findById(anyString(), anyString()))
            .thenReturn(Optional.of(new ServiceAgreement().withMaster(false)));

        when(dataGroupJpaRepository.save(any(DataGroup.class)))
            .thenReturn(dataGroup);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        String dataGroupId = dataGroupService.save(dataGroupBase);

        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        assertEquals(dataGroupName, commandArgumentCaptor.getValue().getName());
        assertEquals(description, commandArgumentCaptor.getValue().getDescription());
        assertEquals(type, commandArgumentCaptor.getValue().getDataItemType());
        assertEquals(dataGroup.getId(), dataGroupId);
        assertThat(dataGroup.getDataItemIds(), is(emptySet()));
        verify(dataGroupItemJpaRepository, times(0)).saveAll(anyIterable());
        verify(approvalServiceAgreementJpaRepository, times(0)).existsByServiceAgreementId(anyString());
    }

    @Test
    public void shouldThrowBadRequestOnSaveDataGroupWhenNameIsNotUnique() {
        String dataGroupName = "name";
        String serviceAgreementId = "0955e686-d31e-4216-b3dd-5d66161d536d";
        String type = "ARRANGEMENTS";
        String description = "des";
        List<String> items = singletonList("00001");

        DataGroupBase dataGroupBase = new DataGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withType(type)
            .withName(dataGroupName)
            .withDescription(description)
            .withItems(items);

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            eq(dataGroupBase.getName()),
            eq(dataGroupBase.getServiceAgreementId())))
            .thenReturn(singletonList(
                new DataGroup()
                    .withId("1")
                    .withName(dataGroupName)
                    .withDataItemType(type)
                    .withDescription(description)
                    .withServiceAgreementId(serviceAgreementId)
                    .withDataItemIds(Sets.newHashSet())));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.save(dataGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestOnSaveDataGroupWhenServiceAgreementDoesNotExist() {
        String dataGroupName = "name";
        String serviceAgreementId = "0955e686-d31e-4216-b3dd-5d66161d536d";
        String type = "ARRANGEMENTS";
        String description = "des";
        List<String> items = Lists.newArrayList("001");

        DataGroupBase dataGroupBase = new DataGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withType(type)
            .withName(dataGroupName)
            .withDescription(description)
            .withItems(items);

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            eq(dataGroupBase.getName()),
            eq(dataGroupBase.getServiceAgreementId())
        )).thenReturn(new ArrayList<>());

        when(serviceAgreementJpaRepository.findById(anyString(), anyString()))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.save(dataGroupBase));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void updateDataGroupTest() {
        String dataGroupId = UUID.randomUUID().toString();
        String dataGroupName = "name";
        String description = "des";
        String type = "ARRANGEMENTS";
        List<String> items = singletonList("00001");
        String serviceAgreementId = "id.serviceagreement";

        DataGroupByIdPutRequestBody dataGroupByPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroupId)
            .withServiceAgreementId(serviceAgreementId)
            .withName(dataGroupName)
            .withDescription(description)
            .withType(type)
            .withItems(items);

        DataGroup dataGroup = new DataGroup();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        dataGroup.setServiceAgreement(serviceAgreement);
        dataGroup.setServiceAgreementId(serviceAgreementId);
        dataGroup.setId(dataGroupId);
        dataGroup.setName(dataGroupName);
        dataGroup.setDescription(description);
        dataGroup.setDataItemType(type);

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));
        doNothing().when(dataGroupItemJpaRepository).deleteAllByDataGroupId(anyString());
        when(dataGroupItemJpaRepository.saveAll(anyIterable())).thenReturn(new ArrayList<>());
        when(dataGroupItemJpaRepository.findAllByDataGroupId(anyString()))
            .thenReturn(new ArrayList<>());
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        dataGroupService.update(dataGroupId, dataGroupByPutRequestBody);
        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        assertEquals(dataGroupId, commandArgumentCaptor.getValue().getId());
        assertEquals(serviceAgreementId, commandArgumentCaptor.getValue().getServiceAgreement().getId());
        assertEquals(dataGroupName, commandArgumentCaptor.getValue().getName());
        assertEquals(description, commandArgumentCaptor.getValue().getDescription());
        assertEquals(type, commandArgumentCaptor.getValue().getDataItemType());
        assertEquals(0, commandArgumentCaptor.getValue().getDataItemIds().size());
        verify(dataGroupItemJpaRepository).deleteAllByDataGroupId(eq(dataGroup.getId()));
        verify(dataGroupItemJpaRepository).saveAll((Iterable<? extends DataGroupItem>) argThat(
            contains(getDataGroupItemMatcher(
                dataGroup, "00001"))));
        verify(dataGroupItemJpaRepository).findAllByDataGroupId(eq(dataGroupId));
        verify(approvalServiceAgreementJpaRepository, times(0)).existsByServiceAgreementId(anyString());
    }

    @Test
    public void updateDataGroupTestWithEmptyDataItems() {
        String dataGroupId = UUID.randomUUID().toString();
        String dataGroupName = "name";
        String description = "des";
        String type = "ARRANGEMENTS";
        List<String> items = emptyList();
        String serviceAgreementId = "id.serviceagreement";

        DataGroupByIdPutRequestBody dataGroupByPutRequestBody =
            new DataGroupByIdPutRequestBody()
                .withId(dataGroupId)
                .withServiceAgreementId(serviceAgreementId)
                .withName(dataGroupName)
                .withDescription(description)
                .withType(type)
                .withItems(items);

        DataGroup dataGroup = new DataGroup();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        dataGroup.setServiceAgreement(serviceAgreement);
        dataGroup.setServiceAgreementId(serviceAgreementId);
        dataGroup.setId(dataGroupId);
        dataGroup.setName(dataGroupName);
        dataGroup.setDescription(description);
        dataGroup.setDataItemType(type);

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        dataGroupService.update(dataGroupId, dataGroupByPutRequestBody);
        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        assertEquals(dataGroupId, commandArgumentCaptor.getValue().getId());
        assertEquals(serviceAgreementId, commandArgumentCaptor.getValue().getServiceAgreement().getId());
        assertEquals(dataGroupName, commandArgumentCaptor.getValue().getName());
        assertEquals(description, commandArgumentCaptor.getValue().getDescription());
        assertEquals(type, commandArgumentCaptor.getValue().getDataItemType());
        assertEquals(0, commandArgumentCaptor.getValue().getDataItemIds().size());
        verify(dataGroupItemJpaRepository).deleteAllByDataGroupId(eq(dataGroup.getId()));
        verify(dataGroupItemJpaRepository, times(0)).saveAll(anyIterable());
        verify(approvalServiceAgreementJpaRepository, times(0)).existsByServiceAgreementId(anyString());
    }

    @Test
    public void shouldGetByServiceAgreementIdAndDataItemType() {

        String type = "ARRANGEMENTS";
        String serviceAgreementId = "id.sa";

        List<DataGroup> repositoryResponse = new ArrayList<>();
        DataGroup dataGroup = createDataGroup("name.dg.1", type, "desc.dg.1", new ServiceAgreement());

        dataGroup.setDataItemIds(Collections.singleton("itemid"));
        repositoryResponse.add(dataGroup);

        when(dataGroupJpaRepository.findByServiceAgreementIdAndDataItemType(eq(serviceAgreementId), eq(type),
            anyString())).thenReturn(repositoryResponse);
        when(validationConfig.getTypes()).thenReturn(singletonList("ARRANGEMENTS"));
        ApprovalDataGroupDetails approvalDataGroupDetails = new ApprovalDataGroupDetails();
        approvalDataGroupDetails.setDataGroupId(dataGroup.getId());
        approvalDataGroupDetails.setApprovalId("approvalId");
        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(any(Set.class)))
            .thenReturn(Lists.newArrayList(approvalDataGroupDetails));

        List<DataGroupItemBase> serviceResponse = dataGroupService
            .getByServiceAgreementIdAndDataItemType(serviceAgreementId, type, true);

        verify(dataGroupJpaRepository, times(1))
            .findByServiceAgreementIdAndDataItemType(eq(serviceAgreementId), eq(type), eq(DATA_GROUP_EXTENDED));
        assertEquals(1, serviceResponse.size());
        DataGroupItemBase dataGroupItemBase = serviceResponse.get(0);
        assertEquals(dataGroup.getId(), dataGroupItemBase.getId());
        assertEquals(dataGroup.getDataItemType(), dataGroupItemBase.getType());
        assertEquals(dataGroup.getName(), dataGroupItemBase.getName());
        assertEquals(dataGroup.getDescription(), dataGroupItemBase.getDescription());
        assertEquals(approvalDataGroupDetails.getApprovalId(), dataGroupItemBase.getApprovalId());
        assertTrue(dataGroup.getDataItemIds().containsAll(dataGroupItemBase.getItems()));
    }

    @Test
    public void shouldGetByServiceAgreementIdAndDataItemTypeNull() {
        String serviceAgreementId = "id.sa";

        List<DataGroup> repositoryResponse = new ArrayList<>();
        DataGroup dataGroup = createDataGroup("name.dg.1", "ARRANGEMENTS", "desc.dg.1", new ServiceAgreement());

        dataGroup.setDataItemIds(Collections.singleton("itemid"));
        repositoryResponse.add(dataGroup);

        when(dataGroupJpaRepository.findByServiceAgreementId(eq(serviceAgreementId), anyString()))
            .thenReturn(repositoryResponse);

        when(approvalDataGroupJpaRepository.findByDataGroupIdIn(any(Set.class)))
            .thenReturn(new ArrayList());

        List<DataGroupItemBase> serviceResponse = dataGroupService
            .getByServiceAgreementIdAndDataItemType(serviceAgreementId, null, true);

        verify(dataGroupJpaRepository, times(1))
            .findByServiceAgreementId(eq(serviceAgreementId), eq(DATA_GROUP_EXTENDED));
        assertEquals(1, serviceResponse.size());
        DataGroupItemBase dataGroupItemBase = serviceResponse.get(0);
        assertEquals(dataGroup.getId(), dataGroupItemBase.getId());
        assertEquals(dataGroup.getDataItemType(), dataGroupItemBase.getType());
        assertEquals(dataGroup.getName(), dataGroupItemBase.getName());
        assertEquals(dataGroup.getDescription(), dataGroupItemBase.getDescription());
        assertNull(dataGroupItemBase.getApprovalId());
        assertTrue(dataGroup.getDataItemIds().containsAll(dataGroupItemBase.getItems()));
    }


    @Test
    public void shouldGetByServiceAgreementIdAndInvalidDataItemType() {
        String serviceAgreementId = "id.sa";
        DataGroup dataGroup = createDataGroup("name.dg.1", "ARRANGEMENTS", "desc.dg.1", new ServiceAgreement());

        dataGroup.setDataItemIds(Collections.singleton("itemid"));
        when(validationConfig.getTypes()).thenReturn(singletonList("ARRANGEMENTS"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.getByServiceAgreementIdAndDataItemType(serviceAgreementId, "INVALID", false));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_050.getErrorMessage(), ERR_ACC_050.getErrorCode()));
    }

    @Test
    public void deleteDataGroupTest() {
        String dataGroupId = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup().withId(dataGroupId).withServiceAgreementId("sa1");

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(false);
        when(dataGroupJpaRepository.findById(anyString())).thenReturn(Optional.of(dataGroup));
        doNothing().when(dataGroupJpaRepository).deleteById(eq(dataGroupId));
        doNothing().when(dataGroupItemJpaRepository).deleteAllByDataGroupId(eq(dataGroup.getId()));

        dataGroupService.delete(dataGroupId);

        verify(dataGroupItemJpaRepository).deleteAllByDataGroupId(eq(dataGroup.getId()));
        verify(dataGroupJpaRepository).deleteById(eq(dataGroupId));
        verify(userAssignedFunctionGroupDataGroupRepository).existsByDataGroupIdsIn(eq(Sets.newHashSet(dataGroupId)));
        verify(approvalServiceAgreementJpaRepository, times(0)).existsByServiceAgreementId(anyString());
    }

    @Test
    public void shouldDeleteDataGroupApproval() {
        String dataGroupId = UUID.randomUUID().toString();
        String approvalId = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup().withId(dataGroupId).withServiceAgreementId("sa1");

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(anyString())).thenReturn(Optional.of(dataGroup));
        when(userAssignedFunctionGroupDataGroupRepository.existsByDataGroupIdsIn(eq(Sets.newHashSet(dataGroupId)))).thenReturn(false);
        dataGroupService.deleteDataGroupApproval(dataGroupId, approvalId);

        verify(approvalDataGroupJpaRepository).save(any(ApprovalDataGroup.class));
        verify(userAssignedFunctionGroupDataGroupRepository).existsByDataGroupIdsIn(eq(Sets.newHashSet(dataGroupId)));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa1"));
    }

    @Test
    public void shouldThrowBadRequestWhenDataGroupIsAssignedToUsersWhenTryingToDeleteDataGroupApproval() {
        String dataGroupId = UUID.randomUUID().toString();
        String approvalId = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dataGroupId);
        dataGroup.setName("dataGroupName");
        dataGroup.setDescription("description");
        dataGroup.setDataItemType("type");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("otherServiceagreementId");
        dataGroup.setServiceAgreement(serviceAgreement);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(eq(dataGroupId)))
            .thenReturn(Optional.of(dataGroup));
        when(userAssignedFunctionGroupDataGroupRepository.existsByDataGroupIdsIn(eq(Sets.newHashSet(dataGroupId)))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.deleteDataGroupApproval(dataGroupId, approvalId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_013.getErrorMessage(), ERR_ACC_013.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenDataGroupHasPendingAssignmentToUsersWhenTryingToDeleteDataGroupApproval() {
        String dataGroupId = UUID.randomUUID().toString();
        String approvalId = UUID.randomUUID().toString();
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dataGroupId);
        dataGroup.setName("dataGroupName");
        dataGroup.setDescription("description");
        dataGroup.setDataItemType("type");

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("otherServiceagreementId");
        dataGroup.setServiceAgreement(serviceAgreement);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(eq(dataGroupId)))
            .thenReturn(Optional.of(dataGroup));
        when(approvalUserContextAssignFunctionGroupJpaRepository.existsByDataGroups(eq(dataGroupId))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.deleteDataGroupApproval(dataGroupId, approvalId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_074.getErrorMessage(), ERR_ACC_074.getErrorCode()));
    }

    @Test
    public void shouldFailDeleteOfDataGroupWhenAssignedToUsers() {
        String dataGroupId = UUID.randomUUID().toString();
        when(userAssignedFunctionGroupDataGroupRepository.existsByDataGroupIdsIn(eq(Sets.newHashSet(dataGroupId)))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.delete(dataGroupId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_013.getErrorMessage(), ERR_ACC_013.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenAlreadyASameDeleteIsInPendingApprovalTableWhenTryingToDeleteDataGroup() {
        String dataGroupId = UUID.randomUUID().toString();
        when(approvalDataGroupJpaRepository.existsByDataGroupId(eq(dataGroupId))).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.delete(dataGroupId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode()));
    }

    @Test
    public void shouldFailDeleteOfDataGroupWhenDataGroupNotExists() {
        String dataGroupId = UUID.randomUUID().toString();

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.delete(dataGroupId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_015.getErrorMessage(), ERR_ACC_015.getErrorCode()));
    }

    @Test
    public void shouldDisallowUpdateOfServiceAgreementForDataGroups() {
        String dataGroupId = UUID.randomUUID().toString();
        String dataGroupName = "name";
        String description = "des";
        String type = "ARRANGEMENTS";
        List<String> items = singletonList("00001");
        String serviceAgreementId = "id.serviceagreement";

        DataGroupByIdPutRequestBody dataGroupByPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroupId)
            .withServiceAgreementId(serviceAgreementId)
            .withName(dataGroupName)
            .withDescription(description)
            .withType(type)
            .withItems(items);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dataGroupId);
        dataGroup.setName(dataGroupName);
        dataGroup.setDescription(description);
        dataGroup.setDataItemType(type);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("otherServiceagreementId");
        dataGroup.setServiceAgreement(serviceAgreement);

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.update(dataGroupId, dataGroupByPutRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_031.getErrorMessage(), ERR_ACC_031.getErrorCode()));
    }

    @Test
    public void shouldFailWhenMismatchingIdFromBodyAndPathAreSend() {

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.update("1",
            new DataGroupByIdPutRequestBody()
                .withId("other")
        ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_051.getErrorMessage(), ERR_ACC_051.getErrorCode()));
    }

    @Test
    public void shouldRetrieveDataGroupIdWhenIdIdentifierIsProvided() {
        String dataGroupId = "dgId";

        String retrievedId = dataGroupService.retrieveDataGroupIdFromIdentifier(new PresentationIdentifier()
            .withIdIdentifier(dataGroupId));

        assertEquals(dataGroupId, retrievedId);
    }

    @Test
    public void shouldRetrieveDataGroupIdWhenNameIdentifierIsProvided() {
        String dataGroupId = "dgId";
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dataGroupId);

        String retrievedId = dataGroupService.retrieveDataGroupIdFromIdentifier(new PresentationIdentifier()
            .withIdIdentifier(dataGroupId));

        assertEquals(dataGroupId, retrievedId);
    }

    @Test
    public void shouldThrowBadRequestWhenNameIdentifierIsProvidedButIsInvalid() {
        String dataGroupId = "dgId";
        String name = "DG-NAME";
        String externalServiceAgreementId = "SA-01";
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withNameIdentifier(new NameIdentifier()
                .withName(name)
                .withExternalServiceAgreementId(externalServiceAgreementId));
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dataGroupId);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.retrieveDataGroupIdFromIdentifier(dataGroupIdentifier));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_051.getErrorMessage(), ERR_ACC_051.getErrorCode()));
    }

    @Test
    public void shouldSuccessfulUpdateDataGroupWithAddingNewDataItems() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement().withId("sa1");

        Set<String> dataGroupItems = Sets.newHashSet(singletonList("ItemId"));
        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withServiceAgreementId("sa1")
            .withDescription("Desc")
            .withName("Name")
            .withDataItemIds(dataGroupItems);
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType(arrangements)
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));
        when(dataGroupItemJpaRepository.existsByDataGroupIdAndDataItemIdIn(eq(dataGroup.getId()), anySet()))
            .thenReturn(false);
        when(dataGroupItemJpaRepository.saveAll(anyIterable())).thenReturn(new ArrayList<>());
        when(dataGroupItemJpaRepository.findAllByDataGroupId(anyString()))
            .thenReturn(new ArrayList<>());

        dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody);

        verify(dataGroupItemJpaRepository).saveAll((Iterable<? extends DataGroupItem>) argThat(
            contains(getDataGroupItemMatcher(
                dataGroup, "dataItemId"))));
        verify(dataGroupItemJpaRepository).findAllByDataGroupId(eq(dataGroupId));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa1"));
    }

    @Test
    public void shouldSuccessfulUpdateDataGroupWithRemovingNewDataItems() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement().withId("sa1");

        DataGroupItem dataItem = new DataGroupItem();
        dataItem.setDataItemId("ItemId");
        dataItem.setDataGroupId(dataGroupId);

        DataGroupItem dataItemForRemoval = new DataGroupItem();
        dataItemForRemoval.setDataItemId("removeId");
        dataItemForRemoval.setDataGroupId(dataGroupId);

        Set<String> dataItemIds = Sets.newHashSet("removeId", "ItemId");
        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withServiceAgreementId("sa1")
            .withDescription("Desc")
            .withName("Name")
            .withDataItemIds(dataItemIds);
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("removeId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType(arrangements)
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(eq(dataGroupId))).thenReturn(Optional.of(dataGroup));
        when(dataGroupItemJpaRepository.findByDataGroupIdAndDataItemIdIn(eq(dataGroup.getId()), anySet()))
            .thenReturn(singletonList(dataItemForRemoval));
        doNothing().when(dataGroupItemJpaRepository)
            .deleteAllByDataGroupIdAndItemIdIn(eq(dataGroup.getId()), eq(new HashSet<>(
                Collections.singletonList("removeId"))));
        dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody);

        verify(dataGroupItemJpaRepository, times(1))
            .deleteAllByDataGroupIdAndItemIdIn(eq(dataGroup.getId()), eq(new HashSet<>(
                Collections.singletonList("removeId"))));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa1"));
    }

    @Test
    public void shouldUpdateDataGroupWhenAllItemsInDataGroupAreSetForRemoval() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement().withId("sa1");

        DataGroupItem dataItem = new DataGroupItem();
        dataItem.setDataItemId("ItemId");
        dataItem.setDataGroupId(dataGroupId);

        DataGroupItem dataItemForRemoval = new DataGroupItem();
        dataItemForRemoval.setDataItemId("removeId");
        dataItemForRemoval.setDataGroupId(dataGroupId);

        Set<String> dataItemIds = Sets.newHashSet("ItemId", "removeId");
        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withServiceAgreementId("sa1")
            .withDescription("Desc")
            .withName("Name")
            .withDataItemIds(dataItemIds);

        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("removeId");
        PresentationItemIdentifier persistenceItemIdentifier2 = new PresentationItemIdentifier()
            .withInternalIdIdentifier("itemId");
        List<PresentationItemIdentifier> dataItems = Arrays
            .asList(persistenceItemIdentifier, persistenceItemIdentifier2);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType(arrangements)
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);
        when(dataGroupJpaRepository.findById(eq(dataGroupId))).thenReturn(Optional.of(dataGroup));
        when(dataGroupItemJpaRepository.findByDataGroupIdAndDataItemIdIn(eq(dataGroup.getId()), anySet()))
            .thenReturn(asList(dataItemForRemoval, dataItem));

        dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody);

        verify(dataGroupItemJpaRepository, times(1))
            .deleteAllByDataGroupIdAndItemIdIn(eq(dataGroup.getId()), eq(new HashSet<>(asList("removeId", "itemId"))));
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq("sa1"));
    }

    @Test
    public void shouldThrowBadRequestWhenUpdatingDataItemsWhenRemovingNonExistingItem() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement();

        DataGroupItem dataItem = new DataGroupItem();
        dataItem.setDataItemId("ItemId");
        dataItem.setDataGroupId(dataGroupId);

        DataGroupItem dataItemForRemoval = new DataGroupItem();
        dataItemForRemoval.setDataItemId("removeId");
        dataItemForRemoval.setDataGroupId(dataGroupId);

        Set<String> dataItemIds = Sets.newHashSet("ItemId", "removeId");
        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withDescription("Desc")
            .withName("Name")
            .withDataItemIds(dataItemIds);

        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("removeId");
        PresentationItemIdentifier persistenceItemIdentifier2 = new PresentationItemIdentifier()
            .withInternalIdIdentifier("random-id");
        List<PresentationItemIdentifier> dataItems = Arrays
            .asList(persistenceItemIdentifier, persistenceItemIdentifier2);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType(arrangements)
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(dataGroupJpaRepository.findById(eq(dataGroupId))).thenReturn(Optional.of(dataGroup));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_079.getErrorMessage(), ERR_ACC_079.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenUpdatingDataItemsWhenAddingAlreadyExistingItem() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement();

        DataGroupItem dataItem = new DataGroupItem();
        dataItem.setDataItemId("ItemId");
        dataItem.setDataGroupId(dataGroupId);

        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withDescription("Desc")
            .withName("Name");
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("itemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType(arrangements)
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));
        when(dataGroupItemJpaRepository.existsByDataGroupIdAndDataItemIdIn(eq(dataGroup.getId()), anySet()))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_081.getErrorMessage(), ERR_ACC_081.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenUpdatingDataItemsWithInvalidType() {
        String dataGroupId = "dataGroupId";
        String arrangements = "ARRANGEMENTS";
        ServiceAgreement serviceAgreement = new ServiceAgreement();

        DataGroupItem dataItem = new DataGroupItem();
        dataItem.setDataItemId("ItemId");
        dataItem.setDataGroupId(dataGroupId);
        Set<String> dataGroupItems = Sets.newHashSet("ItemId");
        DataGroup dataGroup = new DataGroup()
            .withId(dataGroupId)
            .withDataItemType(arrangements)
            .withServiceAgreement(serviceAgreement)
            .withDescription("Desc")
            .withName("Name")
            .withDataItemIds(dataGroupItems);
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("CONTACTS")
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(dataGroup));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_053.getErrorMessage(), ERR_ACC_053.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenDataItemDoesNotExist() {

        String dataGroupId = "dataGroupId";
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("CONTACTS")
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_051.getErrorMessage(), ERR_ACC_051.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeDataGroupIsAssignedToCustomSAInBatchPatch() {

        String dataGroupId = "dataGroupId";
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("CUSTOMERS")
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        DataGroup value = new DataGroup().withDataItemType("CUSTOMERS");
        value.setId(dataGroupId);
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withMaster(false)
            .withCreatorLegalEntity(new LegalEntity().withId("leid"));
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_097.getErrorMessage(), ERR_ACC_097.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeDataGroupWhenItemsAreNotInHierarchyInBatchPatch() {

        String dataGroupId = "dataGroupId";
        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("CUSTOMERS")
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        DataGroup value = new DataGroup().withDataItemType("CUSTOMERS");
        value.setId(dataGroupId);
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withMaster(true)
            .withCreatorLegalEntity(new LegalEntity().withId("leid"));
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));
        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(anyString(), anyCollection()))
            .thenReturn(new ArrayList<>());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_099.getErrorMessage(), ERR_ACC_099.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenDataItemDoesNotExistByNameIdentifier() {

        PresentationItemIdentifier persistenceItemIdentifier = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dataItemId");
        List<PresentationItemIdentifier> dataItems = singletonList(persistenceItemIdentifier);
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withNameIdentifier(
                new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier()
                    .withName("random-name")
                    .withExternalServiceAgreementId("external-id")
            );
        PresentationDataGroupItemPutRequestBody requestBody = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("CONTACTS")
            .withDataItems(dataItems)
            .withDataGroupIdentifier(dataGroupIdentifier);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.updateDataGroupItemsByIdIdentifier(requestBody));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_051.getErrorMessage(), ERR_ACC_051.getErrorCode()));
    }

    @Test
    public void shouldGetByApprovalIdWithActionCreate() {
        String approvalId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();

        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(serviceAgreementId);
        approvalDetails.setName("DG-NAME");
        approvalDetails.setDescription("DG-DESC");
        approvalDetails.setItems(new HashSet<>(Collections.singletonList("item-1")));
        approvalDetails.setType("ARRANGEMENTS");

        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalDetails));
        String creatorLeInternalId = UUID.randomUUID().toString();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SA-NAME");
        serviceAgreement.setMaster(true);
        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId(creatorLeInternalId);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.addParticipant(new Participant()
            .withShareAccounts(true)
            .withShareUsers(true)
            .withLegalEntity(creatorLegalEntity));
        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), anyString()))
            .thenReturn(Optional.of(serviceAgreement));

        PresentationDataGroupApprovalDetailsItem byApprovalId = dataGroupService.getByApprovalId(approvalId);

        assertEquals(approvalId, byApprovalId.getApprovalId());
        assertNull(byApprovalId.getDataGroupId());
        assertEquals(PresentationApprovalAction.EDIT.CREATE, byApprovalId.getAction());

        assertEquals(serviceAgreementId, byApprovalId.getServiceAgreementId());
        assertEquals(serviceAgreement.getName(), byApprovalId.getServiceAgreementName());
        assertEquals(approvalDetails.getType(), byApprovalId.getType());
        assertNull(byApprovalId.getOldState());
        assertEquals("DG-NAME", byApprovalId.getNewState().getName());
        assertEquals("DG-DESC", byApprovalId.getNewState().getDescription());
        assertEquals(approvalDetails.getItems(), byApprovalId.getAddedDataItems());
        assertEquals(emptySet(), byApprovalId.getRemovedDataItems());
        assertEquals(emptySet(), byApprovalId.getUnmodifiedDataItems());
        assertTrue(byApprovalId.getLegalEntityIds().contains(creatorLeInternalId));
    }

    @Test
    public void shouldGetByApprovalIdWithActionUpdate() {
        String approvalId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String dataGroupId = UUID.randomUUID().toString();

        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(dataGroupId);
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(serviceAgreementId);
        approvalDetails.setName("DG-NAME");
        approvalDetails.setDescription("DG-DESC");
        approvalDetails.setItems(new HashSet<>(Arrays.asList("unmodified-1", "added-1")));
        approvalDetails.setType("ARRANGEMENTS");

        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalDetails));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SA-NAME");
        serviceAgreement.setMaster(false);
        Map<String, Participant> participants = new HashMap<>();
        Participant par1 = new Participant();
        par1.setShareAccounts(true);
        LegalEntity le1 = new LegalEntity();
        le1.setId("1");
        par1.setLegalEntity(le1);
        par1.setLegalEntity(le1);
        Participant par2 = new Participant();
        par2.setShareAccounts(false);
        LegalEntity le2 = new LegalEntity();
        le2.setId("2");
        par2.setLegalEntity(le2);
        participants.put("1", par1);
        participants.put("2", par2);
        serviceAgreement.setParticipants(participants);

        DataGroup dataGroup = createDataGroup("OLD-NAME", "ARRANGEMENTS", "OLD-DESC", serviceAgreement,
            Arrays.asList("unmodified-1", "removed-1"));

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_EXTENDED)))
            .thenReturn(Optional.of(dataGroup));

        PresentationDataGroupApprovalDetailsItem byApprovalId = dataGroupService.getByApprovalId(approvalId);

        assertEquals(approvalId, byApprovalId.getApprovalId());
        assertEquals(dataGroupId, byApprovalId.getDataGroupId());
        assertEquals(PresentationApprovalAction.EDIT, byApprovalId.getAction());

        assertTrue(byApprovalId.getLegalEntityIds().contains(par1.getLegalEntity().getId()));
        assertFalse(byApprovalId.getLegalEntityIds().contains(par2.getLegalEntity().getId()));
        assertEquals(serviceAgreementId, byApprovalId.getServiceAgreementId());
        assertEquals(dataGroupId, byApprovalId.getDataGroupId());
        assertEquals(serviceAgreement.getName(), byApprovalId.getServiceAgreementName());
        assertEquals(approvalDetails.getType(), byApprovalId.getType());
        assertEquals("OLD-NAME", byApprovalId.getOldState().getName());
        assertEquals("OLD-DESC", byApprovalId.getOldState().getDescription());
        assertEquals("DG-NAME", byApprovalId.getNewState().getName());
        assertEquals("DG-DESC", byApprovalId.getNewState().getDescription());
        assertEquals(new HashSet<>(singletonList("added-1")), byApprovalId.getAddedDataItems());
        assertEquals(new HashSet<>(singletonList("unmodified-1")), byApprovalId.getUnmodifiedDataItems());
        assertEquals(new HashSet<>(singletonList("removed-1")), byApprovalId.getRemovedDataItems());
    }

    @Test
    public void shouldGetByApprovalIdWithActionDelete() {
        String approvalId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String dataGroupId = UUID.randomUUID().toString();

        ApprovalDataGroup approvalDetails = new ApprovalDataGroup();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(dataGroupId);

        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalDetails));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("SA-NAME");

        DataGroup dataGroup = createDataGroup("OLD-NAME", "ARRANGEMENTS", "OLD-DESC", serviceAgreement,
            Arrays.asList("unmodified-1", "removed-1"));

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_EXTENDED)))
            .thenReturn(Optional.of(dataGroup));

        PresentationDataGroupApprovalDetailsItem byApprovalId = dataGroupService.getByApprovalId(approvalId);

        assertEquals(approvalId, byApprovalId.getApprovalId());
        assertEquals(dataGroupId, byApprovalId.getDataGroupId());
        assertEquals(PresentationApprovalAction.DELETE, byApprovalId.getAction());

        assertEquals(serviceAgreementId, byApprovalId.getServiceAgreementId());
        assertEquals(dataGroupId, byApprovalId.getDataGroupId());
        assertEquals(serviceAgreement.getName(), byApprovalId.getServiceAgreementName());
        assertEquals(dataGroup.getDataItemType(), byApprovalId.getType());
        assertEquals("OLD-NAME", byApprovalId.getOldState().getName());
        assertEquals("OLD-DESC", byApprovalId.getOldState().getDescription());
        assertEquals("OLD-NAME", byApprovalId.getNewState().getName());
        assertEquals("OLD-DESC", byApprovalId.getNewState().getDescription());
        assertEquals(emptySet(), byApprovalId.getAddedDataItems());
        assertEquals(emptySet(), byApprovalId.getUnmodifiedDataItems());
        assertEquals(dataGroup.getDataItemIds(), byApprovalId.getRemovedDataItems());
    }

    @Test
    public void shouldThrowErrorIfApprovalDoesNotExist() {
        String approvalId = UUID.randomUUID().toString();
        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.getByApprovalId(approvalId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_060.getErrorMessage(), ERR_ACQ_060.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorIfServiceAgreementDoesNotExistOnCreate() {
        String approvalId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();

        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(serviceAgreementId);
        approvalDetails.setName("DG-NAME");
        approvalDetails.setDescription("DG-DESC");
        approvalDetails.setItems(new HashSet<>(Collections.singletonList("item-1")));
        approvalDetails.setType("ARRANGEMENTS");

        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalDetails));

        when(serviceAgreementJpaRepository.findById(eq(serviceAgreementId), anyString()))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.getByApprovalId(approvalId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorIfDataGroupDoesNotExistOnUpdate() {
        String approvalId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String dataGroupId = UUID.randomUUID().toString();

        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(dataGroupId);
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(serviceAgreementId);
        approvalDetails.setName("DG-NAME");
        approvalDetails.setDescription("DG-DESC");
        approvalDetails.setItems(new HashSet<>(Arrays.asList("unmodified-1", "added-1")));
        approvalDetails.setType("ARRANGEMENTS");

        when(approvalDataGroupJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalDetails));

        when(dataGroupJpaRepository.findById(eq(dataGroupId), eq(DATA_GROUP_EXTENDED)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dataGroupService.getByApprovalId(approvalId));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldUpdateDataGroupByIdentifier() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            )
            .withName("name")
            .withDescription("description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(UUID.randomUUID().toString())
                )
            );

        DataGroup value = new DataGroup();
        value.setId(idIdentifier);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        value.setServiceAgreementId(serviceAgreement.getId());
        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementIdAndIdNot(
            eq("name"),
            eq(serviceAgreement.getId()),
            eq(idIdentifier)
        )).thenReturn(emptyList());
        DataGroup response = new DataGroup().withId(idIdentifier);
        when(dataGroupJpaRepository.save(value)).thenReturn(response);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        dataGroupService.update(body);

        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        DataGroup capturedValue = commandArgumentCaptor.getValue();

        assertEquals(body.getName(), capturedValue.getName());
        assertEquals(body.getDescription(), capturedValue.getDescription());
        assertEquals(body.getType(), capturedValue.getDataItemType());
        assertEquals(0, capturedValue.getDataItemIds().size());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreement.getId()));
    }

    @Test
    public void shouldUpdateDataGroupByIdentifierWithEmptyDataItems() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            )
            .withName("name")
            .withDescription("description")
            .withType("ARRANGEMENTS");

        DataGroup value = new DataGroup();
        value.setId(idIdentifier);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        value.setServiceAgreementId(serviceAgreement.getId());
        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementIdAndIdNot(
            eq("name"),
            eq(serviceAgreement.getId()),
            eq(idIdentifier)
        )).thenReturn(emptyList());
        DataGroup response = new DataGroup().withId(idIdentifier);
        when(dataGroupJpaRepository.save(value)).thenReturn(response);
        when(applicationProperties.getApproval().getValidation().isEnabled()).thenReturn(true);

        dataGroupService.update(body);

        ArgumentCaptor<DataGroup> commandArgumentCaptor = ArgumentCaptor.forClass(DataGroup.class);
        verify(dataGroupJpaRepository).save(commandArgumentCaptor.capture());

        DataGroup capturedValue = commandArgumentCaptor.getValue();

        assertEquals(body.getName(), capturedValue.getName());
        assertEquals(body.getDescription(), capturedValue.getDescription());
        assertEquals(body.getType(), capturedValue.getDataItemType());
        assertEquals(0, capturedValue.getDataItemIds().size());
        verify(approvalServiceAgreementJpaRepository).existsByServiceAgreementId(eq(serviceAgreement.getId()));
    }

    @Test
    public void shouldThrowBadRequestIfNameIsNotUnique() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            )
            .withName("name")
            .withDescription("description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(UUID.randomUUID().toString())
                )
            );

        DataGroup value = new DataGroup();
        value.setId(idIdentifier);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        when(dataGroupJpaRepository.findDistinctByNameAndServiceAgreementIdAndIdNot(
            "name",
            serviceAgreement.getId(),
            idIdentifier)
        ).thenReturn(singletonList(value));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.update(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeDataGroupIsAssignedToCustomSAInBatchUpdate() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            )
            .withName("name")
            .withDescription("description")
            .withType("CUSTOMERS")
            .withDataItems(
                singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(UUID.randomUUID().toString())
                )
            );

        DataGroup value = new DataGroup();
        value.setId(idIdentifier);
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withMaster(false)
            .withCreatorLegalEntity(new LegalEntity().withId("leid"));
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(new ArrayList<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.update(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_097.getErrorMessage(), ERR_ACC_097.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCustomerTypeItemIsNotInHierarchyBatchUpdate() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            )
            .withName("name")
            .withDescription("description")
            .withType("CUSTOMERS")
            .withDataItems(
                singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(UUID.randomUUID().toString())
                )
            );

        DataGroup value = new DataGroup();
        value.setId(idIdentifier);
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withMaster(true)
            .withCreatorLegalEntity(new LegalEntity().withId("leid"));
        serviceAgreement.setId(UUID.randomUUID().toString());
        value.setServiceAgreement(serviceAgreement);
        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.of(value));

        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(anyString(), anyCollection()))
            .thenReturn(new ArrayList<>());

        when(dataGroupJpaRepository
            .findDistinctByNameAndServiceAgreementIdAndIdNot(anyString(), anyString(), anyString()))
            .thenReturn(new ArrayList<>());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> dataGroupService.update(body));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_099.getErrorMessage(), ERR_ACC_099.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundWhenInvalidIdIdentifier() {
        String idIdentifier = UUID.randomUUID().toString();
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(idIdentifier)
            );

        when(dataGroupJpaRepository.findById(eq(idIdentifier), eq(DATA_GROUP_WITH_SA_CREATOR)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> dataGroupService.update(body));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_085.getErrorMessage(), ERR_ACC_085.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundWhenInvalidNameIdentifier() {
        PresentationSingleDataGroupPutRequestBody body = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withNameIdentifier(
                        new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier()
                            .withExternalServiceAgreementId("ex-sa-id")
                            .withName("name")
                    )
            );

        NotFoundException exception = assertThrows(NotFoundException.class, () -> dataGroupService.update(body));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_085.getErrorMessage(), ERR_ACC_085.getErrorCode()));
    }

    private Matcher<DataGroupItem> getDataGroupItemMatcher(DataGroup dataGroup, String itemId) {
        return allOf(
            hasProperty("dataGroupId", equalTo(dataGroup.getId())),
            hasProperty("dataItemId", equalTo(itemId))
        );
    }
}