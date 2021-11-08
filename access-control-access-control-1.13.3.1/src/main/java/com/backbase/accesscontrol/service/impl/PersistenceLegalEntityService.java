package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.DomainConstants.CUSTOMERS_DATA_GROUP_TYPE;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_004;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_007;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_008;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_009;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_010;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_037;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_043;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_098;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_005;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_035;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_050;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_051;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_052;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_053;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_055;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_064;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.EntityIds;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.LegalEntityToSegmentationBodyMapper;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.IdProjection;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.impl.strategy.legalentity.LegalEntityStrategyContext;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ExistingCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class PersistenceLegalEntityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceLegalEntityService.class);
    private static final String MASTER_SERVICE_AGREEMENT = "Master Service Agreement";
    private static final String LEGAL_ENTITY_WITH_EXTERNAL_ID_DOES_NOT_EXIST =
        "Legal entity with external id {} does not exist";
    private static final String ADD_LEGAL_ENTITY_FROM_COMMAND = "Trying to add Legal Entity from command {}";

    private LegalEntityJpaRepository legalEntityJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private LegalEntityStrategyContext legalEntityStrategyContext;
    private DataGroupJpaRepository dataGroupJpaRepository;
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private LegalEntityToSegmentationBodyMapper legalEntityToSegmentationBodyMapper;
    private BusinessFunctionCache businessFunctionCache;
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    /**
     * Creates new Legal Entity from  {@link LegalEntitiesPostRequestBody} and saves it to a relational database. Sets
     * the parent legal entity if it is required and it exists
     *
     * @param requestBody requestBody
     * @return Created legal entity
     */
    @Transactional
    public LegalEntity addLegalEntity(LegalEntitiesPostRequestBody requestBody) {
        LOGGER.info(ADD_LEGAL_ENTITY_FROM_COMMAND, requestBody);

        if (legalEntityJpaRepository.existsByExternalId(requestBody.getExternalId())) {
            throw getBadRequestException(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode());
        }

        Optional<LegalEntity> parentLegalEntity = getLegalEntityByParentId(requestBody.getParentExternalId());

        validateLegalEntity(parentLegalEntity, requestBody.getType().toString(), requestBody.getParentExternalId());

        LegalEntity legalEntityToSave = populateLegalEntityDomain(
            requestBody.getExternalId(),
            requestBody.getName(),
            requestBody.getAdditions(),
            LegalEntityType.fromString(requestBody.getType().toString()),
            parentLegalEntity.orElse(null)
        );
        LegalEntity createdEntity = legalEntityJpaRepository.save(legalEntityToSave);

        ServiceAgreement masterServiceAgreement = createMasterServiceAgreement(createdEntity,
            requestBody.getActivateSingleServiceAgreement());
        serviceAgreementJpaRepository.save(masterServiceAgreement);
        return createdEntity;

    }

    /**
     * Creates new Legal Entity from  {@link CreateLegalEntitiesPostRequestBody} and saves it to a relational database.
     * Sets the parent legal entity if it is required and it exists
     *
     * @param requestBody requestBody
     * @return Created legal entity
     */
    @Transactional
    public LegalEntity createLegalEntity(CreateLegalEntitiesPostRequestBody requestBody) {
        LOGGER.info(ADD_LEGAL_ENTITY_FROM_COMMAND, requestBody);

        if (legalEntityJpaRepository.existsByExternalId(requestBody.getExternalId())) {
            throw getBadRequestException(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode());
        }

        Optional<LegalEntity> parentLegalEntity = getLegalEntityByParentId(requestBody.getParentExternalId());

        validateLegalEntity(parentLegalEntity, requestBody.getType().toString(), requestBody.getParentExternalId());

        LegalEntity legalEntityToSave = populateLegalEntityDomain(
            requestBody.getExternalId(),
            requestBody.getName(),
            requestBody.getAdditions(),
            LegalEntityType.fromString(requestBody.getType().toString()), parentLegalEntity.orElse(null)
        );
        return legalEntityJpaRepository.save(legalEntityToSave);
    }

    /**
     * Creates new Legal Entity from  {@link PresentationCreateLegalEntityItemPostRequestBody} and saves it to a
     * relational database. Sets the parent legal entity if it is required and it exists
     *
     * @param requestBody requestBody
     * @return Created legal entity
     */
    @Transactional
    public LegalEntity createLegalEntityWithInternalParentId(PresentationCreateLegalEntityItemPostRequestBody
        requestBody) {
        LOGGER.info(ADD_LEGAL_ENTITY_FROM_COMMAND, requestBody);

        if (legalEntityJpaRepository.existsByExternalId(requestBody.getExternalId())) {
            throw getBadRequestException(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode());
        }

        Optional<LegalEntity> parentLegalEntity = getLegalEntityByInternalParentId(requestBody.getParentInternalId());

        validateLegalEntity(parentLegalEntity, requestBody.getType().toString(), requestBody.getParentInternalId());

        LegalEntity legalEntityToSave = populateLegalEntityDomain(
            requestBody.getExternalId(),
            requestBody.getName(),
            requestBody.getAdditions(),
            LegalEntityType.fromString(requestBody.getType().toString()),
            parentLegalEntity.orElse(null)
        );

        LegalEntity createdEntity = legalEntityJpaRepository.save(legalEntityToSave);

        if (requestBody.getActivateSingleServiceAgreement()) {
            ServiceAgreement masterServiceAgreement = createMasterServiceAgreement(createdEntity,
                requestBody.getActivateSingleServiceAgreement());
            serviceAgreementJpaRepository.save(masterServiceAgreement);
        }
        return createdEntity;
    }

    @Transactional
    public LegalEntityAsParticipantPostResponseBody createLegalEntityAsParticipant(
        LegalEntityAsParticipantPostRequestBody requestBody,
        String creatorLegalEntityId) {
        LOGGER.info(ADD_LEGAL_ENTITY_FROM_COMMAND, requestBody);

        if (legalEntityJpaRepository.existsByExternalId(requestBody.getLegalEntityExternalId())) {
            LOGGER.warn("Legal Entity with given external Id already exists");
            throw getBadRequestException(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode());
        }

        Optional<LegalEntity> parentLegalEntity = getLegalEntityByInternalParentId(
            requestBody.getLegalEntityParentId());

        validateLegalEntity(parentLegalEntity, requestBody.getLegalEntityType().toString(),
            requestBody.getLegalEntityParentId());

        LegalEntity legalEntityToSave = populateLegalEntityDomain(
            requestBody.getLegalEntityExternalId(),
            requestBody.getLegalEntityName(),
            requestBody.getAdditions(),
            LegalEntityType.fromString(requestBody.getLegalEntityType().toString()),
            parentLegalEntity.orElse(null)
        );

        LegalEntity createdEntity = legalEntityJpaRepository.save(legalEntityToSave);

        LegalEntityAsParticipantPostResponseBody response =
            new LegalEntityAsParticipantPostResponseBody().withLegalEntityId(createdEntity.getId());

        if (requestBody.getParticipantOf().getExistingCustomServiceAgreement() != null) {
            ExistingCustomServiceAgreement existingCsa = requestBody.getParticipantOf()
                .getExistingCustomServiceAgreement();
            persistenceServiceAgreementService.addParticipant(existingCsa, createdEntity.getExternalId());

        } else if (requestBody.getParticipantOf().getNewCustomServiceAgreement() != null) {
            if (!requestBody
                .getParticipantOf().getNewCustomServiceAgreement().getParticipantInfo().getShareAccounts()
                || !requestBody.getParticipantOf().getNewCustomServiceAgreement().getParticipantInfo()
                .getShareUsers()) {
                LOGGER.warn("LE Participant must share Users and/or Accounts");
                throw getBadRequestException(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode());
            }
            ServiceAgreement createdAgreement = persistenceServiceAgreementService.save(createdEntity,
                requestBody.getParticipantOf().getNewCustomServiceAgreement(), creatorLegalEntityId);
            response.setServiceAgreementId(createdAgreement.getId());

        } else if (requestBody.getParticipantOf().getNewMasterServiceAgreement() != null) {
            ServiceAgreement createdAgreement = persistenceServiceAgreementService.save(createdEntity,
                requestBody.getParticipantOf().getNewMasterServiceAgreement());
            response.setServiceAgreementId(createdAgreement.getId());
        }

        return response;
    }

    private void validateLegalEntity(Optional<LegalEntity> parentLegalEntity,
        String type,
        String parentId) {
        final boolean equalsBank = LegalEntityType.BANK.toString().equals(type);
        if (parentLegalEntity.isPresent() && equalsBank
            && !LegalEntityType.BANK.equals(parentLegalEntity.get().getType())) {
            throw getBadRequestException(ERR_ACC_008.getErrorMessage(), ERR_ACC_008.getErrorCode());
        }
        if (Objects.isNull(parentId) && !equalsBank) {
            throw getBadRequestException(ERR_ACC_009.getErrorMessage(), ERR_ACC_009.getErrorCode());
        }
    }

    private Optional<LegalEntity> getLegalEntityByInternalParentId(String parentInternalId) {
        Optional<LegalEntity> parentLegalEntity = Optional.empty();
        BadRequestException badRequestException = getBadRequestException(ERR_ACC_007.getErrorMessage(),
            ERR_ACC_007.getErrorCode());

        if (Objects.isNull(parentInternalId)) {
            List<LegalEntity> rootLegalEntity = legalEntityJpaRepository.findDistinctByParentIsNull();
            if (!rootLegalEntity.isEmpty()) {
                throw badRequestException;
            }
        } else {
            parentLegalEntity = legalEntityJpaRepository
                .findById(parentInternalId, GRAPH_LEGAL_ENTITY_WITH_ANCESTORS);
            if (!parentLegalEntity.isPresent()) {
                throw badRequestException;
            }
        }
        return parentLegalEntity;
    }

    private Optional<LegalEntity> getLegalEntityByParentId(String parentExternalId) {
        Optional<LegalEntity> parentLegalEntity = Optional.empty();
        BadRequestException badRequestException = getBadRequestException(ERR_ACC_007.getErrorMessage(),
            ERR_ACC_007.getErrorCode());

        if (parentExternalId == null) {

            LOGGER.info("Checking if there is root le");
            boolean checkParent = legalEntityJpaRepository.existsByParentIsNull();
            if (checkParent) {
                throw badRequestException;
            }
        } else {
            parentLegalEntity = legalEntityJpaRepository.findByExternalId(parentExternalId);
            if (!parentLegalEntity.isPresent()) {
                throw badRequestException;
            }
        }
        return parentLegalEntity;
    }

    /**
     * Returns a list of all legal entities by their parent.
     *
     * @param parentEntityId - id of the parent legal entity id
     * @return list of {@link LegalEntity}
     */
    @Transactional(readOnly = true)
    public List<LegalEntity> getLegalEntities(String parentEntityId) {
        LOGGER.info("Trying to get Legal Entities with parent entity id {}", parentEntityId);
        if (parentEntityId == null) {
            return legalEntityJpaRepository
                .findDistinctByParentIsNull(GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT);
        } else {
            return legalEntityJpaRepository.findDistinctByParentId(parentEntityId);
        }
    }

    /**
     * Return legal entity or throws {@link NotFoundException} with {@link QueryErrorCodes#ERR_ACQ_005}.
     *
     * @param id of the legal entity;
     * @return {@link LegalEntity}
     */
    @Transactional(readOnly = true)
    public LegalEntity getLegalEntityById(String id) {
        LOGGER.info("Trying to get Legal Entity with id {}", id);
        LegalEntity legalEntity = legalEntityJpaRepository.findById(id, GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)
            .orElseThrow(() -> {
                LOGGER.warn("Legal entity with id {} does not exist", id);
                return getNotFoundException(ERR_ACQ_005.getErrorMessage(),
                    ERR_ACQ_005.getErrorCode());
            });

        legalEntity.setChildren(
            new HashSet<>(
                legalEntityJpaRepository.findByParentId(
                    legalEntity.getId(),
                    PageRequest.of(0, 1)
                )
            )
        );

        return legalEntity;
    }

    /**
     * Retrieves Legal Entity by External ID.
     *
     * @param externalId external id of the legal entity;
     * @return fetched legal entity
     */
    @Transactional(readOnly = true)
    public LegalEntity getLegalEntityByExternalId(String externalId, Boolean includeAdditions) {
        LOGGER.info("Trying to get Legal Entity with external id {}", externalId);

        String entityGraph = null;
        if (includeAdditions) {
            entityGraph = GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;
        }

        Optional<LegalEntity> legalEntity = legalEntityJpaRepository.findByExternalId(externalId, entityGraph);
        return legalEntity.orElseThrow(() -> {
            LOGGER.warn(LEGAL_ENTITY_WITH_EXTERNAL_ID_DOES_NOT_EXIST, externalId);
            return getNotFoundException(ERR_ACQ_005.getErrorMessage(),
                ERR_ACQ_005.getErrorCode());
        });
    }

    /**
     * Retrieves Legal Entity parents External ID.
     *
     * @param childLegalEntity id of the child
     * @return List of {@link LegalEntity}
     */
    @Transactional(readOnly = true)
    public List<LegalEntity> getParents(String childLegalEntity) {
        LOGGER.info("Trying to get parents for Legal Entity with id {}", childLegalEntity);
        Optional<LegalEntity> one = legalEntityJpaRepository
            .findById(childLegalEntity, GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS);
        LegalEntity legalEntity = one.orElseThrow(() -> {
            LOGGER.warn("Legal entity with id {} does not exist", childLegalEntity);
            return getNotFoundException(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode());
        });
        if (legalEntity.getParent() == null) {
            throw getInternalServerErrorException(QueryErrorCodes.ERR_ACQ_009.getErrorMessage());
        }
        return legalEntity.getLegalEntityAncestors();
    }

    /**
     * Retrieves the root legal entity.
     *
     * @return {@link LegalEntity}
     */
    @Transactional(readOnly = true)
    public LegalEntity getRootLegalEntity() {
        List<LegalEntity> legalEntities = legalEntityJpaRepository.findDistinctByParentIsNull();
        return validateRootLegalEntity(legalEntities);
    }

    /**
     * Returns the master service agreement by the legal entity.
     *
     * @param id - id of the legal entity
     * @return {@link LegalEntity}
     */
    @Transactional(readOnly = true)
    public ServiceAgreement getMasterServiceAgreement(String id) {
        LOGGER.info("Trying to get master service agreement for legal entity {}", id);
        LegalEntity legalEntity = getLegalEntityById(id);
        return getServiceAgreementByLeId(legalEntity.getId());
    }

    /**
     * Retrieves master service agreement by external id.
     *
     * @param externalId - external id
     */
    @Transactional(readOnly = true)
    public ServiceAgreement getMasterServiceAgreementByExternalId(String externalId) {
        LOGGER.info("Trying to get master service agreement for external legal entity {}", externalId);
        LegalEntity legalEntity = legalEntityJpaRepository.findByExternalId(externalId)
            .orElseThrow(() -> {
                LOGGER.warn(LEGAL_ENTITY_WITH_EXTERNAL_ID_DOES_NOT_EXIST, externalId);
                return getNotFoundException(ERR_ACQ_005.getErrorMessage(),
                    ERR_ACQ_005.getErrorCode());
            });
        return getServiceAgreementByLeId(legalEntity.getId());
    }

    protected ServiceAgreement getServiceAgreementByLeId(String legalEntityId) {
        return serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(legalEntityId, true, SERVICE_AGREEMENT_WITH_ADDITIONS)
            .orElseThrow(() -> {
                LOGGER.warn(ERR_ACQ_005.getErrorMessage());
                return getNotFoundException(QueryErrorCodes.ERR_ACQ_006.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_006.getErrorCode());
            });
    }

    /**
     * Retrieves all children of Legal Entity ID.
     *
     * @param legalEntityId                 - id of the Legal Entity
     * @param searchAndPaginationParameters - search and pagination parameters
     * @return fetched sub entities
     */
    @Transactional(readOnly = true)
    public Page<LegalEntity> getSubEntities(String legalEntityId,
        SearchAndPaginationParameters searchAndPaginationParameters, Collection<String> excludeIds) {
        return legalEntityJpaRepository.findAllSubEntities(legalEntityId,
            searchAndPaginationParameters, excludeIds, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
    }

    /**
     * Retrieves sub legal entities for legal entity id.
     *
     * @param legalEntityId - legal entity id
     * @return sub legal entities for legal entity i
     */
    @Transactional
    public List<String> getListOfAllSubEntityIds(String legalEntityId) {
        List<String> legalEntitiesId = new ArrayList<>();

        Optional<LegalEntity> legalEntity = legalEntityJpaRepository.findById(legalEntityId);
        legalEntity.ifPresent(entity -> legalEntitiesId.add(entity.getId()));
        legalEntitiesId.addAll(
            legalEntityJpaRepository.findByLegalEntityAncestorsId(
                legalEntityId
            )
                .stream()
                .map(IdProjection::getId)
                .collect(Collectors.toList()
                )
        );
        return legalEntitiesId;
    }

    private ServiceAgreement createMasterServiceAgreement(LegalEntity legalEntity,
        boolean enabledMasterServiceAgreement) {
        LOGGER.info("Trying to create Master Service Agreement for created Legal Entity {}", legalEntity);

        ServiceAgreement masterServiceAgreement = new ServiceAgreement();
        masterServiceAgreement.setCreatorLegalEntity(legalEntity);
        masterServiceAgreement.setName(legalEntity.getName());
        masterServiceAgreement.setDescription(MASTER_SERVICE_AGREEMENT);
        masterServiceAgreement.setMaster(true);
        if (!enabledMasterServiceAgreement) {
            masterServiceAgreement.setState(ServiceAgreementState.DISABLED);
        }
        masterServiceAgreement.addParticipant(
            Lists.newArrayList(createMasterServiceAgreementParticipant(legalEntity))
        );

        persistenceServiceAgreementService.populateDefaultPermissionSets(masterServiceAgreement);

        return masterServiceAgreement;
    }

    private Participant createMasterServiceAgreementParticipant(LegalEntity legalEntity) {
        Participant participant = new Participant();
        participant.setLegalEntity(legalEntity);
        participant.setShareAccounts(true);
        participant.setShareUsers(true);
        return participant;
    }

    private LegalEntity validateRootLegalEntity(List<LegalEntity> legalEntities) {
        if (legalEntities.isEmpty()) {
            LOGGER.warn("Empty list of legal entities");
            throw getNotFoundException(ERR_ACQ_005.getErrorMessage(),
                ERR_ACQ_005.getErrorCode());
        }
        if (legalEntities.size() > 1) {
            LOGGER.warn("Unexpected number of root legal entities.");
            throw getInternalServerErrorException(QueryErrorCodes.ERR_ACQ_008.getErrorMessage());
        }
        return legalEntities.get(0);
    }


    private LegalEntity populateLegalEntityDomain(String externalId,
        String name, Map<String, String> additions, LegalEntityType type, LegalEntity parent) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(parent);
        legalEntity.setExternalId(externalId);
        legalEntity.setName(name);
        legalEntity.setAdditions(additions);
        legalEntity.setType(type);

        return legalEntity;
    }

    /**
     * Update legal entities.
     *
     * @param externalId     - external id
     * @param legalEntityPut - legal entity object to be updated
     */
    @Transactional
    public String updateLegalEntity(String externalId, LegalEntityByExternalIdPutRequestBody legalEntityPut) {
        LOGGER.info("Updating legal entity with legalEntityPut {}", legalEntityPut);
        LegalEntity legalEntity = findLegalEntityByExternalId(externalId);
        LegalEntity updatedLegalEntity = legalEntityStrategyContext
            .updateLegalEntity(LegalEntityType.fromString(legalEntityPut.getType().toString()), legalEntity);
        legalEntityJpaRepository.save(updatedLegalEntity);
        return legalEntity.getId();
    }

    /**
     * Updating all updatable fields of Legal Entity.
     *
     * @param externalId             - old external legal entity id.
     * @param legalEntityToBeUpdated - legal entity body that will be updated.
     */
    @Transactional
    public String updateLegalEntityFields(String externalId,
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity legalEntityToBeUpdated) {
        LOGGER.info("Updating legal entity with legalEntityPut {}", legalEntityToBeUpdated);
        LegalEntity legalEntity = findLegalEntityByExternalId(externalId);

        validateUniqueExternalIdToBeUpdated(legalEntityToBeUpdated.getExternalId(), legalEntity.getId());
        validateParentExternalId(legalEntityToBeUpdated.getParentExternalId(), legalEntity);

        LegalEntity updatedLegalEntity = legalEntityStrategyContext
            .updateLegalEntity(LegalEntityType.fromString(legalEntityToBeUpdated.getType().toString()), legalEntity);

        updatedLegalEntity.setExternalId(legalEntityToBeUpdated.getExternalId());
        updatedLegalEntity.setName(legalEntityToBeUpdated.getName());
        updatedLegalEntity.setAdditions(legalEntityToBeUpdated.getAdditions());

        return legalEntityJpaRepository.save(updatedLegalEntity).getId();
    }

    private LegalEntity findLegalEntityByExternalId(String externalId) {
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository
            .findByExternalId(externalId, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
        return legalEntityOptional.orElseThrow(
            () -> {
                LOGGER.warn("Legal entity with external id {} was not found.", externalId);
                return getNotFoundException(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode());
            }
        );
    }


    private void validateParentExternalId(String legalEntityExternalIdToBeUpdated, LegalEntity legalEntity) {
        boolean isValid = Optional.ofNullable(legalEntity.getParent())
            .map(le -> le.getExternalId().equals(legalEntityExternalIdToBeUpdated))
            .orElse(legalEntityExternalIdToBeUpdated == null);

        if (!isValid) {
            LOGGER.info("Parent external ID cannot be updated");
            throw getBadRequestException(ERR_ACQ_035.getErrorMessage(), ERR_ACQ_035.getErrorCode());
        }
    }

    private void validateUniqueExternalIdToBeUpdated(String externalId, String id) {
        legalEntityJpaRepository.findByExternalIdIgnoreCaseAndIdNot(externalId, id)
            .ifPresent(withExternal -> {
                LOGGER.warn("LegalEntity with external ID already exists {}.", withExternal);
                throw getBadRequestException(ERR_ACC_037.getErrorMessage(), ERR_ACC_037.getErrorCode());
            });
    }

    /**
     * Retrieves legal entities by external ids.
     *
     * @param ids              set of external ids
     * @param includeAdditions flag which indicates whether to include additions in the response or not
     * @return list of {@link LegalEntity}
     */
    public List<LegalEntity> getBatchLegalEntitiesByExternalIds(Set<String> ids,
        Boolean includeAdditions) {
        LOGGER.info("Trying to list Legal Entities by external ids {}", ids);

        String graphName = null;
        if (includeAdditions) {
            graphName = GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;
        }

        return getLegalEntitiesDomainByExternalIds(ids, graphName);
    }

    private List<LegalEntity> getLegalEntitiesDomainByExternalIds(Set<String> ids,
        String graphLegalEntity) {
        List<String> externalIdList = new ArrayList<>(ids);
        if (externalIdList.isEmpty()) {
            LOGGER.info("Returning empty list for empty list of ids {}", externalIdList);
            return new ArrayList<>();
        }
        return legalEntityJpaRepository.findDistinctByExternalIdIn(externalIdList, graphLegalEntity);
    }

    /**
     * Deletes legal entity identified by external id.
     *
     * @param externalId - legal entity external id
     */
    @Transactional
    public String deleteLegalEntityByExternalId(String externalId) {

        LegalEntity legalEntity = legalEntityJpaRepository
            .findByExternalId(externalId, GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)
            .orElseThrow(() -> {
                LOGGER.warn(LEGAL_ENTITY_WITH_EXTERNAL_ID_DOES_NOT_EXIST, externalId);
                return getNotFoundException(ERR_ACQ_005.getErrorMessage(),
                    ERR_ACQ_005.getErrorCode());
            });

        String id = legalEntity.getId();

        legalEntity.setChildren(
            new HashSet<>(
                legalEntityJpaRepository.findByParentId(
                    legalEntity.getId(),
                    PageRequest.of(0, 1)
                )
            )
        );

        validateDeleteOfLegalEntity(externalId, legalEntity);
        validateIfLegalEntityIsContainedInDataGroupOfType(legalEntity.getId(), CUSTOMERS_DATA_GROUP_TYPE);

        serviceAgreementJpaRepository.findByCreatorLegalEntityIdAndIsMaster(
            legalEntity.getId(),
            true,
            null)
            .ifPresent(sa -> {
                dataGroupJpaRepository.deleteAll(sa.getDataGroups());
                functionGroupJpaRepository.deleteAll(sa.getFunctionGroups());
                serviceAgreementJpaRepository.delete(sa);
            });

        legalEntityJpaRepository.delete(legalEntity);
        return id;
    }

    /**
     * Retrieves all legal entities that have data group of type customer and user has access to.
     *
     * @param holder - search and pagination parameters
     * @return list of {@link SegmentationGetResponseBodyQuery}
     */
    @Transactional(readOnly = true)
    public Page<SegmentationGetResponseBodyQuery> getLegalEntitySegmentation(
        GetLegalEntitySegmentationHolder holder) {

        String serviceAgreementId = getServiceAgreementId(holder);

        Set<String> selectedAfpIds = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                holder.getBusinessFunction(), null,
                Optional.ofNullable(holder.getPrivilege())
                    .map(Sets::newHashSet).orElseGet(HashSet::new));

        Set<String> dataGroupIds = userAssignedCombinationRepository
            .findByUserIdAndServiceAgreementIdAndAfpIdsInAndDataType(
                holder.getUserId(),
                serviceAgreementId,
                selectedAfpIds,
                "CUSTOMERS"
            ).stream().map(DataGroup::getId).collect(Collectors.toSet());
        Page<LegalEntity> allLegalEntitiesSegmentation;
        if (!dataGroupIds.isEmpty()) {
            allLegalEntitiesSegmentation = legalEntityJpaRepository.findAllLegalEntitiesSegmentation(
                holder.getSearchAndPaginationParameters(),
                dataGroupIds,
                GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
            List<LegalEntity> legalEntityStream = allLegalEntitiesSegmentation.get().collect(Collectors.toList());
            return new PageImpl<>(
                legalEntityToSegmentationBodyMapper.sourceToDestination(legalEntityStream),
                Pageable.unpaged(),
                allLegalEntitiesSegmentation.getTotalElements()
            );
        }
        return new PageImpl<>(new ArrayList<>(), Pageable.unpaged(), 0);
    }


    /**
     * Retrive a map of internal and external ids for legal entity.
     *
     * @param externalIds - list of external ids
     * @return Map<String, String>
     */

    public Map<String, String> findInternalByExternalIdsForLegalEntity(Set<String> externalIds) {
        LOGGER.info("Trying to retrieve a list of internal ids for external ids {} legal entity", externalIds);

        List<EntityIds> data = legalEntityJpaRepository.findByExternalIdIn(externalIds);
        return data.stream().collect(
            Collectors.toMap(EntityIds::getExternalId, EntityIds::getId));
    }


    private String getServiceAgreementId(GetLegalEntitySegmentationHolder holder) {
        if (Objects.isNull(holder.getServiceAgreementId()) && Objects.isNull(holder.getLegalEntityId())) {
            throw getBadRequestException(ERR_ACQ_064.getErrorMessage(), ERR_ACQ_064.getErrorCode());
        }
        if (Objects.nonNull(holder.getServiceAgreementId())) {
            return holder.getServiceAgreementId();
        }
        return serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(
                holder.getLegalEntityId(),
                true,
                null)
            .orElseThrow(() -> {
                LOGGER.warn(ERR_ACQ_005.getErrorMessage());
                return getNotFoundException(QueryErrorCodes.ERR_ACQ_006.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_006.getErrorCode());
            }).getId();
    }

    private void validateDeleteOfLegalEntity(String externalId, LegalEntity legalEntity) {
        if (!legalEntity.getChildren().isEmpty()) {
            LOGGER.warn("Legal entity ( external Id {}) contains children, cannot be deleted", externalId);
            throw getBadRequestException(ERR_ACQ_050.getErrorMessage(), ERR_ACQ_050.getErrorCode());
        }

        if (!legalEntityJpaRepository.checkIfNotParticipantInCustomServiceAgreement(externalId)) {
            LOGGER.warn("Legal entity (external ID {}) is participant in CSA, cannot be deleted", externalId);
            throw getBadRequestException(ERR_ACQ_051.getErrorMessage(), ERR_ACQ_051.getErrorCode());
        }
        if (!legalEntityJpaRepository.checkIfNotCreatorOfAnyCsa(externalId)) {
            LOGGER.warn("Legal entity (external ID {}) is creat of CSA, cannot be deleted", externalId);
            throw getBadRequestException(ERR_ACQ_052.getErrorMessage(), ERR_ACQ_052.getErrorCode());
        }
        if (legalEntityJpaRepository.checkIfExistsUsersFromLeWithAssignedPermissionsInMsa(externalId)) {
            LOGGER.warn("There are users from legal entity (external ID {}), "
                + "with assigned permissions in MSA, cannot be deleted", externalId);
            throw getBadRequestException(ERR_ACQ_053.getErrorMessage(), ERR_ACQ_053.getErrorCode());
        }
        if (legalEntityJpaRepository.checkIsExistsUsersFromLeWithPendingPermissionsInMsa(externalId)) {
            LOGGER.warn("There are users from legal entity (external ID {}), "
                + "with pending permissions in MSA, cannot be deleted", externalId);
            throw getBadRequestException(ERR_ACQ_055.getErrorMessage(), ERR_ACQ_055.getErrorCode());
        }
    }

    private void validateIfLegalEntityIsContainedInDataGroupOfType(String legalEntityId, String dataGroupType) {
        if (dataGroupJpaRepository.existsByDataItemTypeAndDataItemIds(dataGroupType, legalEntityId)) {
            throw getBadRequestException(ERR_ACC_098.getErrorMessage(), ERR_ACC_098.getErrorCode());
        }
    }
}
