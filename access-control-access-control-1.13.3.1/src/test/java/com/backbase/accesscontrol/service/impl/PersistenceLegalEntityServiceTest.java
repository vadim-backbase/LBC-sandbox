package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_004;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_007;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_008;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_009;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_037;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_043;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_098;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_005;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_009;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_035;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_050;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.EntityIds;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.LegalEntityToSegmentationBodyMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.impl.strategy.legalentity.LegalEntityStrategyContext;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ExistingCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewMasterServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantInfo;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantOf;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceLegalEntityServiceTest {

    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private LegalEntityStrategyContext legalEntityStrategyContext;
    @Mock
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Mock
    private LegalEntityToSegmentationBodyMapper legalEntityToSegmentationBodyMapper;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private UserAssignedCombinationRepository userAssignedCombinationRepository;
    @InjectMocks
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Test
    public void addEnabledLegalEntityTest() {
        String parentExternalId = "PAR-EX-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            parentExternalId,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER,
            true
        );

        LegalEntity parentLegalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.BANK);

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(false);
        when(legalEntityJpaRepository.findByExternalId(parentExternalId))
            .thenReturn(Optional.of(legalEntity));

        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        LegalEntity responseLegalEntity = persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody);

        verify(legalEntityJpaRepository, times(1))
            .findByExternalId(parentExternalId);
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
        assertEquals(legalEntityPostRequestBody.getName(), responseLegalEntity.getName());
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
    }

    @Test
    public void shouldAddLegalEntityWithInternalParentIdTestWithMSA() {
        String parentInternalId = "PAR-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        PresentationCreateLegalEntityItemPostRequestBody legalEntityPostRequestBody = new
            PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId(externalId)
            .withName(legalEntityName)
            .withParentInternalId(parentInternalId)
            .withType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER)
            .withActivateSingleServiceAgreement(true);

        LegalEntity parentLegalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.BANK);

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(false);
        when(legalEntityJpaRepository.findById(eq(parentInternalId), eq(GRAPH_LEGAL_ENTITY_WITH_ANCESTORS)))
            .thenReturn(Optional.of(legalEntity));

        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        LegalEntity responseLegalEntity = persistenceLegalEntityService
            .createLegalEntityWithInternalParentId(legalEntityPostRequestBody);

        verify(legalEntityJpaRepository, times(1))
            .findById(parentInternalId, GRAPH_LEGAL_ENTITY_WITH_ANCESTORS);
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
        assertEquals(legalEntityPostRequestBody.getName(), responseLegalEntity.getName());
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
    }

    @Test
    public void shouldThrowBadRequestWhenAddLegalEntityBankUnderCustomerTest() {
        String parentExternalId = "PAR-EX-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            parentExternalId,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK,
            true
        );

        LegalEntity parentLegalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.CUSTOMER);

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(false);
        when(legalEntityJpaRepository.findByExternalId(parentExternalId))
            .thenReturn(Optional.of(legalEntity));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_008.getErrorMessage(), ERR_ACC_008.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenAddLegalEntityWithInternalParentIdBankUnderCustomerTest() {
        String parentInternalId = "PAR-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        PresentationCreateLegalEntityItemPostRequestBody legalEntityPostRequestBody = new
            PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId(externalId)
            .withName(legalEntityName)
            .withParentInternalId(parentInternalId)
            .withType(com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
            .withActivateSingleServiceAgreement(true);

        LegalEntity parentLegalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.CUSTOMER);

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(false);
        when(legalEntityJpaRepository.findById(parentInternalId, GRAPH_LEGAL_ENTITY_WITH_ANCESTORS))
            .thenReturn(Optional.of(legalEntity));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.createLegalEntityWithInternalParentId(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_008.getErrorMessage(), ERR_ACC_008.getErrorCode()));
    }

    @Test
    public void addLegalEntityShouldThrowBadRequestExceptionWhenTheLegalEntityByExternalIDAlreadyExists() {
        String parentExternalId = "PAR-EX-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            parentExternalId,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER,
            true
        );

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenIngestingRootAsCustomer() {
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            null,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER,
            true
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_009.getErrorMessage(), ERR_ACC_009.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenIngestingRootAsCustomerInCreateLegalEntityWithInternalParentId() {
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        PresentationCreateLegalEntityItemPostRequestBody legalEntityPostRequestBody = new
            PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId(externalId)
            .withName(legalEntityName)
            .withParentInternalId(null)
            .withType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER)
            .withActivateSingleServiceAgreement(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.createLegalEntityWithInternalParentId(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_009.getErrorMessage(), ERR_ACC_009.getErrorCode()));
    }

    @Test
    public void addLegalEntityShouldThrowBadRequestExceptionWhenLegalEntityIsRootAndRootAlreadyExists() {
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            null,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK, true);
        LegalEntity rootLegalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.existsByParentIsNull())
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_007.getErrorMessage(), ERR_ACC_007.getErrorCode()));
    }

    @Test
    public void addLegalEntityShouldThrowBadRequestExceptionWhenParentLegalEntityNotExists() {
        String parentExternalId = "PAR-EX-ID";
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(
            externalId,
            legalEntityName,
            parentExternalId,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER,
            true);

        when(legalEntityJpaRepository.existsByExternalId(externalId))
            .thenReturn(false);
        when(legalEntityJpaRepository.findByExternalId(parentExternalId))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_007.getErrorMessage(), ERR_ACC_007.getErrorCode()));
    }

    @Test
    public void getLegalEntitiesWithoutParentTest() {
        LegalEntity legalEntityWithoutParent = createLegalEntity(
            "ex1",
            "LE-01",
            null,
            LegalEntityType.BANK);

        when(legalEntityJpaRepository.findDistinctByParentIsNull(GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT))
            .thenReturn(Collections.singletonList(legalEntityWithoutParent));

        List<LegalEntity> legalEntities = persistenceLegalEntityService.getLegalEntities(null);

        verify(legalEntityJpaRepository, times(1))
            .findDistinctByParentIsNull(GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT);
        assertEquals(1, legalEntities.size());
    }

    @Test
    public void getLegalEntitiesWithParentTest() {
        LegalEntity legalEntityWithoutParent = createLegalEntity("ex1", "LE-01", null, LegalEntityType.BANK);
        LegalEntity legalEntityWithParent = createLegalEntity("ex2", "LE-02", legalEntityWithoutParent,
            LegalEntityType.CUSTOMER);

        when(legalEntityJpaRepository.findDistinctByParentId(eq(legalEntityWithoutParent.getId())))
            .thenReturn(Collections.singletonList(legalEntityWithParent));

        List<LegalEntity> legalEntities = persistenceLegalEntityService
            .getLegalEntities(legalEntityWithoutParent.getId());

        verify(legalEntityJpaRepository, times(1)).findDistinctByParentId(legalEntityWithoutParent.getId());
        assertEquals(1, legalEntities.size());
    }

    @Test
    public void getLegalEntityByExternalIdNotFoundException() {
        String externalId = "ex-id-01";
        when(legalEntityJpaRepository.findByExternalId(eq(externalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getLegalEntityByExternalId(externalId, true));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void getLegalEntityByExternalId() {
        String externalId = "ex-id-01";
        String legalEntityName = "le-name";
        LegalEntity parentLegalEntity = createLegalEntity("ex-id-par", "parent-le", null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.CUSTOMER);
        when(legalEntityJpaRepository.findByExternalId(eq(externalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));
        LegalEntity responseLegalEntityByExternalId = persistenceLegalEntityService
            .getLegalEntityByExternalId(externalId, true);
        verify(legalEntityJpaRepository, times(1))
            .findByExternalId(eq(externalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS));
        assertEquals(legalEntity, responseLegalEntityByExternalId);
    }

    @Test
    public void getLegalEntityById() {
        LegalEntity legalEntity = createLegalEntity("exid", "lename", null, LegalEntityType.BANK);
        when(legalEntityJpaRepository
            .findById(eq(legalEntity.getId()), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));

        LegalEntity legalEntityById = persistenceLegalEntityService.getLegalEntityById(legalEntity.getId());

        assertEquals(legalEntity.getId(), legalEntityById.getId());
    }

    @Test
    public void getRootLegalEntity() {
        LegalEntity legalEntity = createLegalEntity("exid", "lename", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findDistinctByParentIsNull()).thenReturn(singletonList(legalEntity));

        LegalEntity rootLegalEntity = persistenceLegalEntityService.getRootLegalEntity();

        assertNull(rootLegalEntity.getParent());
    }

    @Test
    public void getRootLegalEntityThatDoesNotExist() {
        when(legalEntityJpaRepository.findDistinctByParentIsNull()).thenReturn(new ArrayList<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getRootLegalEntity());
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void getRootLegalEntityWithInternalServerError() {
        LegalEntity legalEntity = createLegalEntity("exid", "lename", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findDistinctByParentIsNull()).thenReturn(asList(legalEntity, legalEntity));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
            () -> persistenceLegalEntityService.getRootLegalEntity());
        assertEquals("Unexpected number of Legal Entity document", exception.getMessage());
    }

    @Test
    public void getParents() {
        LegalEntity parent = createLegalEntity("parentex", "nameex", null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity("exid", "lename", parent, LegalEntityType.CUSTOMER);

        legalEntity.setLegalEntityAncestors(Collections.singletonList(parent));

        when(legalEntityJpaRepository.findById(eq(legalEntity.getId()), eq(
            GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));

        List<LegalEntity> parents = persistenceLegalEntityService.getParents(legalEntity.getId());

        assertEquals(1, parents.size());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenTheLegalEntityDoesNotExist() {
        String id = UUID.randomUUID().toString();

        when(legalEntityJpaRepository.findById(eq(id), eq(GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getParents(id));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void shouldThrowInternalServerExceptionWhenTheLegalEntityIsRoot() {
        LegalEntity root = createLegalEntity("parentex", "nameex", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findById(eq(root.getId()), eq(
            GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS)))
            .thenReturn(Optional.of(root));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
            () -> persistenceLegalEntityService.getParents(root.getId()));
        assertEquals(ERR_ACQ_009.getErrorMessage(), exception.getMessage());
    }

    @Test
    public void getLegalEntityByIdWhenTheLegalEntityDoesNotExist() {
        String id = UUID.randomUUID().toString();
        when(legalEntityJpaRepository.findById(eq(id), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getLegalEntityById(id));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void getSubEntities() {
        String query = null;
        String parentLegalEntityId = UUID.randomUUID().toString();
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(parentLegalEntityId);

        String firstChildId = UUID.randomUUID().toString();
        LegalEntity firstChild = new LegalEntity();
        firstChild.setId(firstChildId);

        String secondChildId = UUID.randomUUID().toString();
        LegalEntity secondChild = new LegalEntity();
        secondChild.setId(secondChildId);

        List<LegalEntity> allLegalEntities = asList(legalEntity, firstChild, secondChild);
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(null, null,
            query, null);

        when(legalEntityJpaRepository.findAllSubEntities(
            eq(parentLegalEntityId),
            eq(searchAndPaginationParameters),
            anySet(), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(new PageImpl<>(allLegalEntities));

        List<LegalEntity> subEntities = persistenceLegalEntityService.getSubEntities(
            parentLegalEntityId,
            searchAndPaginationParameters, new HashSet<>())
            .getContent();

        verify(legalEntityJpaRepository, times(1))
            .findAllSubEntities(eq(parentLegalEntityId), eq(searchAndPaginationParameters),
                anySet(), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)
            );
        assertEquals(3, subEntities.size());
        assertTrue(subEntities.containsAll(asList(legalEntity, firstChild, secondChild)));
    }

    @Test
    public void getLegalEntitiesSegmentationReturnEmptyListWhenDataGroupIdsFromUserAssignedPrivDgAreZero() {
        String externalId = "externalId";

        SearchAndPaginationParameters searchParameters = new SearchAndPaginationParameters(0, 10, externalId, null);

        GetLegalEntitySegmentationHolder parameters = new GetLegalEntitySegmentationHolder()
            .withPrivilege("view")
            .withBusinessFunction("entitlements")
            .withServiceAgreementId("saId")
            .withSearchAndPaginationParameters(searchParameters);

        List<SegmentationGetResponseBodyQuery> returnedList = persistenceLegalEntityService
            .getLegalEntitySegmentation(parameters).get().collect(Collectors.toList());
        assertEquals(0, returnedList.size());
        verify(businessFunctionCache).getByFunctionNameOrResourceNameOrPrivilegesOptional(
            eq("entitlements"), ArgumentMatchers.isNull(), eq(Sets.newHashSet("view")));
    }

    @Test
    public void getLegalEntitiesSegmentation() {
        String externalId = "externalId";

        SearchAndPaginationParameters searchParameters = new SearchAndPaginationParameters(0, 10, externalId, null);
        LegalEntity legalEntity = new LegalEntity()
            .withExternalId(externalId);
        SegmentationGetResponseBodyQuery segmentationLegalEntity = new SegmentationGetResponseBodyQuery()
            .withExternalId(externalId);
        List<LegalEntity> allLegalEntities = singletonList(legalEntity);

        GetLegalEntitySegmentationHolder parameters = new GetLegalEntitySegmentationHolder()
            .withPrivilege("view")
            .withBusinessFunction("entitlements")
            .withServiceAgreementId("saId")
            .withSearchAndPaginationParameters(searchParameters);

        Set<String> dataGroupIds = Sets.newHashSet(singletonList("dataGroupId"));
        List<DataGroup> dataGroups = singletonList(new DataGroup().withId("dataGroupId"));
        when(userAssignedCombinationRepository
            .findByUserIdAndServiceAgreementIdAndAfpIdsInAndDataType(
                eq(parameters.getUserId()),
                eq(parameters.getServiceAgreementId()),
                anySet(),
                eq("CUSTOMERS")
            )).thenReturn(dataGroups);

        when(legalEntityJpaRepository
            .findAllLegalEntitiesSegmentation(refEq(searchParameters), eq(dataGroupIds),
                eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(new PageImpl<>(allLegalEntities));

        List<LegalEntity> sourceList = singletonList(legalEntity);
        List<SegmentationGetResponseBodyQuery> destList = singletonList(segmentationLegalEntity);
        when(legalEntityToSegmentationBodyMapper.sourceToDestination(sourceList)).thenReturn(destList);

        List<SegmentationGetResponseBodyQuery> returnedList = persistenceLegalEntityService
            .getLegalEntitySegmentation(parameters).get().collect(Collectors.toList());
        assertEquals(returnedList.get(0).getExternalId(), legalEntity.getExternalId());
        verify(businessFunctionCache).getByFunctionNameOrResourceNameOrPrivilegesOptional(
            eq("entitlements"), ArgumentMatchers.isNull(), eq(Sets.newHashSet("view")));
    }

    @Test
    public void shouldReturnEmptyListWhenQueryIsPresent() {
        String parentLegalEntityId = UUID.randomUUID().toString();
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(null, null,
            "somestring", null);

        String id = "0123";
        Set excludedIds = new HashSet() {{
            add(id);
        }};

        when(legalEntityJpaRepository.findAllSubEntities(
            eq(parentLegalEntityId),
            eq(searchAndPaginationParameters),
            anySet(), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<LegalEntity> pageResponse = persistenceLegalEntityService.getSubEntities(
            parentLegalEntityId,
            searchAndPaginationParameters, excludedIds);
        List<LegalEntity> subEntities = pageResponse.getContent();

        verify(legalEntityJpaRepository, times(1))
            .findAllSubEntities(parentLegalEntityId, searchAndPaginationParameters,
                excludedIds,
                GRAPH_LEGAL_ENTITY_WITH_ADDITIONS
            );
        assertEquals(0, subEntities.size());
    }

    @Test
    public void shouldCreateMasterServiceAgreementOnCreatingLegalEntity() {
        ArgumentCaptor<ServiceAgreement> serviceAgreementCaptor = ArgumentCaptor.forClass(ServiceAgreement.class);
        String legalEntityName = "LE-NAME";
        String externalId = "EX-ID-01";

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = createAddLegalEntityPostRequestBody(externalId,
            legalEntityName, null,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK, true);

        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        doNothing().when(persistenceServiceAgreementService).populateDefaultPermissionSets(any(ServiceAgreement.class));

        LegalEntity responseLegalEntity = persistenceLegalEntityService.addLegalEntity(legalEntityPostRequestBody);

        verify(serviceAgreementJpaRepository, times(1)).save(serviceAgreementCaptor.capture());
        verify(persistenceServiceAgreementService, times(1)).populateDefaultPermissionSets(any());
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
        assertEquals(legalEntityPostRequestBody.getName(), responseLegalEntity.getName());
        assertEquals(legalEntityPostRequestBody.getExternalId(), responseLegalEntity.getExternalId());
        assertEquals(legalEntity.getName(), serviceAgreementCaptor.getValue().getName());
        assertEquals("Master Service Agreement", serviceAgreementCaptor.getValue().getDescription());
        assertEquals(legalEntity.getId(), serviceAgreementCaptor.getValue().getCreatorLegalEntity().getId());
        assertEquals(serviceAgreementCaptor.getValue().getCreatorLegalEntity().getId(), legalEntity.getId());
    }

    @Test
    public void getMasterServiceAgreementTest() {
        LegalEntity legalEntity = createLegalEntity("externalId", "LE-001", null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", legalEntity, "C1",
            "P1");
        serviceAgreement.setId("SA_ID");
        serviceAgreement.setMaster(true);
        when(legalEntityJpaRepository
            .findById(eq(legalEntity.getId()), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));
        when(serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(eq(serviceAgreement.getCreatorLegalEntity().getId()),
                eq(serviceAgreement.isMaster()), eq(SERVICE_AGREEMENT_WITH_ADDITIONS))).thenReturn(Optional.of
            (serviceAgreement));

        ServiceAgreement masterServiceAgreement = persistenceLegalEntityService
            .getMasterServiceAgreement(serviceAgreement.getCreatorLegalEntity().getId());

        verify(legalEntityJpaRepository, times(1)).findById(masterServiceAgreement.getCreatorLegalEntity().getId(),
            GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS);
        verify(serviceAgreementJpaRepository, times(1))
            .findByCreatorLegalEntityIdAndIsMaster(masterServiceAgreement.getCreatorLegalEntity().getId(),
                masterServiceAgreement.isMaster(), SERVICE_AGREEMENT_WITH_ADDITIONS);

        assertEquals(serviceAgreement.getId(), masterServiceAgreement.getId());
        assertEquals(serviceAgreement.getName(), masterServiceAgreement.getName());
        assertEquals(serviceAgreement.getDescription(), masterServiceAgreement.getDescription());
        assertEquals(serviceAgreement.getCreatorLegalEntity(), masterServiceAgreement.getCreatorLegalEntity());
        assertEquals(serviceAgreement.getParticipants(), masterServiceAgreement.getParticipants());
        assertTrue(masterServiceAgreement.isMaster());
    }

    @Test
    public void getMasterServiceAgreementWhenServiceAgreementNotExistsTest() {
        LegalEntity legalEntity = createLegalEntity("externalId", "LE-001", null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", legalEntity, "C1",
            "P1");
        serviceAgreement.setId("SA_ID");
        serviceAgreement.setMaster(true);
        when(legalEntityJpaRepository
            .findById(eq(legalEntity.getId()), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));
        when(serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(eq(serviceAgreement.getCreatorLegalEntity().getId()),
                eq(serviceAgreement.isMaster()), eq(SERVICE_AGREEMENT_WITH_ADDITIONS))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceLegalEntityService
            .getMasterServiceAgreement(serviceAgreement.getCreatorLegalEntity().getId()));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void getMasterServiceAgreementWhenLegalEntityNotExistsTest() {
        LegalEntity legalEntity = createLegalEntity("externalId", "LE-001", null, LegalEntityType.BANK);
        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "desc", legalEntity, "C1",
            "P1");
        serviceAgreement.setId("SA_ID");
        serviceAgreement.setMaster(true);
        when(legalEntityJpaRepository
            .findById(eq(legalEntity.getId()), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceLegalEntityService
            .getMasterServiceAgreement(serviceAgreement.getCreatorLegalEntity().getId()));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenCSAParticipantsSharesNoUsersNoAccountsTest() {
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
            .withLegalEntityName("le-test")
            .withLegalEntityExternalId("le-ext-id")
            .withLegalEntityType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
            .withParticipantOf(new ParticipantOf()
                .withNewCustomServiceAgreement(new NewCustomServiceAgreement().withParticipantInfo(
                    new ParticipantInfo().withShareAccounts(false).withShareUsers(false)
                ))
            );

        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        String parentExternalId = "RootLE";
        LegalEntity parentLe =
            createLegalEntity(parentExternalId, "rootLe", null, LegalEntityType.BANK);
        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", parentLe, LegalEntityType.BANK);
        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> persistenceLegalEntityService
            .createLegalEntityAsParticipant(requestBody, "creator-le-id"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode()));
    }

    @Test
    public void shouldThrowNotExistingException() {
        String externalId = "notExisting";
        when(legalEntityJpaRepository.findByExternalId(eq(externalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> persistenceLegalEntityService
            .updateLegalEntity(externalId, new LegalEntityByExternalIdPutRequestBody()));
        assertEquals("Not Found", exception.getMessage());
    }

    @Test
    public void shouldUpdateLegalEntity() {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId("id");
        String externalId = "someEntity";
        LegalEntity parentLegalEntity = createLegalEntity("PARENT EX ID", "Parent LE Name", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findByExternalId(eq(externalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(legalEntity));

        LegalEntity legalEntity1Returned = createLegalEntity(null, null, parentLegalEntity, LegalEntityType.CUSTOMER);

        when(legalEntityStrategyContext.updateLegalEntity(
            LegalEntityType.BANK, legalEntity))
            .thenReturn(legalEntity1Returned);

        persistenceLegalEntityService.updateLegalEntity(
            externalId,
            new LegalEntityByExternalIdPutRequestBody()
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
        );

        verify(legalEntityStrategyContext).updateLegalEntity(
            eq(LegalEntityType.BANK),
            eq(legalEntity));
        verify(legalEntityJpaRepository).save(legalEntity1Returned);
    }

    @Test
    public void getBatchLegalEntitiesByExternalIds() {
        String externalId = "ex-id-01";
        String externalId1 = "ex-id-par";
        String legalEntityName = "le-name";
        Set<String> ids = new HashSet<>();
        ids.add(externalId);
        ids.add(externalId1);

        LegalEntity parentLegalEntity = createLegalEntity(externalId1, "parent-le", null, LegalEntityType.BANK);
        LegalEntity legalEntity = createLegalEntity(externalId, legalEntityName, parentLegalEntity,
            LegalEntityType.CUSTOMER);
        List<LegalEntity> legalEntities = new ArrayList<>();
        legalEntities.add(parentLegalEntity);
        legalEntities.add(legalEntity);
        when(legalEntityJpaRepository.findDistinctByExternalIdIn(eq(Lists.newArrayList(ids)),
            eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(legalEntities);

        List<LegalEntity> legalEntitiesByExternalIds = persistenceLegalEntityService
            .getBatchLegalEntitiesByExternalIds(ids, true);

        verify(legalEntityJpaRepository, times(1)).findDistinctByExternalIdIn(eq(Lists.newArrayList(ids)),
            eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS));
        assertEquals(2, legalEntitiesByExternalIds.size());
    }

    @Test
    public void testUpdateLegalEntityFields() {
        String oldExternalId = "Ex-Id-Old";
        LegalEntity parentLegalEntity = createLegalEntity("PARENT EX ID", "Parent LE Name", null, LegalEntityType.BANK);
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity legalEntity =
            new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity()
                .withExternalId("new-ex-id")
                .withName("new name")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER)
                .withParentExternalId(parentLegalEntity.getExternalId());

        LegalEntity oldLegalEntity = createLegalEntity(oldExternalId, "Old LE Name", parentLegalEntity,
            LegalEntityType.BANK);

        when(legalEntityJpaRepository.findByExternalId(eq(oldExternalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(oldLegalEntity));

        when(legalEntityJpaRepository
            .findByExternalIdIgnoreCaseAndIdNot(legalEntity.getExternalId(), oldLegalEntity.getId()))
            .thenReturn(Optional.empty());
        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(oldLegalEntity);

        when(legalEntityStrategyContext.updateLegalEntity(LegalEntityType.CUSTOMER, oldLegalEntity))
            .thenReturn(createLegalEntity(null, null,
                parentLegalEntity, LegalEntityType.CUSTOMER));

        persistenceLegalEntityService.updateLegalEntityFields(
            oldExternalId,
            legalEntity
        );
        ArgumentCaptor<LegalEntity> captor = ArgumentCaptor.forClass(LegalEntity.class);
        verify(legalEntityJpaRepository).save(captor.capture());

        assertEquals(legalEntity.getExternalId(), captor.getValue().getExternalId());
        assertEquals(legalEntity.getName(), captor.getValue().getName());
        assertEquals(legalEntity.getType().toString(), captor.getValue().getType().toString());
        assertEquals(legalEntity.getParentExternalId(), captor.getValue().getParent().getExternalId());

    }

    @Test
    public void testUpdateLegalEntityWithNullParentID() {
        String oldExternalId = "Ex-Id-Old";
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity legalEntity =
            new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity()
                .withExternalId("new-ex-id")
                .withName("new name")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
                .withParentExternalId(null);

        LegalEntity parentLegalEntity = createLegalEntity("PARENT EX ID", "Parent LE Name", null, LegalEntityType.BANK);
        LegalEntity oldLegalEntity = createLegalEntity(oldExternalId, "Old LE Name", parentLegalEntity,
            LegalEntityType.BANK);

        when(legalEntityJpaRepository.findByExternalId(eq(oldExternalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(oldLegalEntity));

        when(legalEntityJpaRepository
            .findByExternalIdIgnoreCaseAndIdNot(legalEntity.getExternalId(), oldLegalEntity.getId()))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.updateLegalEntityFields(
                oldExternalId,
                legalEntity));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_035.getErrorMessage(), ERR_ACQ_035.getErrorCode()));
    }

    @Test
    public void testUpdateParentIdOnLegalEntity() {
        String oldExternalId = "Ex-Id-Old";
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity legalEntity =
            new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity()
                .withExternalId("new-ex-id")
                .withName("new name")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
                .withParentExternalId("PARENT EX ID");

        LegalEntity oldLegalEntity = createLegalEntity(oldExternalId, "Old LE Name", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findByExternalId(eq(oldExternalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(oldLegalEntity));

        when(legalEntityJpaRepository
            .findByExternalIdIgnoreCaseAndIdNot(legalEntity.getExternalId(), oldLegalEntity.getId()))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.updateLegalEntityFields(
                oldExternalId,
                legalEntity
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_035.getErrorMessage(), ERR_ACQ_035.getErrorCode()));
    }

    @Test
    public void testUpdateLegalEntityWhenNewExternalIdIsNotUnique() {
        String oldExternalId = "Ex-Id-Old";
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity legalEntity =
            new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity()
                .withExternalId("new-ex-id")
                .withName("new name")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
                .withParentExternalId("PARENT EX ID");

        LegalEntity oldLegalEntity = createLegalEntity(oldExternalId, "Old LE Name", null, LegalEntityType.BANK);

        when(legalEntityJpaRepository.findByExternalId(eq(oldExternalId), eq(GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)))
            .thenReturn(Optional.of(oldLegalEntity));

        when(legalEntityJpaRepository
            .findByExternalIdIgnoreCaseAndIdNot(legalEntity.getExternalId(), oldLegalEntity.getId()))
            .thenReturn(Optional.of(new LegalEntity()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.updateLegalEntityFields(
                oldExternalId,
                legalEntity
            ));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_037.getErrorMessage(), ERR_ACC_037.getErrorCode()));
    }


    @Test
    public void testServiceAgreementMasterByNotExistingLegalEntity() {
        when(legalEntityJpaRepository.findByExternalId(eq("externalId")))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getMasterServiceAgreementByExternalId("externalId"));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void testServiceAgreementMasaterByNotExistingServiceAgreement() {
        LegalEntity legalEntity = createLegalEntity("externalId", "LE Name", null, LegalEntityType.BANK);
        legalEntity.setExternalId("le.id");
        when(legalEntityJpaRepository.findByExternalId(eq("externalId")))
            .thenReturn(Optional.of(legalEntity));

        when(serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(legalEntity.getId(), true, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.getMasterServiceAgreementByExternalId("externalId"));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void testServiceAgreementMasaterByExternalId() {
        LegalEntity legalEntity = createLegalEntity("externalId", "LE Name", null, LegalEntityType.BANK);
        legalEntity.setExternalId("le.id");
        when(legalEntityJpaRepository.findByExternalId(eq("externalId")))
            .thenReturn(Optional.of(legalEntity));

        ServiceAgreement response = new ServiceAgreement();
        when(serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(legalEntity.getId(), true, SERVICE_AGREEMENT_WITH_ADDITIONS))
            .thenReturn(Optional.of(response));

        ServiceAgreement externalId = persistenceLegalEntityService.getMasterServiceAgreementByExternalId("externalId");
        assertEquals(externalId, response);
    }

    @Test
    public void testDeleteNonExistsLegalEntity() {

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> persistenceLegalEntityService.deleteLegalEntityByExternalId("externalId"));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode()));
    }

    @Test
    public void testDeleteLegalEntityWithChildren() {
        LegalEntity le1 = LegalEntityUtil.createLegalEntity("le1", "le1", "le1", null, LegalEntityType.CUSTOMER);
        LegalEntity le2 = LegalEntityUtil.createLegalEntity("le2", "le2", "le2", le1, LegalEntityType.CUSTOMER);
        le1.getChildren().add(le2);

        when(legalEntityJpaRepository
            .findByExternalId(eq("externalId"), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.of(le1));

        when(legalEntityJpaRepository.findByParentId(
            eq(le1.getId()),
            eq(PageRequest.of(0, 1))
        )).thenReturn(singletonList(le2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.deleteLegalEntityByExternalId("externalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_050.getErrorMessage(), ERR_ACQ_050.getErrorCode()));
    }

    @Test
    public void testDeleteLegalEntityContainedInDataGroupOfTypeCustomers() {
        LegalEntity le1 = LegalEntityUtil.createLegalEntity("le1", "le1", "le1", null, LegalEntityType.CUSTOMER);
        LegalEntity le2 = LegalEntityUtil.createLegalEntity("le2", "le2", "le2", le1, LegalEntityType.CUSTOMER);
        le1.getChildren().add(le2);

        when(legalEntityJpaRepository
            .findByExternalId(eq("externalId"), eq(GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS)))
            .thenReturn(Optional.of(le1));

        when(legalEntityJpaRepository.findByParentId(
            eq(le1.getId()),
            eq(PageRequest.of(0, 1))
        )).thenReturn(new ArrayList<>());

        when(legalEntityJpaRepository.checkIfNotParticipantInCustomServiceAgreement(anyString()))
            .thenReturn(true);
        when(legalEntityJpaRepository.checkIfNotCreatorOfAnyCsa(anyString())).thenReturn(true);
        when(legalEntityJpaRepository.checkIfExistsUsersFromLeWithAssignedPermissionsInMsa(anyString()))
            .thenReturn(false);
        when(legalEntityJpaRepository.checkIsExistsUsersFromLeWithPendingPermissionsInMsa(anyString()))
            .thenReturn(false);
        when(dataGroupJpaRepository
            .existsByDataItemTypeAndDataItemIds(eq("CUSTOMERS"), anyString()))
            .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> persistenceLegalEntityService.deleteLegalEntityByExternalId("externalId"));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_098.getErrorMessage(), ERR_ACC_098.getErrorCode()));
    }

    @Test
    public void testFindInternalByExternalIdsForLegalEntity() {
        String internalId = "key";
        String externalId = "value";
        EntityIds expected = new EntityIds(internalId, externalId);

        when(legalEntityJpaRepository.findByExternalIdIn(eq(Collections.singleton(externalId))))
            .thenReturn(Collections.singletonList(expected));
        Map<String, String> result = persistenceLegalEntityService
            .findInternalByExternalIdsForLegalEntity(Collections.singleton(externalId));
        assertEquals(1, result.size());
        assertTrue(result.containsKey(externalId));
        assertEquals(internalId, result.get(externalId));

    }

    @Test
    public void testShouldCreateLegalEntity() {
        CreateLegalEntitiesPostRequestBody createLegalEntitiesPostRequestBody =
            new CreateLegalEntitiesPostRequestBody()
                .withName("le-test")
                .withExternalId("le-ext-id")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK);

        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", null, LegalEntityType.BANK);
        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        when(legalEntityJpaRepository.existsByParentIsNull()).thenReturn(false);

        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);
        LegalEntity result = persistenceLegalEntityService
            .createLegalEntity(createLegalEntitiesPostRequestBody);
        assertEquals(createLegalEntitiesPostRequestBody.getExternalId(), result.getExternalId());
    }

    @Test
    public void testShouldCreateLegalEntityWithParent() {
        String parentExternalId = "RootLE";
        CreateLegalEntitiesPostRequestBody createLegalEntitiesPostRequestBody =
            new CreateLegalEntitiesPostRequestBody()
                .withParentExternalId(parentExternalId)
                .withName("le-test")
                .withExternalId("le-ext-id")
                .withType(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK);

        LegalEntity parentLe =
            createLegalEntity(parentExternalId, "rootLe", null, LegalEntityType.BANK);
        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", parentLe, LegalEntityType.BANK);
        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        when(legalEntityJpaRepository.findByExternalId(parentExternalId)).thenReturn(Optional.of(parentLe));

        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);
        LegalEntity result = persistenceLegalEntityService
            .createLegalEntity(createLegalEntitiesPostRequestBody);
        assertEquals(createLegalEntitiesPostRequestBody.getExternalId(), result.getExternalId());
    }

    @Test
    public void testShouldCreateLegalEntityAsParticipantOfExistingCustomServiceAgreement() {
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
            .withLegalEntityName("le-test")
            .withLegalEntityExternalId("le-ext-id")
            .withLegalEntityType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
            .withParticipantOf(new ParticipantOf()
                .withExistingCustomServiceAgreement(new ExistingCustomServiceAgreement()
                    .withServiceAgreementId("sa-test")
                    .withParticipantInfo(new ParticipantInfo()
                        .withShareAccounts(true)
                        .withShareUsers(false)))
                .withNewCustomServiceAgreement(new NewCustomServiceAgreement().withParticipantInfo(
                    new ParticipantInfo().withShareAccounts(true).withShareUsers(false)
                ))
            );

        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        String parentExternalId = "RootLE";
        LegalEntity parentLe =
            createLegalEntity(parentExternalId, "rootLe", null, LegalEntityType.BANK);
        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", parentLe, LegalEntityType.BANK);
        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        LegalEntityAsParticipantPostResponseBody response = persistenceLegalEntityService
            .createLegalEntityAsParticipant(requestBody, "creator-le-id");
        assertNotNull(response.getLegalEntityId());
        assertNull(response.getServiceAgreementId());
        verify(persistenceServiceAgreementService)
            .addParticipant(eq(requestBody.getParticipantOf().getExistingCustomServiceAgreement()),
                eq(legalEntity.getExternalId()));
        verify(persistenceServiceAgreementService, times(0))
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), anyString());
        verify(persistenceServiceAgreementService, times(0))
            .save(any(LegalEntity.class), any(NewMasterServiceAgreement.class));
    }

    @Test
    public void testShouldCreateLegalEntityAsParticipantOfNewCustomServiceAgreement() {
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
            .withLegalEntityName("le-test")
            .withLegalEntityExternalId("le-ext-id")
            .withLegalEntityType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
            .withParticipantOf(new ParticipantOf()
                .withNewCustomServiceAgreement(new NewCustomServiceAgreement()
                    .withServiceAgreementName("sa-name")
                    .withServiceAgreementExternalId("sa-ext-id")
                    .withParticipantInfo(new ParticipantInfo()
                        .withShareAccounts(true)
                        .withShareUsers(true))));

        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        String parentExternalId = "RootLE";
        LegalEntity parentLe =
            createLegalEntity(parentExternalId, "rootLe", null, LegalEntityType.BANK);
        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", parentLe, LegalEntityType.BANK);
        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement().withId(UUID.randomUUID().toString());
        when(persistenceServiceAgreementService
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), anyString()))
            .thenReturn(serviceAgreement);

        String legalEntityId = "creator-le-id";
        LegalEntityAsParticipantPostResponseBody response = persistenceLegalEntityService
            .createLegalEntityAsParticipant(requestBody, legalEntityId);
        assertNotNull(response.getLegalEntityId());
        assertEquals(serviceAgreement.getId(), response.getServiceAgreementId());
        verify(persistenceServiceAgreementService)
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), eq(legalEntityId));
        verify(persistenceServiceAgreementService, times(0)).addParticipant(any(PresentationParticipantPutBody.class));
        verify(persistenceServiceAgreementService, times(0))
            .save(any(LegalEntity.class), any(NewMasterServiceAgreement.class));
    }

    @Test
    public void testShouldCreateLegalEntityAsParticipantOfNewMasterServiceAgreement() {
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
            .withLegalEntityName("le-test")
            .withLegalEntityExternalId("le-ext-id")
            .withLegalEntityType(
                com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.BANK)
            .withParticipantOf(new ParticipantOf()
                .withNewMasterServiceAgreement(new NewMasterServiceAgreement()
                    .withServiceAgreementName("sa-name"))
                .withNewCustomServiceAgreement(new NewCustomServiceAgreement().withParticipantInfo(
                    new ParticipantInfo().withShareAccounts(true).withShareUsers(true)
                )));

        when(legalEntityJpaRepository.existsByExternalId(any())).thenReturn(false);
        String parentExternalId = "RootLE";
        LegalEntity parentLe =
            createLegalEntity(parentExternalId, "rootLe", null, LegalEntityType.BANK);
        LegalEntity legalEntity =
            createLegalEntity("le-ext-id", "le-test", parentLe, LegalEntityType.BANK);
        when(legalEntityJpaRepository.save(any(LegalEntity.class))).thenReturn(legalEntity);

        ServiceAgreement serviceAgreement = new ServiceAgreement().withId(UUID.randomUUID().toString());
        when(persistenceServiceAgreementService
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), any()))
            .thenReturn(serviceAgreement);

        LegalEntityAsParticipantPostResponseBody response = persistenceLegalEntityService
            .createLegalEntityAsParticipant(requestBody, "creator-le-id");
        assertNotNull(response.getLegalEntityId());
        assertEquals(serviceAgreement.getId(), response.getServiceAgreementId());
        verify(persistenceServiceAgreementService)
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), any());
        verify(persistenceServiceAgreementService, times(0)).addParticipant(any(PresentationParticipantPutBody.class));
        verify(persistenceServiceAgreementService, times(1))
            .save(any(LegalEntity.class), any(NewCustomServiceAgreement.class), anyString());

    }

    private LegalEntity createLegalEntity(String externalId, String legalEntityName, LegalEntity parentLegalEntity,
        LegalEntityType type) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(UUID.randomUUID().toString());
        legalEntity.setExternalId(externalId);
        legalEntity.setName(legalEntityName);
        legalEntity.setParent(parentLegalEntity);
        legalEntity.setType(type);
        return legalEntity;
    }

    private LegalEntitiesPostRequestBody createAddLegalEntityPostRequestBody(String externalId, String name,
        String parentExternalId,
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType type,
        boolean enabledMasterServiceAgreement) {
        return new LegalEntitiesPostRequestBody()
            .withExternalId(externalId)
            .withName(name)
            .withParentExternalId(parentExternalId)
            .withType(type)
            .withActivateSingleServiceAgreement(enabledMasterServiceAgreement);
    }
}
