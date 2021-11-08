package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT;
import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS;
import static com.backbase.accesscontrol.matchers.LegalEntityMatcher.getLegalEntityMatcher;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUtil;
import org.assertj.core.util.Sets;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public class LegalEntityJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    private RepositoryCleaner repositoryCleaner;
    @Autowired
    private BusinessFunctionCache businessFunctionCache;
    @Autowired
    private UserContextJpaRepository userContextJpaRepository;
    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private LegalEntity bank;
    private LegalEntity bank2;
    private PersistenceUtil persistenceUtil;

    private LegalEntity companyAUnderBank;
    private LegalEntity companyBUnderBank;
    private LegalEntity customerDataGroupLegalEntityBank;
    private LegalEntity companyA1UnderA;


    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();

        persistenceUtil = Persistence.getPersistenceUtil();
        bank = createLegalEntity(null, "bank", "bank", null, LegalEntityType.BANK);
        HashMap<String, String> additions = new HashMap<>();
        additions.put("leExternalId", "asdads");
        additions.put("second", "asdads");
        bank.setAdditions(additions);
        bank = legalEntityJpaRepository.save(bank);

        companyAUnderBank = createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", bank,
            LegalEntityType.CUSTOMER);
        companyAUnderBank.setAdditions(additions);
        companyBUnderBank = createLegalEntity(null, "companyBUnderBank", "companyBUnderBank", bank,
            LegalEntityType.CUSTOMER);
        companyBUnderBank.setAdditions(additions);
        companyA1UnderA = createLegalEntity(null, "companyA1UnderA", "companyA1UnderA", companyAUnderBank,
            LegalEntityType.CUSTOMER);
        companyA1UnderA.setAdditions(additions);

        companyAUnderBank = legalEntityJpaRepository.save(companyAUnderBank);
        companyBUnderBank = legalEntityJpaRepository.save(companyBUnderBank);
        companyA1UnderA = legalEntityJpaRepository.save(companyA1UnderA);

        flushAndClearSession();
    }

    @Test
    @Transactional
    public void shouldFindNullForNoEntity() {
        legalEntityJpaRepository.deleteAll();
        assertFalse(legalEntityJpaRepository.findById(bank.getId()).isPresent());

    }

    @Test
    @Transactional
    public void shouldPersist() {
        LegalEntity legalEntity = createLegalEntity(null, "name", "external1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        assertThat(legalEntity,
            getLegalEntityMatcher(is(notNullValue(LegalEntity.class)), is("external1"), is("name"),
                is(nullValue(LegalEntity.class)), is(empty()), is(empty()))
        );
    }


    @Test
    @Transactional
    public void shouldLookForNotExistingEntity() {
        assertFalse(
            legalEntityJpaRepository.findByExternalId("external1", GRAPH_LEGAL_ENTITY_WITH_ADDITIONS).isPresent());
    }

    @Test
    @Transactional
    public void shouldGetByExternalId() {
        legalEntityJpaRepository.save(createLegalEntity(null, "name", "external1", null, LegalEntityType.BANK));
        assertNotNull(legalEntityJpaRepository.findByExternalId("external1", GRAPH_LEGAL_ENTITY_WITH_ADDITIONS));
    }

    @Test
    @Transactional
    public void shouldGetById() {
        LegalEntity legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "name", "external1", null, LegalEntityType.BANK));

        Optional<LegalEntity> byId = legalEntityJpaRepository.findById(legalEntity.getId());

        assertNotNull(byId.get());
        assertEquals("name", byId.get().getName());
    }


    @Test
    @Transactional
    public void shouldGetByIdWithChildrenAndParentAndAncestors() {
        LegalEntity legalEntityParent = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameParent", "externalParent", null, LegalEntityType.BANK));
        LegalEntity legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "name", "external1", legalEntityParent, LegalEntityType.CUSTOMER));
        legalEntityJpaRepository
            .save(createLegalEntity(null, "nameChild", "external1Child", legalEntity, LegalEntityType.CUSTOMER));

        flushAndClearSession();
        Optional<LegalEntity> legalEntityById = legalEntityJpaRepository
            .findById(legalEntity.getId(), GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS);
        flushAndClearSession();

        assertNotNull(legalEntityById.get());
        assertEquals(legalEntityParent, legalEntityById.get().getParent());
    }

    @Test
    @Transactional
    public void shouldCreateValidHierarchy() {
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository
            .findByExternalId("companyA1UnderA", GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
        assertTrue(legalEntityOptional.isPresent());
        legalEntityOptional.ifPresent(
            legalEntity -> assertThat(legalEntity,
                getLegalEntityMatcher(is(notNullValue(LegalEntity.class)), is("companyA1UnderA"), is("companyA1UnderA"),
                    is(notNullValue(LegalEntity.class)), is(empty()), hasSize(2))
            )
        );
    }

    @Test
    @Transactional
    public void shouldLoadAdditions() {
        entityManager.flush();
        entityManager.clear();
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository
            .findByExternalId("companyA1UnderA", GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
        assertTrue(legalEntityOptional.isPresent());
        legalEntityOptional.ifPresent(
            legalEntity -> {
                assertTrue(persistenceUtil.isLoaded(legalEntity, "additions"));
                assertFalse(persistenceUtil.isLoaded(legalEntity, "legalEntityAncestors"));
            }
        );
    }

    @Test
    @Transactional
    public void shouldNotLoadAdditions() {
        entityManager.flush();
        entityManager.clear();
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository.findByExternalId("companyA1UnderA", null);
        assertTrue(legalEntityOptional.isPresent());
        legalEntityOptional.ifPresent(
            legalEntity -> {
                assertFalse(persistenceUtil.isLoaded(legalEntity, "additions"));
                assertFalse(persistenceUtil.isLoaded(legalEntity, "legalEntityAncestors"));
            }
        );
    }

    @Test
    @Transactional
    public void shouldLoadAncestorsAndAdditions() {
        entityManager.flush();
        entityManager.clear();
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository
            .findByExternalId("companyA1UnderA", GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS);
        assertTrue(legalEntityOptional.isPresent());
        legalEntityOptional.ifPresent(
            legalEntity -> {
                assertTrue(persistenceUtil.isLoaded(legalEntity, "additions"));
                assertTrue(persistenceUtil.isLoaded(legalEntity, "legalEntityAncestors"));
            }
        );
    }

    @Test
    @Transactional
    public void shouldCreateValidBankWithChildren() {
        Optional<LegalEntity> legalEntityOptional = legalEntityJpaRepository
            .findByExternalId("bank", GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
        assertTrue(legalEntityOptional.isPresent());
        legalEntityOptional.ifPresent(
            legalEntity -> assertThat(legalEntity,
                getLegalEntityMatcher(is(notNullValue(LegalEntity.class)), is("bank"), is("bank"),
                    is(nullValue(LegalEntity.class)), hasSize(2), is(empty()))
            )
        );
    }

    @Test
    @Transactional
    public void shouldReturnAllLegalEntitiesForBankParentId() {
        List<LegalEntity> legalEntityList = legalEntityJpaRepository.findDistinctByParentId(bank.getId());
        assertThat(legalEntityList, hasSize(2));
        Matcher<?> bankId = is(hasProperty("id", is(bank.getId())));
        assertThat(legalEntityList,
            hasItems(
                getLegalEntityMatcher(is(notNullValue(LegalEntity.class)), is("companyAUnderBank"),
                    is("companyAUnderBank"), bankId, hasSize(1), hasSize(1)),
                getLegalEntityMatcher(is(notNullValue(LegalEntity.class)), is("companyBUnderBank"),
                    is("companyBUnderBank"), bankId, hasSize(0), hasSize(1))
            )
        );
    }

    @Test
    @Transactional
    public void shouldGetChildrenLegalEntitiesByParentId() {
        List<LegalEntity> allByParent = legalEntityJpaRepository.findDistinctByParentId(bank.getId());
        assertEquals(2, allByParent.size());
    }

    @Test
    @Transactional
    public void shouldReturnLegalEntitiesWithoutParent() {
        List<LegalEntity> allByParentIsNull = legalEntityJpaRepository.findDistinctByParentIsNull();
        assertEquals(1, allByParentIsNull.size());
        assertEquals(bank.getId(), allByParentIsNull.get(0).getId());
    }

    @Test
    @Transactional
    public void shouldReturnLegalEntitiesWithParentIdNullWithChildren() {
        legalEntityJpaRepository.deleteAll();

        LegalEntity legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameParent", "externalParent", null, LegalEntityType.BANK));
        LegalEntity legalEntityChild1 = legalEntityJpaRepository
            .save(createLegalEntity(null, "name", "external1", legalEntity, LegalEntityType.CUSTOMER));
        LegalEntity legalEntityChild2 = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameChild", "external1Child", legalEntity, LegalEntityType.CUSTOMER));

        flushAndClearSession();
        List<LegalEntity> allByParentIsNull = legalEntityJpaRepository
            .findDistinctByParentIsNull(GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT);
        flushAndClearSession();

        assertEquals(1, allByParentIsNull.size());
        assertThat(allByParentIsNull.get(0).getChildren(), containsInAnyOrder(legalEntityChild1, legalEntityChild2));
    }

    @Test
    @Transactional
    public void shouldReturnListLegalEntitiesByExternalIds() {
        List<LegalEntity> legalEntities = legalEntityJpaRepository
            .findDistinctByExternalIdIn(asList(bank.getExternalId(), "companyAUnderBank"),
                GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);
        assertEquals(2, legalEntities.size());
    }

    @Test
    @Transactional
    public void shouldNotLoadAdditionsWhenReturningListOfLegalEntitiesByExternalIds() {
        entityManager.flush();
        entityManager.clear();
        List<LegalEntity> legalEntityList = legalEntityJpaRepository
            .findDistinctByExternalIdIn(asList(bank.getExternalId(), "companyAUnderBank"), null);

        legalEntityList.forEach(legalEntity -> {
            assertFalse(persistenceUtil.isLoaded(legalEntity, "additions"));
            assertFalse(persistenceUtil.isLoaded(legalEntity, "legalEntityAncestors"));
        });
    }

    @Test
    @Transactional
    public void shouldReturnOnlyBankUnderBank() {
        LegalEntity bankUnderBank = createLegalEntity(null, "bankUnderBank", "bankUnderBank", bank,
            LegalEntityType.BANK);
        legalEntityJpaRepository.saveAndFlush(bankUnderBank);
        List<LegalEntity> allByParentIdAndType = legalEntityJpaRepository
            .findAllByParentIdAndType(bank.getId(), LegalEntityType.BANK);
        assertThat(allByParentIdAndType, hasSize(1));
        assertThat(bankUnderBank, in(allByParentIdAndType));
    }

    @Test
    @Transactional
    public void shouldReturnOnlyCustomrsUnderBank() {
        LegalEntity bankUnderBank = createLegalEntity(null, "bankUnderBank", "bankUnderBank", bank,
            LegalEntityType.BANK);
        legalEntityJpaRepository.saveAndFlush(bankUnderBank);
        List<LegalEntity> allByParentIdAndType = legalEntityJpaRepository
            .findAllByParentIdAndType(bank.getId(), LegalEntityType.CUSTOMER);
        assertThat(allByParentIdAndType, hasSize(2));
        assertThat(bankUnderBank, not(in(allByParentIdAndType)));
    }

    @Test
    @Transactional
    public void shouldReturnNoChildren() {
        LegalEntity bankUnderBank = createLegalEntity(null, "bankUnderBank", "bankUnderBank", bank,
            LegalEntityType.BANK);
        legalEntityJpaRepository.saveAndFlush(bankUnderBank);
        List<LegalEntity> allByParentIdAndType = legalEntityJpaRepository
            .findAllByParentIdAndType(bankUnderBank.getId(), LegalEntityType.CUSTOMER);
        assertThat(allByParentIdAndType, hasSize(0));
    }

    @Test
    @Transactional
    public void shouldFindAllSubEntities() {
        String query = "com";
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 2, query,
            null);
        Page<LegalEntity> retrievedPage = legalEntityJpaRepository.findAllSubEntities(
            bank.getId(), searchAndPaginationParameters, null, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);

        assertEquals(3, retrievedPage.getTotalElements());
        assertEquals(2, retrievedPage.getContent().size());
        List<String> retrievedLegalEntityIds = retrievedPage.getContent()
            .stream()
            .map(LegalEntity::getId)
            .collect(Collectors.toList());
        assertTrue(retrievedLegalEntityIds.containsAll(asList(companyA1UnderA.getId(), companyAUnderBank.getId())));

    }


    @Test
    @Transactional
    public void shouldFindAllSubEntitiesWithExcludeBankIds() {
        String query = null;
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 5, query,
            null);
        Set<String> ids = new HashSet<>();
        ids.add(bank.getId());

        Page<LegalEntity> retrievedPage = legalEntityJpaRepository.findAllSubEntities(
            bank.getId(), searchAndPaginationParameters, ids, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);

        assertEquals(3, retrievedPage.getTotalElements());
        assertEquals(3, retrievedPage.getContent().size());
        List<String> retrievedLegalEntityIds = retrievedPage.getContent()
            .stream()
            .map(LegalEntity::getId)
            .collect(Collectors.toList());
        assertTrue(retrievedLegalEntityIds.containsAll(asList(companyA1UnderA.getId(), companyAUnderBank.getId())));

    }

    @Test
    @Transactional
    public void shouldFindAllSubEntitiesWithExcludeLeId() {
        String query = null;
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 5, query,
            null);
        Set<String> ids = new HashSet<>();
        ids.add(companyA1UnderA.getId());

        Page<LegalEntity> retrievedPage = legalEntityJpaRepository.findAllSubEntities(
            bank.getId(), searchAndPaginationParameters, ids, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);

        assertEquals(3, retrievedPage.getTotalElements());
        assertEquals(3, retrievedPage.getContent().size());
        List<String> retrievedLegalEntityIds = retrievedPage.getContent()
            .stream()
            .map(LegalEntity::getId)
            .collect(Collectors.toList());
        assertEquals(3, retrievedLegalEntityIds.size());
        assertTrue(retrievedLegalEntityIds.containsAll(asList(bank.getId())));
        assertTrue(!retrievedLegalEntityIds.containsAll(asList(companyA1UnderA.getId())));
    }

    @Test
    @Transactional
    public void shouldFindByIdInAndNameLikeIgnoreCaseOrderByNameOutOfRange() {
        String query = "com";
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(4, 10, query,
            null);
        Page<LegalEntity> retrievedPage = legalEntityJpaRepository.findAllSubEntities(
            bank.getId(), searchAndPaginationParameters, null, GRAPH_LEGAL_ENTITY_WITH_ADDITIONS);

        assertEquals(3, retrievedPage.getTotalElements());
        assertEquals(0, retrievedPage.getContent().size());
    }

    @Test
    @Transactional
    public void findAllLegalEntitiesSegmentation() {
        bank2 = createLegalEntity(null, "bank2", "bank2", null, LegalEntityType.BANK);
        HashMap<String, String> additions = new HashMap<>();
        additions.put("leExternalId", "asdads");
        additions.put("second", "asdads");
        bank2.setAdditions(additions);
        bank2 = legalEntityJpaRepository.saveAndFlush(bank2);
        customerDataGroupLegalEntityBank = createLegalEntity(null, "customerDataGroupLegalEntityBank",
            "bankExternalId", bank2,
            LegalEntityType.CUSTOMER);
        customerDataGroupLegalEntityBank.setAdditions(additions);
        customerDataGroupLegalEntityBank = legalEntityJpaRepository.saveAndFlush(customerDataGroupLegalEntityBank);

        // create SA
        ServiceAgreement serviceAgreement = createServiceAgreement(
            "BB between self", "id.external", "desc", customerDataGroupLegalEntityBank,
            customerDataGroupLegalEntityBank.getId(),
            customerDataGroupLegalEntityBank.getId());
        serviceAgreement = serviceAgreementJpaRepository.saveAndFlush(serviceAgreement);

        DataGroup dataGroup = DataGroupUtil
            .createDataGroup("name2", "CUSTOMERS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Collections.singleton(customerDataGroupLegalEntityBank.getId()));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        ApplicableFunctionPrivilege viewLe = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, viewLe, null);
        FunctionGroup testFg = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        UserContext userContext = userContextJpaRepository
            .saveAndFlush(new UserContext("user", serviceAgreement.getId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(testFg, userContext);

        userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup);
        Set<String> dgSet = new HashSet<>();
        dgSet.add(dataGroup.getId());
        userAssignedCombinationRepository
            .saveAndFlush(new UserAssignedFunctionGroupCombination(dgSet, userAssignedFunctionGroup));

        String query = "bankExternalId";
        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 5, query,
            null);
        GetLegalEntitySegmentationHolder holder = new GetLegalEntitySegmentationHolder()
            .withPrivilege("view")
            .withBusinessFunction("Entitlements")
            .withSearchAndPaginationParameters(searchAndPaginationParameters);
        HashSet<String> dataGroupIds = Sets.newHashSet(Collections.singletonList(dataGroup.getId()));
        Optional<LegalEntity> first = legalEntityJpaRepository
            .findAllLegalEntitiesSegmentation(
                searchAndPaginationParameters,
                dataGroupIds,
                GRAPH_LEGAL_ENTITY_WITH_ADDITIONS)
            .get()
            .findFirst();
        if (first.isPresent()) {
            LegalEntity data = first.get();
            assertEquals("bankExternalId", data.getExternalId());
        }
    }

    @Test
    @Transactional
    public void findByLegalEntityAncestorsIdAndIdIn() {
        LegalEntity legalEntityParent = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameParent", "externalParent", null, LegalEntityType.BANK));
        LegalEntity legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "name", "external1", legalEntityParent, LegalEntityType.CUSTOMER));
        LegalEntity legalEntity2 = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameChild", "external1Child", legalEntity, LegalEntityType.CUSTOMER));

        List<String> participants = new ArrayList<>();
        participants.add(legalEntity.getId());
        participants.add(legalEntity2.getId());
        flushAndClearSession();
        List<IdProjection> result = legalEntityJpaRepository
            .findByLegalEntityAncestorsIdAndIdIn(legalEntityParent.getId(), participants);
        flushAndClearSession();

        assertEquals(2, result.size());
    }

    @Test
    @Transactional
    public void shouldGetByLegalEntityAncestorsIdInAndId() {
        LegalEntity legalEntityParent = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameParent", "externalParent", null, LegalEntityType.BANK));
        LegalEntity legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "name", "external1", legalEntityParent, LegalEntityType.CUSTOMER));
        LegalEntity legalEntity2 = legalEntityJpaRepository
            .save(createLegalEntity(null, "nameChild", "external1Child", legalEntity, LegalEntityType.CUSTOMER));
        List<String> participants = new ArrayList<>();
        participants.add(legalEntityParent.getId());
        participants.add(legalEntity.getId());
        flushAndClearSession();
        int result = legalEntityJpaRepository
            .countByLegalEntityAncestorsIdInAndId(participants, legalEntity2.getId());
        flushAndClearSession();
        assertEquals(2, result);
    }

    @Test
    @Transactional
    public void findInternalByExternalIds() {
        legalEntityJpaRepository.save(createLegalEntity(null, "name", "external1", null, LegalEntityType.BANK));
        assertNotNull(legalEntityJpaRepository.findByExternalIdIn(Collections.singleton("external1")));
    }


    private void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }
}