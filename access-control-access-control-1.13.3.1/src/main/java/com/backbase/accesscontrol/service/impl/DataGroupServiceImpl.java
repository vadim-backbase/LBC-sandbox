package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.DomainConstants.CUSTOMERS_DATA_GROUP_TYPE;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_WITH_SA_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_015;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_050;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_051;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_053;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_079;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_081;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_082;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_083;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_085;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_097;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_099;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_107;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_060;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.dto.IdentifierDto;
import com.backbase.accesscontrol.dto.NameIdentifier;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.mappers.DataGroupDomainMapper;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.IdProjection;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.DataGroupHandlerUtil;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.accesscontrol.util.errorcodes.ErrorCode;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.approvals.PersistenceDataGroupState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DataGroupServiceImpl implements DataGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupServiceImpl.class);

    private DataGroupJpaRepository dataGroupJpaRepository;
    private DataGroupItemJpaRepository dataGroupItemJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private UserAssignedCombinationRepository userAssignedCombinationRepository;
    private ValidationConfig validationConfig;
    private ApprovalUserContextAssignFunctionGroupJpaRepository
        approvalUserContextAssignFunctionGroupJpaRepository;
    private ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    private ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    private LegalEntityJpaRepository legalEntityJpaRepository;
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    private ApplicationProperties applicationProperties;

    private DataGroupDomainMapper mapper;


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DataGroup getByIdWithExtendedData(String id) {
        LOGGER.info("Trying to get Data group with id {} ", id);
        return getDataGroup(new IdentifierDto().withIdIdentifier(id), ERR_ACQ_001, DATA_GROUP_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DataGroup getById(String id) {
        LOGGER.info("Trying to get Data group with id {} ", id);
        return getDataGroup(new IdentifierDto().withIdIdentifier(id), ERR_ACQ_001, null);
    }

    private DataGroup getById(String id, String entityGraph) {
        LOGGER.info("Trying to get Data group with id {} with items: {}", id, entityGraph);
        return getDataGroup(new IdentifierDto().withIdIdentifier(id), ERR_ACQ_001, entityGraph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PresentationDataGroupApprovalDetailsItem getByApprovalId(String approvalId) {
        LOGGER.info("Trying to get Approval details group with id {}", approvalId);
        ApprovalDataGroup approvalDataGroup = approvalDataGroupJpaRepository
            .findByApprovalId(approvalId)
            .orElseThrow(() -> {
                LOGGER.warn("Approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_060.getErrorMessage(), ERR_ACQ_060.getErrorCode());
            });

        return getPresentationDataGroupApprovalDetailsItem(approvalDataGroup);
    }

    private PresentationDataGroupApprovalDetailsItem getPresentationDataGroupApprovalDetailsItem(
        ApprovalDataGroup approvalDataGroup) {
        PresentationDataGroupApprovalDetailsItem dataGroupApprovalDetailsItem =
            new PresentationDataGroupApprovalDetailsItem()
                .withApprovalId(approvalDataGroup.getApprovalId())
                .withDataGroupId(approvalDataGroup.getDataGroupId())
                .withAction(PresentationApprovalAction.valueOf(approvalDataGroup.getApprovalAction().name()));

        if (approvalDataGroup.getApprovalAction() == ApprovalAction.CREATE) {
            populateDetailsForApprovalCreate((ApprovalDataGroupDetails) approvalDataGroup,
                dataGroupApprovalDetailsItem);
        } else {
            DataGroup dataGroup = getById(approvalDataGroup.getDataGroupId(), DATA_GROUP_EXTENDED);
            dataGroupApprovalDetailsItem
                .withServiceAgreementId(dataGroup.getServiceAgreement().getId())
                .withServiceAgreementName(dataGroup.getServiceAgreement().getName())
                .withType(dataGroup.getDataItemType())
                .withOldState(
                    new PersistenceDataGroupState()
                        .withName(dataGroup.getName())
                        .withDescription(dataGroup.getDescription())
                );

            setLegalEntityIds(dataGroup.getServiceAgreement(), dataGroupApprovalDetailsItem);

            populateNewState(approvalDataGroup, dataGroup, dataGroupApprovalDetailsItem);

            populateDataItemsDifference(approvalDataGroup, dataGroup, dataGroupApprovalDetailsItem);
        }

        return dataGroupApprovalDetailsItem;
    }

    private void setLegalEntityIds(ServiceAgreement serviceAgreement,
        PresentationDataGroupApprovalDetailsItem persistenceDataGroupApprovalDetailsItem) {

        persistenceDataGroupApprovalDetailsItem
            .setLegalEntityIds(getParticipantsWithSharingAccounts(serviceAgreement));
    }

    private Set<String> getParticipantsWithSharingAccounts(ServiceAgreement serviceAgreement) {

        return serviceAgreement.getParticipants().values().stream().filter(Participant::isShareAccounts)
            .map(p -> p.getLegalEntity().getId()).collect(Collectors.toSet());
    }

    private void populateNewState(ApprovalDataGroup approvalDataGroup, DataGroup dataGroup,
        PresentationDataGroupApprovalDetailsItem persistenceDataGroupApprovalDetailsItem) {
        LOGGER.info("Populating new state for approval with id {} and action {}", approvalDataGroup.getApprovalId(),
            approvalDataGroup.getApprovalAction());
        if (approvalDataGroup.getApprovalAction() == ApprovalAction.DELETE) {
            persistenceDataGroupApprovalDetailsItem.withNewState(
                new PersistenceDataGroupState()
                    .withName(dataGroup.getName())
                    .withDescription(dataGroup.getDescription())
            );
        } else {
            ApprovalDataGroupDetails approvalDataGroupDetails = (ApprovalDataGroupDetails) approvalDataGroup;
            persistenceDataGroupApprovalDetailsItem.withNewState(
                new PersistenceDataGroupState()
                    .withName(approvalDataGroupDetails.getName())
                    .withDescription(approvalDataGroupDetails.getDescription())
            );
        }
    }

    private void populateDataItemsDifference(ApprovalDataGroup approvalDataGroup, DataGroup dataGroup,
        PresentationDataGroupApprovalDetailsItem persistenceDataGroupApprovalDetailsItem) {
        LOGGER.info("Populating items for approval with id {} and action {}", approvalDataGroup.getApprovalId(),
            approvalDataGroup.getApprovalAction());

        Set<String> currentItemIds = dataGroup.getDataItemIds();

        Set<String> newItems;
        if (approvalDataGroup.getApprovalAction() == ApprovalAction.DELETE) {
            newItems = Collections.emptySet();
        } else {
            newItems = ((ApprovalDataGroupDetails) approvalDataGroup).getItems();
        }

        Set<String> addedItems = getSetsDifference(newItems, currentItemIds);
        Set<String> removedItems = getSetsDifference(currentItemIds, newItems);
        SetView<String> unmodifiedItems = Sets.intersection(currentItemIds, newItems);

        persistenceDataGroupApprovalDetailsItem
            .withAddedDataItems(addedItems)
            .withRemovedDataItems(removedItems)
            .withUnmodifiedDataItems(unmodifiedItems);
    }

    private void populateDetailsForApprovalCreate(ApprovalDataGroupDetails approvalDataGroupDetails,
        PresentationDataGroupApprovalDetailsItem persistenceDataGroupApprovalDetailsItem) {
        LOGGER.info("Populating details for create approval with id {} and action {}",
            approvalDataGroupDetails.getApprovalId(),
            approvalDataGroupDetails.getApprovalAction());
        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(
                approvalDataGroupDetails.getServiceAgreementId(),
                SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR)
            .orElseThrow(() -> {
                LOGGER.warn("Service Agreement with id {} does not exist",
                    approvalDataGroupDetails.getServiceAgreementId());
                return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        persistenceDataGroupApprovalDetailsItem
            .withServiceAgreementId(serviceAgreement.getId())
            .withServiceAgreementName(serviceAgreement.getName())
            .withType(approvalDataGroupDetails.getType())
            .withNewState(
                new PersistenceDataGroupState()
                    .withName(approvalDataGroupDetails.getName())
                    .withDescription(approvalDataGroupDetails.getDescription())
            )
            .withAddedDataItems(approvalDataGroupDetails.getItems());
        setLegalEntityIds(serviceAgreement, persistenceDataGroupApprovalDetailsItem);

    }

    private Set<String> getSetsDifference(Set<String> first, Set<String> second) {
        return Sets.difference(first, second).immutableCopy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> getBulkDataGroups(Collection<String> ids) {
        return dataGroupJpaRepository.findAllDataGroupsWithIdsIn(ids, DATA_GROUP_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<PersistenceDataGroupExtendedItemDto> getDataGroupsByExternalServiceAgreementIds(Collection<String> ids,
        boolean returnApprovalDataGroups) {

        List<PersistenceDataGroupExtendedItemDto> result = transformToPersistenceDataGroupExtendedItems(
            dataGroupJpaRepository
                .findAllDataGroupsWithExternalServiceAgreementIdsIn(ids, DATA_GROUP_EXTENDED));

        if (returnApprovalDataGroups) {

            Map<String, String> serviceAgreementMap = serviceAgreementJpaRepository.findAllByExternalIdIn(ids)
                .stream().collect(Collectors.toMap(ServiceAgreement::getId, ServiceAgreement::getExternalId));

            result.addAll(transformToPersistenceDataGroupExtendedItems(approvalDataGroupDetailsJpaRepository
                .findAllByServiceAgreementIdIn(serviceAgreementMap.keySet()), serviceAgreementMap));

        }

        return result;
    }

    @Override
    @Transactional
    public Boolean checkIfExistsPendingDataGroupByServiceAgreementId(String serviceAgreementId,
        boolean returnApprovalDataGroups) {
        boolean resultApproval = false;

        if (returnApprovalDataGroups) {

            resultApproval = approvalDataGroupDetailsJpaRepository.existsByServiceAgreementId(serviceAgreementId);
        }

        return resultApproval;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String save(com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupBase) {
        LOGGER.info("Saving data group {} ...", dataGroupBase);
        checkIfDataGroupNameAlreadyExists(dataGroupBase);
        checkIfServiceAgreementsIsInPendingState(dataGroupBase.getServiceAgreementId());
        ServiceAgreement serviceAgreement = getServiceAgreementWithCreator(dataGroupBase.getServiceAgreementId());
        validateCustomerTypeDataGroup(serviceAgreement, dataGroupBase.getType(), dataGroupBase.getItems());
        checkIfApprovalDataGroupAlreadyPending(dataGroupBase.getServiceAgreementId(),
            dataGroupBase.getName());
        DataGroup dataGroup = populateDataGroupDomain(dataGroupBase, serviceAgreement);
        dataGroup = dataGroupJpaRepository.save(dataGroup);
        addNewItemsToDataGroup(dataGroup.getId(), new HashSet<>(dataGroupBase.getItems()));
        return dataGroup.getId();
    }

    private ServiceAgreement getServiceAgreementWithCreator(String serviceAgreementId) {
        return serviceAgreementJpaRepository.findById(
            serviceAgreementId, GraphConstants.SERVICE_AGREEMENT_WITH_CREATOR)
            .orElseThrow(() -> {
                LOGGER.warn("Service agreement with id {} does not exists.", serviceAgreementId);
                return getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String saveDataGroupApproval(
        com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupApprovalCreate, String approvalId) {
        LOGGER.info("Saving data group with approval ON {} ...", dataGroupApprovalCreate);
        checkIfServiceAgreementExist(dataGroupApprovalCreate);
        checkIfDataGroupNameAlreadyExists(dataGroupApprovalCreate);
        checkIfServiceAgreementsIsInPendingState(dataGroupApprovalCreate.getServiceAgreementId());
        checkIfApprovalDataGroupAlreadyPending(dataGroupApprovalCreate.getServiceAgreementId(),
            dataGroupApprovalCreate.getName());
        ApprovalDataGroupDetails dataGroup = populateApprovalDataGroupDomain(dataGroupApprovalCreate,
            approvalId);
        approvalDataGroupDetailsJpaRepository.save(dataGroup);
        return approvalId;
    }

    private void checkIfServiceAgreementsIsInPendingState(String serviceAgreementId) {
        if (applicationProperties.getApproval().getValidation().isEnabled() &&
            approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)) {
            LOGGER
                .warn("Data group operation is not allowed, there is pending operation on service agreement with id {}",
                    serviceAgreementId);
            throw getBadRequestException(ERR_ACC_107.getErrorMessage(), ERR_ACC_107.getErrorCode());
        }
    }

    private void checkIfApprovalDataGroupAlreadyPending(String serviceAgreementId, String dataGroupName) {
        if (approvalDataGroupDetailsJpaRepository
            .existsByNameAndServiceAgreementId(dataGroupName, serviceAgreementId)) {
            LOGGER.warn(
                "There is pending creation of data group for service agreement with id {} with data group name {}",
                serviceAgreementId, dataGroupName);
            throw getBadRequestException(ERR_ACC_082.getErrorMessage(), ERR_ACC_082.getErrorCode());
        }
    }

    private void validateCustomerTypeDataGroup(ServiceAgreement serviceAgreement, String dataGroupType,
        Collection<String> items) {

        if (dataGroupType.equals(CUSTOMERS_DATA_GROUP_TYPE) && !serviceAgreement.isMaster()) {
            LOGGER.warn("Data group of type CUSTOMERS can not be add to CUSTOM service agreement with id {}",
                serviceAgreement.getId());
            throw getBadRequestException(ERR_ACC_097.getErrorMessage(), ERR_ACC_097.getErrorCode());
        }
        if (dataGroupType.equals(CUSTOMERS_DATA_GROUP_TYPE) && !items.isEmpty()
            && !getLegalEntityIdsInHierarchy(serviceAgreement, items).containsAll(items)) {

            LOGGER.warn("Data items of type CUSTOMERS are not in the hierarchy with the creator of the "
                + "service agreement with id {}", serviceAgreement.getId());
            throw getBadRequestException(ERR_ACC_099.getErrorMessage(), ERR_ACC_099.getErrorCode());
        }
    }

    private Set<String> getLegalEntityIdsInHierarchy(ServiceAgreement serviceAgreement, Collection<String> items) {
        Set<String> leIds = legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(serviceAgreement
            .getCreatorLegalEntity().getId(), items).stream()
            .map(IdProjection::getId)
            .collect(Collectors.toSet());

        if (items.contains(serviceAgreement.getCreatorLegalEntity().getId())) {
            leIds.add(serviceAgreement.getCreatorLegalEntity().getId());
        }
        return leIds;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(String dataGroupId, DataGroupByIdPutRequestBody dataGroupPutRequestBody) {
        LOGGER.info("Trying to update Data group with id {} and data {}", dataGroupId, dataGroupPutRequestBody);
        if (!dataGroupId.equals(dataGroupPutRequestBody.getId())) {
            LOGGER.warn("Invalid data group identifiers {}",
                dataGroupPutRequestBody.getId());
            throw getBadRequestException(ERR_ACC_051.getErrorMessage(),
                ERR_ACC_051.getErrorCode());
        }
        checkIfApprovalDataGroupAlreadyPending(dataGroupPutRequestBody.getServiceAgreementId(),
            dataGroupPutRequestBody.getName());
        DataGroup dataGroupDomain = getDataGroupIfExistsWithSACreator(
            new IdentifierDto().withIdIdentifier(dataGroupId), ERR_ACQ_001);
        if (!dataGroupDomain.getServiceAgreement().getId().equals(dataGroupPutRequestBody.getServiceAgreementId())) {
            LOGGER.warn("Data group with id {} can't be moved under other service agreement",
                dataGroupPutRequestBody.getId());
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_031.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_031.getErrorCode());
        }
        checkIfServiceAgreementsIsInPendingState(dataGroupDomain.getServiceAgreementId());
        updateDataGroupsByUpdatableFields(dataGroupPutRequestBody, dataGroupDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String update(PresentationSingleDataGroupPutRequestBody item) {
        DataGroup dataGroupDomain = getDataGroupIfExistsWithSACreator(
            mapper.presentationToIdentifierDto(item.getDataGroupIdentifier()), ERR_ACC_085);

        checkUniquenessOfDataGroupName(item.getName(), dataGroupDomain.getServiceAgreement().getId(),
            dataGroupDomain.getId());
        checkIfServiceAgreementsIsInPendingState(dataGroupDomain.getServiceAgreementId());

        List<String> newItems = item.getDataItems().stream()
            .map(PresentationItemIdentifier::getInternalIdIdentifier)
            .collect(Collectors.toList());

        validateCustomerTypeDataGroup(dataGroupDomain.getServiceAgreement(), item.getType(), newItems);

        dataGroupDomain.setName(item.getName());
        dataGroupDomain.setDescription(item.getDescription());
        dataGroupDomain.setDataItemType(item.getType());
        DataGroup savedDataGroup = dataGroupJpaRepository.save(dataGroupDomain);

        updateItems(newItems, dataGroupDomain.getId());
        return savedDataGroup.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateDataGroupApproval(DataGroupByIdPutRequestBody dataGroupApprovalUpdate) {
        String dataGroupId = dataGroupApprovalUpdate.getId();
        LOGGER.info("Trying to update Data group with id {} and data {}", dataGroupId,
            dataGroupApprovalUpdate);

        DataGroup dataGroupDomain = getById(dataGroupId, DATA_GROUP_SERVICE_AGREEMENT);
        if (!dataGroupDomain.getServiceAgreement().getId().equals(dataGroupApprovalUpdate.getServiceAgreementId())) {
            LOGGER.warn("Data group with id {} can't be moved under other service agreement",
                dataGroupId);
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_031.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_031.getErrorCode());
        }
        checkIfServiceAgreementsIsInPendingState(dataGroupDomain.getServiceAgreementId());

        checkUniquenessOfDataGroupName(dataGroupApprovalUpdate.getName(),
            dataGroupApprovalUpdate.getServiceAgreementId(),
            dataGroupId);

        checkIfPendingActionForDataGroup(dataGroupApprovalUpdate.getId());

        ApprovalDataGroupDetails approvalDetails = mapper
            .approvalDataGroupDetailsToApprovalDataGroupDetails(dataGroupApprovalUpdate);

        approvalDataGroupDetailsJpaRepository.save(approvalDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteDataGroupApproval(String dataGroupId, String approvalId) {
        LOGGER.info("Trying to delete Data group with approval ON and id {}", dataGroupId);
        DataGroup dataGroup = getDataGroupIfExists(dataGroupId);
        checkIfServiceAgreementsIsInPendingState(dataGroup.getServiceAgreementId());
        checkIfDataGroupIsAssignedToUser(dataGroupId);
        verifyPendingAssignmentsForDataGroup(dataGroupId);
        checkIfPendingActionForDataGroup(dataGroupId);
        ApprovalDataGroup approvalDataGroup = new ApprovalDataGroup();
        approvalDataGroup.setDataGroupId(dataGroupId);
        approvalDataGroup.setApprovalId(approvalId);
        approvalDataGroupJpaRepository.save(approvalDataGroup);
    }

    public DataGroup getDataGroupIfExists(String dataGroupId) {
        return dataGroupJpaRepository.findById(dataGroupId).orElseThrow(() -> {
            LOGGER.warn("Data group with id: {} doesn't exist", dataGroupId);
            return getNotFoundException(ERR_ACC_015.getErrorMessage(), ERR_ACC_015.getErrorCode());
        });
    }

    private DataGroup getDataGroupIfExists(IdentifierDto identifier, CommandErrorCodes commandErrorCode) {
        return getDataGroup(identifier, commandErrorCode, null);
    }

    private DataGroup getDataGroup(IdentifierDto identifier, ErrorCode commandErrorCode, String entityGraph) {
        return Optional
            .ofNullable(identifier.getIdIdentifier())
            .map(id -> isNull(entityGraph) ? dataGroupJpaRepository.findById(id)
                : dataGroupJpaRepository.findById(id, entityGraph))
            .orElseGet(() -> getByNameIdentifier(identifier.getNameIdentifier(), entityGraph))
            .orElseThrow(() -> getNotFoundException(
                commandErrorCode.getErrorMessage(),
                commandErrorCode.getErrorCode()));
    }

    private DataGroup getDataGroupIfExistsWithSACreator(IdentifierDto identifier,
        ErrorCode commandErrorCode) {
        return getDataGroup(identifier, commandErrorCode, DATA_GROUP_WITH_SA_CREATOR);
    }

    private void checkIfDataGroupIsAssignedToUser(String dataGroupId) {
        if (userAssignedCombinationRepository.existsByDataGroupIdsIn(Sets.newHashSet(dataGroupId))) {
            LOGGER.warn("Data group with id:{} is assigned to users", dataGroupId);
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_013.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_013.getErrorCode());
        }
    }

    private void checkIfPendingActionForDataGroup(String dataGroupId) {
        if (approvalDataGroupJpaRepository.existsByDataGroupId(dataGroupId)) {
            LOGGER.warn(
                "There is pending update or delete of data group with id {}", dataGroupId);
            throw getBadRequestException(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode());
        }
    }

    private void updateDataGroupsByUpdatableFields(DataGroupByIdPutRequestBody dataGroupPutRequestBody,
        DataGroup dataGroupDomain) {
        checkUniquenessOfDataGroupName(dataGroupPutRequestBody.getName(), dataGroupDomain.getServiceAgreement().getId(),
            dataGroupDomain.getId());

        dataGroupDomain.setName(dataGroupPutRequestBody.getName());
        dataGroupDomain.setDescription(dataGroupPutRequestBody.getDescription());
        dataGroupDomain.setDataItemType(dataGroupPutRequestBody.getType());
        dataGroupJpaRepository.save(dataGroupDomain);

        updateItems(dataGroupPutRequestBody.getItems(), dataGroupDomain.getId());
    }

    private ApprovalDataGroupDetails populateApprovalDataGroupDomain(
        com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupApprovalUpdate,
        String approvalId) {
        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setName(dataGroupApprovalUpdate.getName());
        approvalDetails.setDescription(dataGroupApprovalUpdate.getDescription());
        approvalDetails.setServiceAgreementId(dataGroupApprovalUpdate.getServiceAgreementId());
        approvalDetails.setType(dataGroupApprovalUpdate.getType());
        approvalDetails.setApprovalId(approvalId);
        if (!dataGroupApprovalUpdate.getItems().isEmpty()) {
            Set<String> items = new HashSet<>(dataGroupApprovalUpdate.getItems());
            approvalDetails.setItems(items);
        }

        return approvalDetails;
    }


    private List<PersistenceDataGroupExtendedItemDto> transformToPersistenceDataGroupExtendedItems(
        List<DataGroup> dataGroups) {
        return dataGroups.stream()
            .map(this::transformPersistenceDataGroupExtendedItem)
            .collect(Collectors.toList());
    }

    private List<PersistenceDataGroupExtendedItemDto> transformToPersistenceDataGroupExtendedItems(
        List<ApprovalDataGroupDetails> dataGroups, Map<String, String> serviceAgreementMap) {
        return dataGroups.stream()
            .map(dg -> transformPersistenceDataGroupExtendedItem(dg, serviceAgreementMap))
            .collect(Collectors.toList());
    }

    private PersistenceDataGroupExtendedItemDto transformPersistenceDataGroupExtendedItem(DataGroup dataGroup) {
        PersistenceDataGroupExtendedItemDto dto = new PersistenceDataGroupExtendedItemDto();
        dto.setId(dataGroup.getId());
        dto.setDescription(dataGroup.getDescription());
        dto.setExternalServiceAgreementId(dataGroup.getServiceAgreement().getExternalId());
        dto.setName(dataGroup.getName());
        dto.setItems(new ArrayList<>(dataGroup.getDataItemIds()));
        dto.setType(dataGroup.getDataItemType());
        return dto;
    }

    private PersistenceDataGroupExtendedItemDto transformPersistenceDataGroupExtendedItem(
        ApprovalDataGroupDetails dataGroup, Map<String, String> serviceAgreementMap) {
        PersistenceDataGroupExtendedItemDto dto = new PersistenceDataGroupExtendedItemDto();
        dto.setId(dataGroup.getDataGroupId());
        dto.setDescription(dataGroup.getDescription());
        dto.setExternalServiceAgreementId(serviceAgreementMap.get(dataGroup.getServiceAgreementId()));
        dto.setName(dataGroup.getName());
        dto.setItems(new ArrayList<>(dataGroup.getItems()));
        dto.setType(dataGroup.getType());
        return dto;
    }

    private Optional<DataGroup> getByNameIdentifier(
        NameIdentifier nameIdentifier,
        String entityGraph) {
        return Optional.ofNullable(nameIdentifier)
            .flatMap(identifier -> dataGroupJpaRepository
                .findByServiceAgreementExternalIdAndName(identifier.getExternalServiceAgreementId(),
                    identifier.getName(), entityGraph)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(String dataGroupId) {
        LOGGER.info("Trying to delete Data group with id {}", dataGroupId);

        checkIfDataGroupIsAssignedToUser(dataGroupId);
        verifyPendingAssignmentsForDataGroup(dataGroupId);
        checkIfPendingActionForDataGroup(dataGroupId);
        DataGroup dataGroup = getDataGroupIfExists(dataGroupId);
        checkIfServiceAgreementsIsInPendingState(dataGroup.getServiceAgreementId());

        dataGroupItemJpaRepository.deleteAllByDataGroupId(dataGroupId);
        dataGroupJpaRepository.deleteById(dataGroupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<DataGroupItemBase> getByServiceAgreementIdAndDataItemType(String serviceAgreementId, String type,
        boolean includeItems) {
        LOGGER.info("Retrieving Data groups with service agreement id {} and type {}", serviceAgreementId, type);
        List<DataGroup> dataGroups;
        String graphName = null;
        if (includeItems) {
            graphName = DATA_GROUP_EXTENDED;
        }
        if (type == null) {
            dataGroups = dataGroupJpaRepository.findByServiceAgreementId(serviceAgreementId, graphName);
        } else if (validationConfig.getTypes().contains(type)) {
            dataGroups = dataGroupJpaRepository.findByServiceAgreementIdAndDataItemType(
                serviceAgreementId,
                type, graphName);
        } else {
            LOGGER
                .warn("Invalid or duplicate identifiers for function/data group for sa with id {}", serviceAgreementId);
            throw getBadRequestException(ERR_ACC_050.getErrorMessage(), ERR_ACC_050.getErrorCode());
        }
        Map<String, String> dataGroupsWithApprovals = approvalDataGroupJpaRepository.findByDataGroupIdIn(
            dataGroups.stream().map(DataGroup::getId).collect(Collectors.toSet()))
            .stream().collect(Collectors.toMap(ApprovalDataGroup::getDataGroupId, ApprovalDataGroup::getApprovalId));
        return dataGroups.stream()
            .map(dataGroup -> convertDataGroupToDataGroupItemBase(dataGroup,
                dataGroupsWithApprovals.get(dataGroup.getId()), includeItems))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveDataGroupIdFromIdentifier(PresentationIdentifier dataGroupIdentifier) {
        LOGGER.info("Trying to retrieve data group id from identifier {}", dataGroupIdentifier);
        return Optional.ofNullable(dataGroupIdentifier.getIdIdentifier())
            .orElseGet(
                () -> getDataGroupIdByNameAndExternalServiceAgreementId(dataGroupIdentifier.getNameIdentifier()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String updateDataGroupItemsByIdIdentifier(PresentationDataGroupItemPutRequestBody item) {
        DataGroup dataGroup;
        if (item.getAction().equals(PresentationAction.ADD)) {
            dataGroup = getDataGroupIfExistsWithSACreator(
                mapper.presentationToIdentifierDto(item.getDataGroupIdentifier()), ERR_ACC_051);
        } else {
            dataGroup = getDataGroupIfExists(mapper.presentationToIdentifierDto(item.getDataGroupIdentifier()),
                ERR_ACC_051);
        }
        validateTypeOfDataGroup(dataGroup.getDataItemType(), item.getType());
        checkIfServiceAgreementsIsInPendingState(dataGroup.getServiceAgreementId());
        if (item.getAction().equals(PresentationAction.ADD)) {
            addDataItemsToDataGroup(item, dataGroup);
        } else {
            removeDataItemsFromDataGroup(item, dataGroup.getId());
        }
        return dataGroup.getId();
    }

    private void validateTypeOfDataGroup(String dataItemType, String type) {
        if (!StringUtils.equals(dataItemType, type)) {
            throw getBadRequestException(ERR_ACC_053.getErrorMessage(), ERR_ACC_053.getErrorCode());
        }
    }

    private void addDataItemsToDataGroup(PresentationDataGroupItemPutRequestBody item, DataGroup dataGroup) {
        Set<String> dataItemsIds = getDataItemsIds(item.getDataItems());
        validateCustomerTypeDataGroup(dataGroup.getServiceAgreement(), dataGroup.getDataItemType(), dataItemsIds);
        validateIfDataItemAlreadyExistInDataGroup(dataGroup.getId(), dataItemsIds);
        addNewItemsToDataGroup(dataGroup.getId(), dataItemsIds);
    }

    private void validateIfDataItemAlreadyExistInDataGroup(String dataGroupId, Set<String> dataItemsToValidate) {
        if (dataGroupItemJpaRepository.existsByDataGroupIdAndDataItemIdIn(dataGroupId, dataItemsToValidate)) {
            LOGGER.warn("Data items: {} exists in data group with id: {}", dataItemsToValidate, dataGroupId);
            throw getBadRequestException(ERR_ACC_081.getErrorMessage(), ERR_ACC_081.getErrorCode());
        }
    }

    private Set<String> getDataItemsIds(List<PresentationItemIdentifier> dataItems) {
        return dataItems.stream().flatMap(item -> Arrays.stream(item.getInternalIdIdentifier().split(",")))
            .collect(Collectors.toSet());
    }

    private void removeDataItemsFromDataGroup(PresentationDataGroupItemPutRequestBody item,
        String dataGroupId) {
        Set<String> dataItemsIds = getDataItemsIds(item.getDataItems());
        validateIfAllItemsExist(dataItemsIds, dataGroupId);
        if (!dataItemsIds.isEmpty()) {
            dataGroupItemJpaRepository.deleteAllByDataGroupIdAndItemIdIn(dataGroupId, dataItemsIds);
        }
    }

    private void validateIfAllItemsExist(Set<String> dataItems, String dataGroupId) {
        List<DataGroupItem> dataGroupItems = dataGroupItemJpaRepository
            .findByDataGroupIdAndDataItemIdIn(dataGroupId, dataItems);

        if (dataGroupItems.size() != dataItems.size()) {
            throw getBadRequestException(ERR_ACC_079.getErrorMessage(), ERR_ACC_079.getErrorCode());
        }
    }


    private String getDataGroupIdByNameAndExternalServiceAgreementId(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier dataGroupIdentifier) {
        LOGGER.info("Trying to retrieve data group id by name {} and external service agreement id {}",
            dataGroupIdentifier.getName(), dataGroupIdentifier.getExternalServiceAgreementId());

        Optional<DataGroup> dataGroup = dataGroupJpaRepository
            .findByServiceAgreementExternalIdAndName(dataGroupIdentifier.getExternalServiceAgreementId(),
                dataGroupIdentifier.getName(), null);
        return dataGroup
            .map(DataGroup::getId)
            .orElseThrow(() -> {
                LOGGER.warn("Identifier: {} is invalid", dataGroupIdentifier);
                return getBadRequestException(ERR_ACC_051.getErrorMessage(), ERR_ACC_051.getErrorCode());
            });
    }

    private void updateItems(List<String> items, String dataGroupId) {
        dataGroupItemJpaRepository.deleteAllByDataGroupId(dataGroupId);
        addNewItemsToDataGroup(dataGroupId, new HashSet<>(items));
    }

    private DataGroup populateDataGroupDomain(
        com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupBase,
        ServiceAgreement serviceAgreement) {
        DataGroup dataGroup = new DataGroup();
        dataGroup.setName(dataGroupBase.getName());
        dataGroup.setDescription(dataGroupBase.getDescription());
        dataGroup.setDataItemType(dataGroupBase.getType());
        dataGroup.setServiceAgreement(serviceAgreement);
        return dataGroup;
    }

    private void addNewItemsToDataGroup(String dataGroupId, Set<String> items) {

        if (!items.isEmpty()) {
            List<DataGroupItem> existingItems = dataGroupItemJpaRepository.findAllByDataGroupId(dataGroupId);

            Set<String> newItems = Sets.difference(items, existingItems.stream()
                .map(DataGroupItem::getDataItemId).collect(Collectors.toSet()));

            existingItems.addAll(newItems.stream().map(itemId -> new DataGroupItem()
                .withDataItemId(itemId)
                .withDataGroupId(dataGroupId))
                .collect(Collectors.toSet()));

            dataGroupItemJpaRepository.saveAll(existingItems);
        }
    }

    private void checkIfDataGroupNameAlreadyExists(
        com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupBase) {
        LOGGER.info(
            "Check if data group with name {}, service agreement id {} and data item type {} already exists.",
            dataGroupBase.getName(), dataGroupBase.getServiceAgreementId(), dataGroupBase.getType());
        List<DataGroup> dataGroups = dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            dataGroupBase.getName(),
            dataGroupBase.getServiceAgreementId()
        );
        if (!dataGroups.isEmpty()) {
            LOGGER.warn("Data group with name {} already exists.", dataGroupBase.getName());
            throw getBadRequestException(ERR_ACC_028.getErrorMessage(),
                ERR_ACC_028.getErrorCode());

        }
    }

    private void checkUniquenessOfDataGroupName(String name, String serviceAgreementId,
        String id) {
        boolean unique = dataGroupJpaRepository.findDistinctByNameAndServiceAgreementIdAndIdNot(
            name,
            serviceAgreementId,
            id
        ).isEmpty();
        if (!unique) {
            LOGGER.warn("Data group name {} already exists.", name);
            throw getBadRequestException(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode());
        }
        LOGGER.info("Unique in master table is {}", unique);
        boolean existsInPendingTable = !approvalDataGroupDetailsJpaRepository
            .findByNameAndServiceAgreementId(
                name,
                serviceAgreementId
            ).stream().filter(item -> !id.equals(item.getDataGroupId())).collect(Collectors.toSet()).isEmpty();
        LOGGER.info("existsInPendingTable in approvalDataGroupDetailsJpaRepository is {}", existsInPendingTable);
        if (existsInPendingTable) {
            LOGGER.warn("Data group name {} already exists in pending table.", name);
            throw getBadRequestException(ERR_ACC_083.getErrorMessage(), ERR_ACC_083.getErrorCode());
        }
    }

    private void checkIfServiceAgreementExist(
        com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase dataGroupBase) {
        LOGGER.info("Check if service agreement with id {} exists.", dataGroupBase.getServiceAgreementId());
        if (!serviceAgreementJpaRepository.existsById(dataGroupBase.getServiceAgreementId())) {
            LOGGER.warn("Service agreement with id {} does not exists.", dataGroupBase.getServiceAgreementId());
            throw getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());
        }
    }

    private DataGroupItemBase convertDataGroupToDataGroupItemBase(DataGroup dataGroup, String approvalId,
        boolean includeItems) {
        return new DataGroupItemBase()
            .withId(dataGroup.getId())
            .withName(dataGroup.getName())
            .withDescription(dataGroup.getDescription())
            .withServiceAgreementId(dataGroup.getServiceAgreementId())
            .withType(dataGroup.getDataItemType())
            .withApprovalId(approvalId)
            .withItems(DataGroupHandlerUtil.getDataGroupItemsIds(dataGroup, includeItems));
    }

    private void verifyPendingAssignmentsForDataGroup(String dataGroupId) {
        if (approvalUserContextAssignFunctionGroupJpaRepository.existsByDataGroups(dataGroupId)) {
            LOGGER.warn("Data group with id:{} can not be deleted, there are pending assignments",
                dataGroupId);
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_074.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_074.getErrorCode());
        }
    }
}
