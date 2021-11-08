package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR;
import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.CUSTOM;
import static com.backbase.accesscontrol.matchers.ServiceAgreementMatcher.getServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUtil;
import javax.persistence.Tuple;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;


public class ServiceAgreementJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Autowired
    private UserContextJpaRepository userContextJpaRepository;
    @Autowired
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Autowired
    private RepositoryCleaner repositoryCleaner;
    @Autowired
    private BusinessFunctionCache businessFunctionCache;
    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private ServiceAgreement serviceAgreement1;
    private ServiceAgreement serviceAgreement2;
    private ServiceAgreement serviceAgreement3;

    private LegalEntity serviceAgreementLegalEntity1;
    private LegalEntity serviceAgreementLegalEntity2;

    private Map<String, String> additions = new HashMap<>();
    private PersistenceUtil persistenceUtil;

    @Before
    public void setUp() {
        repositoryCleaner.clean();
        persistenceUtil = Persistence.getPersistenceUtil();
        additions.put("externalId", "adadsa");
        additions.put("second", "adadsa");
    }

    @Test
    @Transactional
    public void shouldFindByIdWithNamedEntityGraph() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();

        Optional<ServiceAgreement> returnedById = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_EXTENDED);
        flushAndClearSession();

        assertTrue(returnedById.isPresent());
        assertThat(serviceAgreement,
            getServiceAgreementMatcher(equalTo(returnedById.get().getId()), equalTo(returnedById.get().getName()),
                equalTo(returnedById.get().getDescription()),
                hasProperty("id", equalTo(returnedById.get().getCreatorLegalEntity().getId()))));
    }

    @Test
    @Transactional
    public void shouldSaveAndFindByIdWithAssignablePermissions() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        Set<AssignablePermissionSet> assignablePermissionSetsRegular = Sets
            .newHashSet(assignablePermissionSetJpaRepository
                .findFirstByType(AssignablePermissionType.ADMIN_USER_DEFAULT.getValue()).get());
        Set<AssignablePermissionSet> assignablePermissionSetsAdmin = Sets
            .newHashSet(assignablePermissionSetJpaRepository
                .findFirstByType(AssignablePermissionType.REGULAR_USER_DEFAULT.getValue()).get());
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);
        serviceAgreement.setPermissionSetsAdmin(assignablePermissionSetsAdmin);
        serviceAgreement.setPermissionSetsRegular(assignablePermissionSetsRegular);

        serviceAgreementJpaRepository.saveAndFlush(serviceAgreement);

        Optional<ServiceAgreement> returnedById = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId());
        flushAndClearSession();

        assertTrue(returnedById.isPresent());

    }

    @Test
    @Transactional
    public void shouldLoadAdditionsProvidersAndCreatorLegalEntity() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "Backbase2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);

        String userId = "User-001";
        String userId2 = "User-002";

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity, asList(userId, userId2),
            Collections.emptyList(), true, true);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity2, Collections.emptyList(),
            Collections.emptyList(), true, false);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();

        Optional<ServiceAgreement> returnedById = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_EXTENDED);
        flushAndClearSession();

        assertTrue(returnedById.isPresent());

        assertTrue(persistenceUtil.isLoaded(returnedById.get(), "additions"));
        assertTrue(persistenceUtil.isLoaded(returnedById.get(), "providers"));
        assertTrue(persistenceUtil.isLoaded(returnedById.get(), "creatorLegalEntity"));
    }

    @Test
    @Transactional
    public void shouldPrefetchParticipantsAndCreator() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        String legalEntityId = legalEntityJpaRepository.save(legalEntity).getId();
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity, Collections.emptyList(),
            Collections.emptyList(), true, true);
        String serviceAgreementId = serviceAgreementJpaRepository.save(serviceAgreement).getId();
        flushAndClearSession();

        serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR).get();

        assertTrue(persistenceUtil.isLoaded(serviceAgreementId, "providers"));
        assertTrue(persistenceUtil.isLoaded(serviceAgreementId, "creatorLegalEntity"));
        assertTrue(
            persistenceUtil.isLoaded(serviceAgreement.getParticipants().get(legalEntityId).getId(), "legalEntity"));
    }

    @Test
    @Transactional
    public void shouldLoadAdditionsAndCreatorLegalEntity() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "Backbase2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity, Collections.emptyList(),
            Collections.emptyList(), true, true);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity2, Collections.emptyList(),
            Collections.emptyList(), true, false);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();
        Optional<ServiceAgreement> returnedById = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_WITH_ADDITIONS);
        flushAndClearSession();

        assertTrue(returnedById.isPresent());

        assertTrue(persistenceUtil.isLoaded(returnedById.get(), "additions"));
        assertFalse(persistenceUtil.isLoaded(returnedById.get(), "participants"));
        assertTrue(persistenceUtil.isLoaded(returnedById.get(), "creatorLegalEntity"));
    }

    @Test
    @Transactional
    public void shouldLoadServiceAgreementWith() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "Backbase2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exId", "description", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity, Collections.emptyList(),
            Collections.emptyList(), true, true);
        addParticipantToServiceAgreement(serviceAgreement, legalEntity2, Collections.emptyList(),
            Collections.emptyList(), true, false);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();
        Optional<ServiceAgreement> returnedById = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), null);
        flushAndClearSession();

        assertTrue(returnedById.isPresent());

        assertFalse(persistenceUtil.isLoaded(returnedById.get(), "additions"));
        assertFalse(persistenceUtil.isLoaded(returnedById.get(), "participants"));
        assertFalse(persistenceUtil.isLoaded(returnedById.get(), "creatorLegalEntity"));
    }


    @Test
    @Transactional
    public void shouldFindMasterServiceAgreementByCreatorLegalEntityAndIsMaster() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "ex.id", "description", legalEntity, null,
            null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();

        Optional<ServiceAgreement> returnedServiceAgreement = serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(serviceAgreement.getCreatorLegalEntity().getId(),
                serviceAgreement.isMaster(), SERVICE_AGREEMENT_WITH_ADDITIONS);
        flushAndClearSession();

        assertTrue(returnedServiceAgreement.isPresent());
        assertThat(serviceAgreement, getServiceAgreementMatcher(equalTo(returnedServiceAgreement.get().getId()),
            equalTo(returnedServiceAgreement.get().getName()),
            equalTo(returnedServiceAgreement.get().getDescription()),
            hasProperty("id", equalTo(returnedServiceAgreement.get().getCreatorLegalEntity().getId()))));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWithCreatorId() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "LE2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity2, null, null);

        serviceAgreement1.setAdditions(additions);
        serviceAgreement2.setAdditions(additions);
        serviceAgreement3.setAdditions(additions);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        Page<ServiceAgreement> result1 = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, null, null), null);

        Page<ServiceAgreement> result2 = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity2.getId(),
                new SearchAndPaginationParameters(null, null, null, null), null);

        Page<ServiceAgreement> result3 = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, "nonexisting",
                new SearchAndPaginationParameters(null, null, null, null), null);

        assertEquals(2, result1.getContent().size());
        assertThat(result1.getContent(), containsInAnyOrder(serviceAgreement1, serviceAgreement2));
        assertEquals(1, result2.getContent().size());
        assertEquals(serviceAgreement3, result2.getContent().get(0));
        assertEquals(0, result3.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhenSearchParameterFindByName() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        Page<ServiceAgreement> findByName = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, "SA1", null), null);

        assertEquals(1L, findByName.getTotalElements());
        assertEquals(1, findByName.getContent().size());
        assertEquals(serviceAgreement1, findByName.getContent().get(0));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhenSearchParameterFindByDescription() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        Page<ServiceAgreement> findByDescription = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, "desc3", null), null);

        assertEquals(1L, findByDescription.getTotalElements());
        assertEquals(1, findByDescription.getContent().size());
        assertEquals(serviceAgreement3, findByDescription.getContent().get(0));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhenSearchParameterFindByDescriptionAndName() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Sa4", "id.ex4", "desc4", legalEntity1, null, null);
        ServiceAgreement serviceAgreement5 = createServiceAgreement("Name", "id.ex5", "saDescription", legalEntity1,
            null, null);
        ServiceAgreement serviceAgreement6 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);
        serviceAgreementJpaRepository.save(serviceAgreement5);
        serviceAgreementJpaRepository.save(serviceAgreement6);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, "SA", null), null);

        assertEquals(5L, findByNameAndDescription.getTotalElements());
        assertEquals(5, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            containsInAnyOrder(serviceAgreement1, serviceAgreement2, serviceAgreement3, serviceAgreement4,
                serviceAgreement5));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhenSearchByNotExisting() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        Page<ServiceAgreement> notExistingSearchParameter = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, "notExisting", null), null);

        assertEquals(0L, notExistingSearchParameter.getTotalElements());
        assertEquals(0, notExistingSearchParameter.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhitPagination() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(0, 2, "SA", null), null);

        assertEquals(3L, findByNameAndDescription.getTotalElements());
        assertEquals(2, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            contains(serviceAgreement1, serviceAgreement2));
    }

    @Test
    @Transactional
    public void shouldFindOneServiceAgreementsWhitPagination() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(1, 1, "SA", null), null);

        assertEquals(3L, findByNameAndDescription.getTotalElements());
        assertEquals(1, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            contains(serviceAgreement2));
    }

    @Test
    @Transactional
    public void shouldNotReturnMasterServiceAgreementWhenGetByCreator() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement masterSA = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        masterSA.setMaster(true);
        ServiceAgreement customSA = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(masterSA);
        serviceAgreementJpaRepository.save(customSA);

        Page<ServiceAgreement> response = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(null, legalEntity1.getId(),
                new SearchAndPaginationParameters(null, null, null, null), null);

        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertThat(response.getContent(),
            contains(customSA));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWithName() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "LE2", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity2, null, null);

        serviceAgreement1.setAdditions(additions);
        serviceAgreement2.setAdditions(additions);
        serviceAgreement3.setAdditions(additions);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        Page<ServiceAgreement> result1 = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters("SA1", null,
                new SearchAndPaginationParameters(null, null, null, null), null);

        Page<ServiceAgreement> result2 = serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters("nonexisting", null,
                new SearchAndPaginationParameters(null, null, null, null), null);

        assertEquals(1, result1.getContent().size());
        assertEquals(0, result2.getContent().size());
    }

    @Test
    @Transactional
    public void testSaveServiceAgreementWithAdditionalProperties() {
        LegalEntity legalEntity = createLegalEntity(null, "EX23", "LE23", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        ServiceAgreement serviceAgreement = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity, null, null);
        serviceAgreement.setAdditions(additions);

        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();

        Optional<ServiceAgreement> savedAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_EXTENDED);
        assertTrue(savedAgreement.isPresent());
        assertEquals(2, savedAgreement.get().getAdditions().size());
        assertEquals(additions.get("second"), savedAgreement.get().getAdditions().get("second"));
    }


    private void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }


    @Test
    @Transactional
    public void shouldFindServiceAgreementsWithCaseInsensitiveWithUserIdPage0() {
        setupSAListForPagination();
        Page<ServiceAgreement> result = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions("6", "", getPageableObjWithoutSortingObject(0, 7));

        assertEquals(7, result.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWithCaseInsensitiveWithUserIdPage1() {
        setupSAListForPagination();
        Page<ServiceAgreement> result = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions("6", "", getPageableObjWithoutSortingObject(1, 7));

        assertEquals(3, result.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWithPermissionsIdAndEmptyQuery() {
        setupServiceAgreementsLegalEntitiesAndProviders();
        Page<ServiceAgreement> result1 = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions("6", "", getPageableObjWithoutSortingObject(1, 10));
        assertEquals(1, result1.getTotalElements());
    }


    @Test
    @Transactional
    public void testValidServiceAgreementSelection() {
        String userId = "User-001";

        String legalEntityName1 = "service agreement legal entity 1";
        String legalEntityName2 = "service agreement legal entity 2";

        LegalEntity providerLE = createAndSaveLegalEntity(legalEntityName1, "providerId", null);
        LegalEntity consumerLE = createAndSaveLegalEntity(legalEntityName2, "consumerId", null);
        LegalEntity providerAndConsumerLE = createAndSaveLegalEntity("participant", "participantId", null);
        LegalEntity creator = createAndSaveLegalEntity("creator", "creatorId", null);

        ServiceAgreement serviceAgreementWithProviderAndPermissions = createServiceAgreement("sa1", "id.ex1", "desc1",
            creator, null, null);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, providerLE, asList(userId),
            new ArrayList<>(), true, false);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, consumerLE, new ArrayList<>(),
            null, false, true);
        serviceAgreementJpaRepository.save(serviceAgreementWithProviderAndPermissions);

        addPermissionToServiceAgreement(userId, serviceAgreementWithProviderAndPermissions);

        ServiceAgreement disabledServiceAgreement = createServiceAgreement("sa4", "id.ex4", "desc1", creator, null,
            null);
        disabledServiceAgreement.setState(ServiceAgreementState.DISABLED);
        addParticipantToServiceAgreement(disabledServiceAgreement, providerLE, asList(userId), new ArrayList<>(), true,
            false);
        addParticipantToServiceAgreement(disabledServiceAgreement, consumerLE, new ArrayList<>(), null, false, true);
        serviceAgreementJpaRepository.save(disabledServiceAgreement);

        addPermissionToServiceAgreement(userId, disabledServiceAgreement);

        ServiceAgreement masterServiceAgreement = createServiceAgreement("sa2", "id.ex2", "desc2", creator, null, null);
        masterServiceAgreement.setMaster(true);
        serviceAgreementJpaRepository.save(masterServiceAgreement);

        addPermissionToServiceAgreement(userId, masterServiceAgreement);

        ServiceAgreement serviceAgreementWithoutPermissions = createServiceAgreement("sa3", "id.ex3", "desc3", creator,
            null, null);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            null, false, true);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            asList(userId), true, false);
        serviceAgreementJpaRepository.save(serviceAgreementWithoutPermissions);
        flushAndClearSession();

        assertTrue(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, masterServiceAgreement.getId()));

        assertFalse(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, disabledServiceAgreement.getId()));

        assertFalse(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, serviceAgreementWithoutPermissions.getId()));
        assertTrue(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, serviceAgreementWithProviderAndPermissions.getId()));
    }

    @Test
    @Transactional
    public void shouldNotListServiceAgreementThatIsDisabled() {
        String userId = "User-001";

        String legalEntityName1 = "service agreement legal entity 1";
        String legalEntityName2 = "service agreement legal entity 2";

        LegalEntity providerLE = createAndSaveLegalEntity(legalEntityName1, "providerId", null);
        LegalEntity consumerLE = createAndSaveLegalEntity(legalEntityName2, "consumerId", null);
        LegalEntity providerAndConsumerLE = createAndSaveLegalEntity("participant", "participantId", null);
        LegalEntity creator = createAndSaveLegalEntity("creator", "creatorId", null);

        ServiceAgreement serviceAgreementWithProviderAndPermissions = createServiceAgreement("sa1", "id.ex1", "desc1",
            creator, null, null);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, providerLE, asList(userId),
            new ArrayList<>(), true, false);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, consumerLE, new ArrayList<>(),
            null, false, true);
        serviceAgreementJpaRepository.save(serviceAgreementWithProviderAndPermissions);

        addPermissionToServiceAgreement(userId, serviceAgreementWithProviderAndPermissions);

        ServiceAgreement disabledServiceAgreement = createServiceAgreement("sa4", "id.ex4", "desc1", creator, null,
            null);
        disabledServiceAgreement.setState(ServiceAgreementState.DISABLED);
        addParticipantToServiceAgreement(disabledServiceAgreement, providerLE, asList(userId), new ArrayList<>(), true,
            false);
        addParticipantToServiceAgreement(disabledServiceAgreement, consumerLE, new ArrayList<>(), null, false, true);
        serviceAgreementJpaRepository.save(disabledServiceAgreement);
        addPermissionToServiceAgreement(userId, disabledServiceAgreement);

        ServiceAgreement serviceAgreementWithoutPermissions = createServiceAgreement("sa3", "id.ex3", "desc3", creator,
            null, null);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            null, false, true);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            asList(userId), true, false);
        serviceAgreementJpaRepository.save(serviceAgreementWithoutPermissions);
        flushAndClearSession();

        Page<ServiceAgreement> serviceAgreements = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions(userId, "", getPageableObjWithoutSortingObject(0, 10));

        List<String> serviceAgreementIds = serviceAgreements.getContent().stream()
            .map(ServiceAgreement::getId)
            .collect(Collectors.toList());
        assertTrue(serviceAgreementIds.contains(serviceAgreementWithProviderAndPermissions.getId()));
        assertFalse(serviceAgreementIds.contains(disabledServiceAgreement.getId()));
    }

    private void addPermissionToServiceAgreement(String userId, ServiceAgreement serviceAgreement) {

        UserContext userContext = userContextJpaRepository.save(new UserContext(userId,
            serviceAgreement.getId()));

        ApplicableFunctionPrivilege viewSa = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");

        GroupedFunctionPrivilege gfpViewSa
            = getGroupedFunctionPrivilege(null, viewSa, null);

        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, RandomStringUtils.randomAlphabetic(8), "function-group-description",
                getGroupedFunctionPrivileges(
                    gfpViewSa
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
    }

    @Test
    @Transactional
    public void testValidateUserContextWithoutLegalEntity() {
        String userId = "User-001";

        String legalEntityName1 = "service agreement legal entity 1";
        String legalEntityName2 = "service agreement legal entity 2";

        LegalEntity providerLE = createAndSaveLegalEntity(legalEntityName1, "providerId", null);
        LegalEntity consumerLE = createAndSaveLegalEntity(legalEntityName2, "consumerId", null);
        LegalEntity providerAndConsumerLE = createAndSaveLegalEntity("participant", "participantId", null);
        LegalEntity creator = createAndSaveLegalEntity("creator", "creatorId", null);

        ServiceAgreement serviceAgreementWithProviderAndPermissions = createServiceAgreement("sa1", "id.ex1", "desc1",
            creator, null, null);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, providerLE, asList(userId),
            new ArrayList<>(), true, false);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, consumerLE, new ArrayList<>(),
            null, false, true);
        serviceAgreementJpaRepository.save(serviceAgreementWithProviderAndPermissions);
        addPermissionToServiceAgreement(userId, serviceAgreementWithProviderAndPermissions);

        ServiceAgreement masterServiceAgreement = createServiceAgreement("sa2", "id.ex2", "desc2", creator, null, null);
        masterServiceAgreement.setMaster(true);
        serviceAgreementJpaRepository.save(masterServiceAgreement);
        addPermissionToServiceAgreement(userId, masterServiceAgreement);

        ServiceAgreement serviceAgreementWithoutPermissions = createServiceAgreement("sa3", "id.ex3", "desc3", creator,
            null, null);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            null, false, true);
        addParticipantToServiceAgreement(serviceAgreementWithoutPermissions, providerAndConsumerLE, new ArrayList<>(),
            asList(userId), true, false);
        serviceAgreementJpaRepository.save(serviceAgreementWithoutPermissions);
        flushAndClearSession();

        assertTrue(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, masterServiceAgreement.getId()));
        assertFalse(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, serviceAgreementWithoutPermissions.getId()));
        assertTrue(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, serviceAgreementWithProviderAndPermissions.getId()));
    }

    @Test
    @Transactional
    public void shouldReturnFalseWhenServiceAgreementAndPrivilegesAreDisabled() {
        String userId = "User-001";

        String legalEntityName1 = "service agreement legal entity 1";
        String legalEntityName2 = "service agreement legal entity 2";

        LegalEntity providerLE = createAndSaveLegalEntity(legalEntityName1, "providerId", null);
        LegalEntity consumerLE = createAndSaveLegalEntity(legalEntityName2, "consumerId", null);
        LegalEntity creator = createAndSaveLegalEntity("creator", "creatorId", null);

        ServiceAgreement serviceAgreementWithProviderAndPermissions = createServiceAgreement("sa1", "id.ex1", "desc1",
            creator, null, null);
        serviceAgreementWithProviderAndPermissions.setState(ServiceAgreementState.DISABLED);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, providerLE, asList(userId),
            new ArrayList<>(), true, false);
        addParticipantToServiceAgreement(serviceAgreementWithProviderAndPermissions, consumerLE, new ArrayList<>(),
            null, false, true);
        serviceAgreementJpaRepository.save(serviceAgreementWithProviderAndPermissions);
        addPermissionToServiceAgreement(userId, serviceAgreementWithProviderAndPermissions);
        flushAndClearSession();

        assertFalse(serviceAgreementJpaRepository
            .existContextForUserIdAndServiceAgreementId(userId, serviceAgreementWithProviderAndPermissions.getId()));
    }

    @Test
    @Transactional
    public void regularUserAndAdminShouldNotHaveAccessForExpiredDisabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.minusDays(5).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.DISABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserAndAdminShouldNotHaveAccessForExpiredEnabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.minusDays(5).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.ENABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserShouldNotHaveAccessAndAdminShouldHaveAccessForValidTimeBoundAndDisabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(10).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.DISABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserAndAdminShouldHaveAccessForValidTimeBoundAndEnabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.ENABLED);

        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserAndAdminShouldNotHaveAccessForFeatureTimeBoundAndDisabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.plusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(15).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.DISABLED);
        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserAndAdminShouldNotHaveAccessForFeatureTimeBoundAndEnabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.plusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(15).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, null, null, startDate, endDate,
            ServiceAgreementState.ENABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserShouldNotHaveAccessAndAdminShouldHaveAccessForFgExpiredAndSaValidTimeBoundAndEnabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date saStartDate = Date.from(localDateTIme.minusDays(15).atZone(ZoneId.systemDefault()).toInstant());
        Date saEndDate = Date.from(localDateTIme.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());

        Date fgStartDate = Date.from(localDateTIme.minusDays(14).atZone(ZoneId.systemDefault()).toInstant());
        Date fgEndDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, fgStartDate, fgEndDate, saStartDate,
            saEndDate,
            ServiceAgreementState.ENABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserShouldNotHaveAccessAndAdminShouldHaveAccessForFgExpiredAndSaValidTimeBoundAndDisabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date saStartDate = Date.from(localDateTIme.minusDays(15).atZone(ZoneId.systemDefault()).toInstant());
        Date saEndDate = Date.from(localDateTIme.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());

        Date fgStartDate = Date.from(localDateTIme.minusDays(14).atZone(ZoneId.systemDefault()).toInstant());
        Date fgEndDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, fgStartDate, fgEndDate, saStartDate,
            saEndDate,
            ServiceAgreementState.DISABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserShouldNotHaveAccessAndAdminShouldHaveAccessForFgFeaturedAndSaValidTimeBoundAndDisabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date saStartDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date saEndDate = Date.from(localDateTIme.plusDays(15).atZone(ZoneId.systemDefault()).toInstant());

        Date fgStartDate = Date.from(localDateTIme.plusDays(11).atZone(ZoneId.systemDefault()).toInstant());
        Date fgEndDate = Date.from(localDateTIme.plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, fgStartDate, fgEndDate, saStartDate,
            saEndDate,
            ServiceAgreementState.DISABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    @Test
    @Transactional
    public void regularUserShouldNotHaveAccessAndAdminShouldHaveAccessForFgFeaturedAndSaValidTimeBoundAndEnabledServiceAgreement() {
        String adminId = "admin";
        String userId = "user";

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date saStartDate = Date.from(localDateTIme.minusDays(10).atZone(ZoneId.systemDefault()).toInstant());
        Date saEndDate = Date.from(localDateTIme.plusDays(15).atZone(ZoneId.systemDefault()).toInstant());

        Date fgStartDate = Date.from(localDateTIme.plusDays(11).atZone(ZoneId.systemDefault()).toInstant());
        Date fgEndDate = Date.from(localDateTIme.plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        String saId = addPermissionsAndReturnServiceAgreementId(userId, adminId, fgStartDate, fgEndDate, saStartDate,
            saEndDate,
            ServiceAgreementState.ENABLED);

        assertFalse(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(userId, saId));
        assertTrue(serviceAgreementJpaRepository.existContextForUserIdAndServiceAgreementId(adminId, saId));
    }

    private String addPermissionsAndReturnServiceAgreementId(String userId, String adminId, Date fgStartDate,
        Date fgEndDate, Date saStartDate, Date saEndDate, ServiceAgreementState saState) {

        ApplicableFunctionPrivilege viewLe = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");

        LegalEntity legalEntity = legalEntityJpaRepository
            .saveAndFlush(createLegalEntity(null, "le-ex-id", "le-name", null, LegalEntityType.BANK));

        Participant adminParticipant = ServiceAgreementUtil.createParticipantWithAdmin(adminId, true, false);
        adminParticipant.addAdmin(adminId);
        adminParticipant.addParticipantUsers(Arrays.asList(userId));

        ServiceAgreement testSa = createServiceAgreement("BB between self", "id.external", "desc",
            legalEntity, legalEntity.getId(), legalEntity.getId());
        testSa.setState(saState);
        testSa.setStartDate(saStartDate);
        testSa.setEndDate(saEndDate);
        testSa.addParticipant(adminParticipant, legalEntity.getId(), true, false);
        adminParticipant.setServiceAgreement(testSa);
        serviceAgreementJpaRepository.saveAndFlush(testSa);

        //save function group
        GroupedFunctionPrivilege viewEntitlementsWithoutLimit = getGroupedFunctionPrivilege(null, viewLe,
            null);

        FunctionGroup systemFG = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "SYSTEM_FUNCTION_GROUP", "desc system fg",
                newHashSet(viewEntitlementsWithoutLimit), FunctionGroupType.SYSTEM, testSa));

        FunctionGroup testFg = functionGroupJpaRepository
            .saveAndFlush(getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithoutLimit
                ),
                FunctionGroupType.DEFAULT, testSa).withStartDate(fgStartDate).withEndDate(fgEndDate));

        UserContext testUserUc = userContextJpaRepository.saveAndFlush(new UserContext(userId, testSa.getId()));
        UserAssignedFunctionGroup testUafg = new UserAssignedFunctionGroup(testFg, testUserUc);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(testUafg);

        UserContext testAdminUc = userContextJpaRepository.saveAndFlush(new UserContext(adminId, testSa.getId()));

        UserAssignedFunctionGroup expiredDisabledAdminUafg = new UserAssignedFunctionGroup(systemFG, testAdminUc);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(expiredDisabledAdminUafg);

        return testSa.getId();
    }

    @Test
    @Transactional
    public void shouldFindNoServiceAgreementWithWrongQuery() {
        setupServiceAgreementsLegalEntitiesAndProviders();

        Page<ServiceAgreement> result1 = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions("6", "wrongpattern",
                getPageableObjWithoutSortingObject(1, 10));

        assertEquals(0, result1.getTotalElements());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsWhereUserHasPermissions() {
        setupServiceAgreementsLegalEntitiesAndProviders();

        Page<ServiceAgreement> result = serviceAgreementJpaRepository
            .findServiceAgreementsWhereUserHasPermissions("6", "", getPageableObjWithoutSortingObject(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Transactional
    public void shouldThrowExceptionWhenExternalIdIsTheSame() {
        LegalEntity legalEntity = createLegalEntity("name.le", "id.ex.le", null);
        legalEntityJpaRepository.saveAndFlush(legalEntity);
        flushAndClearSession();
        ServiceAgreement s1 = createServiceAgreement("name.sa1", "id.ex", "desc.sa1", legalEntity, null, null);
        ServiceAgreement s2 = createServiceAgreement("name.sa2", "id.ex", "desc.sa2", legalEntity, null, null);
        serviceAgreementJpaRepository.saveAndFlush(s1);
        flushAndClearSession();
        assertThrows(DataIntegrityViolationException.class,
            () -> serviceAgreementJpaRepository.saveAndFlush(s2));
    }

    @Test
    @Transactional
    public void shouldExistById() {
        LegalEntity legalEntity = createLegalEntity("name.le", "id.ex.le", null);
        legalEntityJpaRepository.saveAndFlush(legalEntity);
        flushAndClearSession();
        ServiceAgreement s1 = createServiceAgreement("name.sa1", "id.ex", "desc.sa1", legalEntity, null, null);
        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository.saveAndFlush(s1);
        flushAndClearSession();

        assertTrue(serviceAgreementJpaRepository.existsById(serviceAgreement.getId()));
    }

    @Test
    @Transactional
    public void shouldNotExistById() {
        assertFalse(serviceAgreementJpaRepository.existsById("SOME-ID-THAT-SHOULD-NOT-EXIST"));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsByCreatorIdInHierarchy() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);
        LegalEntity legalEntity2 = createLegalEntity(null, "EX2", "LE2", legalEntity1, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);
        LegalEntity legalEntity3 = createLegalEntity(null, "EX3", "LE3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity3);
        Participant participant1 = new Participant();
        participant1.setLegalEntity(legalEntity1);
        Participant participant2 = new Participant();
        participant2.setLegalEntity(legalEntity3);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        serviceAgreement1.addParticipant(participant1);
        serviceAgreement1.addParticipant(participant2);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        serviceAgreement2.setMaster(true);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity2, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("SA4", "id.ex4", "desc4", legalEntity3, null, null);

        serviceAgreement1.setAdditions(additions);
        serviceAgreement2.setAdditions(additions);
        serviceAgreement3.setAdditions(additions);
        serviceAgreement4.setAdditions(additions);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);

        Page<ServiceAgreement> result1 = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(null, null, null, null), null);

        Page<ServiceAgreement> result3 = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters("nonexisting", new UserParameters(null, null),
                new SearchAndPaginationParameters(null, null, null, null), null);

        assertEquals(3, result1.getContent().size());
        assertThat(result1.getContent(), containsInAnyOrder(serviceAgreement1, serviceAgreement2, serviceAgreement3));

        assertEquals(0, result3.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsByCreatorIdInWhenSearchParameterFindByName() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        Page<ServiceAgreement> findByName = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(null, null, "A1", null), null);

        assertEquals(1L, findByName.getTotalElements());
        assertEquals(1, findByName.getContent().size());
        assertEquals(serviceAgreement1, findByName.getContent().get(0));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsByCreatorIdInWhenSearchParameterFindByDescriptionAndName() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Sa4", "id.ex4", "desc4", legalEntity1, null, null);
        ServiceAgreement serviceAgreement5 = createServiceAgreement("Name", "id.ex5", "saDescription", legalEntity1,
            null, null);
        ServiceAgreement serviceAgreement6 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);
        serviceAgreementJpaRepository.save(serviceAgreement5);
        serviceAgreementJpaRepository.save(serviceAgreement6);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(null, null, "SA", null), null);

        assertEquals(5L, findByNameAndDescription.getTotalElements());
        assertEquals(5, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            containsInAnyOrder(serviceAgreement1, serviceAgreement2, serviceAgreement3, serviceAgreement4,
                serviceAgreement5));
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsByCreatorIdInWhenSearchByNotExisting() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        Page<ServiceAgreement> notExistingSearchParameter = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(null, null, "notExisting", null), null);

        assertEquals(0L, notExistingSearchParameter.getTotalElements());
        assertEquals(0, notExistingSearchParameter.getContent().size());
    }

    @Test
    @Transactional
    public void shouldFindServiceAgreementsByCreatorIdInWithPagination() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(0, 2, "SA", null), null);

        assertEquals(3L, findByNameAndDescription.getTotalElements());
        assertEquals(2, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            contains(serviceAgreement1, serviceAgreement2));
    }

    @Test
    @Transactional
    public void shouldFindOneServiceAgreementsByCreatorIdInWithPagination() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        ServiceAgreement serviceAgreement2 = createServiceAgreement("SA2", "id.ex2", "desc2", legalEntity1, null, null);
        ServiceAgreement serviceAgreement3 = createServiceAgreement("SA3", "id.ex3", "desc3", legalEntity1, null, null);
        ServiceAgreement serviceAgreement4 = createServiceAgreement("Name1", "id.ex6", "Description", legalEntity1,
            null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreementJpaRepository.save(serviceAgreement4);

        Page<ServiceAgreement> findByNameAndDescription = serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(legalEntity1.getId(), new UserParameters(null, null),
                new SearchAndPaginationParameters(1, 1, "SA", null), null);

        assertEquals(3L, findByNameAndDescription.getTotalElements());
        assertEquals(1, findByNameAndDescription.getContent().size());
        assertThat(findByNameAndDescription.getContent(),
            contains(serviceAgreement2));
    }

    @Test
    @Transactional
    public void shouldFindMasterServiceAgreement() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "ex.id", "description", legalEntity, null,
            null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClearSession();

        boolean exists = serviceAgreementJpaRepository
            .existsByCreatorLegalEntityIdAndIsMasterTrue(legalEntity.getId());
        assertTrue(exists);
    }

    @Test
    @Transactional
    public void shouldProvideMasterServiceAgreementNotFound() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement1);
        flushAndClearSession();

        boolean exists = serviceAgreementJpaRepository
            .existsByCreatorLegalEntityIdAndIsMasterTrue(legalEntity1.getId());
        assertFalse(exists);
    }

    @Test
    @Transactional
    public void shouldProvideMasterServiceAgreementEmptyOptional() {
        LegalEntity legalEntity1 = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity1, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement1);
        flushAndClearSession();

        Optional<ServiceAgreement> master = serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(legalEntity1.getId(), true, null);
        assertFalse(master.isPresent());
    }

    @Test
    @Transactional
    public void shouldRetrieveFunctionAndDataGroups() {
        ApplicableFunctionPrivilege viewLe = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");

        LegalEntity legalEntity = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity, null, null);
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "fn name", "description", new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);
        functionGroup.setPermissions(newHashSet(
            GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, viewLe, functionGroup)));

        serviceAgreement.setFunctionGroups(newHashSet(functionGroup));

        serviceAgreement.setDataGroups(newHashSet(DataGroupUtil
            .createDataGroup("dg name", "ARRANGEMENTS", "description", serviceAgreement)));

        serviceAgreementJpaRepository.save(serviceAgreement);

        String saID = serviceAgreement.getId();

        entityManager.flush();
        entityManager.clear();

        Optional<ServiceAgreement> res = serviceAgreementJpaRepository
            .findById(saID,
                GraphConstants
                    .SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS);

        assertTrue(res.isPresent());
        res.ifPresent(returnedSA -> {
            assertTrue(persistenceUtil.isLoaded(returnedSA, "functionGroups"));
            assertTrue(persistenceUtil.isLoaded(returnedSA, "dataGroups"));

            Optional<FunctionGroup> optionalFunctionGroup = returnedSA.getFunctionGroups().stream().findFirst();
            assertTrue(optionalFunctionGroup.isPresent());
            optionalFunctionGroup.ifPresent(
                functionGroup1 -> assertTrue(persistenceUtil.isLoaded(functionGroup, "groupedFunctionPrivilegeList")));
        });
    }

    @Test
    @Transactional
    public void shouldFetchPermissionSetsWithPermissions() {
        serviceAgreementLegalEntity1 = createLegalEntity("EX-LE-1", "le-name-1", null);

        legalEntityJpaRepository.save(serviceAgreementLegalEntity1);

        ApplicableFunctionPrivilege viewLe = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");

        AssignablePermissionSet userSet = new AssignablePermissionSet();
        userSet.setName("user-set-1");
        userSet.setDescription("desc");
        userSet.setType(CUSTOM);
        userSet.getPermissions().add(viewLe.getId());

        assignablePermissionSetJpaRepository.save(userSet);

        AssignablePermissionSet adminSet = new AssignablePermissionSet();
        adminSet.setName("admin-set-1");
        adminSet.setDescription("desc");
        adminSet.setType(CUSTOM);
        adminSet.getPermissions().add(viewLe.getId());

        assignablePermissionSetJpaRepository.save(adminSet);

        ServiceAgreement sa = new ServiceAgreement()
            .withMaster(true)
            .withName("sa-1")
            .withDescription("desc")
            .withCreatorLegalEntity(serviceAgreementLegalEntity1);

        sa.addParticipant(new Participant()
            .withLegalEntity(serviceAgreementLegalEntity1)
            .withShareAccounts(true)
            .withShareUsers(true));

        sa.getPermissionSetsRegular().add(userSet);
        sa.getPermissionSetsAdmin().add(adminSet);

        String saId = serviceAgreementJpaRepository.save(sa).getId();

        entityManager.flush();
        entityManager.clear();

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(saId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR).orElse(null);

        assertTrue(persistenceUtil.isLoaded(serviceAgreement, "permissionSetsRegular"));
        assertTrue(persistenceUtil
            .isLoaded(serviceAgreement.getPermissionSetsRegular().stream().findFirst().get(), "permissions"));
    }

    @Test
    @Transactional
    public void shouldReturnServiceAgreementsAssignedToPermissionSet() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "LE1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ApplicableFunctionPrivilege viewLe = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");
        AssignablePermissionSet userSet = new AssignablePermissionSet();
        userSet.setName("user-set-1");
        userSet.setDescription("desc");
        userSet.setType(CUSTOM);
        userSet.getPermissions().add(viewLe.getId());

        userSet = assignablePermissionSetJpaRepository.save(userSet);

        ServiceAgreement serviceAgreement = createServiceAgreement("SA1", "id.ex1", "desc1", legalEntity, null, null);
        serviceAgreement.getPermissionSetsAdmin().add(userSet);
        serviceAgreementJpaRepository.save(serviceAgreement);

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(
            0, 10, null, null
        );

        Page<ServiceAgreement> list =
            serviceAgreementJpaRepository.getServiceAgreementByPermissionSetId(
                userSet,
                searchAndPaginationParameters
            );

        assertEquals(1, list.getTotalPages());
        assertEquals(1, list.getContent().size());
    }

    @Test
    @Transactional
    public void shouldReturnEmptyListWhenApsNotAssignedToSa() {
        AssignablePermissionSet userSet = new AssignablePermissionSet();
        userSet.setName("user-set-2");
        userSet.setDescription("desc");
        userSet.setType(CUSTOM);

        userSet = assignablePermissionSetJpaRepository.save(userSet);

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(0, 10, null,
            null);

        Page<ServiceAgreement> serviceAgreementByPermissionSetId = serviceAgreementJpaRepository
            .getServiceAgreementByPermissionSetId(userSet, searchAndPaginationParameters);

        assertEquals(1, serviceAgreementByPermissionSetId.getTotalPages());
        assertEquals(0, serviceAgreementByPermissionSetId.getContent().size());
    }

    @Test
    @Transactional
    public void shouldDeleteServiceAgreementWithPermissionSets() {

        LegalEntity legalEntity = createLegalEntity("prLe1", "providerName1", null);
        legalEntityJpaRepository.save(legalEntity);
        ServiceAgreement sa = createServiceAgreement("sa_name", "sa-ext-id", "desc", legalEntity, legalEntity.getId(),
            legalEntity.getId());

        Set<AssignablePermissionSet> aps = createAssignablePermissionSet("aps-name", "desc", CUSTOM);
        List<AssignablePermissionSet> apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsRegular().add(apsList.get(0));

        aps = createAssignablePermissionSet("aps-name1", "desc", CUSTOM);
        apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsRegular().add(apsList.get(0));

        aps = createAssignablePermissionSet("aps-name2", "desc", CUSTOM);
        apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsAdmin().add(apsList.get(0));

        String id = serviceAgreementJpaRepository.saveAndFlush(sa).getId();
        flushAndClearSession();
        serviceAgreementJpaRepository.delete(serviceAgreementJpaRepository.findById(id).get());
        flushAndClearSession();
        assertFalse(serviceAgreementJpaRepository.findById(id).isPresent());
    }

    @Test
    @Transactional
    public void shouldRemovePermissionSets() {

        LegalEntity legalEntity = createLegalEntity("prLe1", "providerName1", null);
        legalEntityJpaRepository.save(legalEntity);
        ServiceAgreement sa = createServiceAgreement("sa_name", "sa-ext-id", "desc", legalEntity, legalEntity.getId(),
            legalEntity.getId());

        Set<AssignablePermissionSet> aps = createAssignablePermissionSet("aps-name", "desc", CUSTOM);
        List<AssignablePermissionSet> apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsRegular().add(apsList.get(0));

        aps = createAssignablePermissionSet("aps-name1", "desc", CUSTOM);
        apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsRegular().add(apsList.get(0));

        Long apsId = apsList.get(0).getId();

        aps = createAssignablePermissionSet("aps-name2", "desc", CUSTOM);
        apsList = assignablePermissionSetJpaRepository.saveAll(aps);
        sa.getPermissionSetsAdmin().add(apsList.get(0));

        String id = serviceAgreementJpaRepository.saveAndFlush(sa).getId();
        flushAndClearSession();
        sa = serviceAgreementJpaRepository.findById(id, SERVICE_AGREEMENT_WITH_PERMISSION_SETS).get();
        sa.getPermissionSetsRegular().remove(assignablePermissionSetJpaRepository.findById(apsId).get());

        serviceAgreementJpaRepository.save(sa);

        flushAndClearSession();
        assertEquals(1, serviceAgreementJpaRepository
            .findById(id, SERVICE_AGREEMENT_WITH_PERMISSION_SETS).get().getPermissionSetsRegular().size());
    }

    @Test
    @Transactional
    public void shouldReturnSaIdsAlongWithDgIdsAndDataItemIds() {
        final String USER_ID = UUID.randomUUID().toString();

        FunctionGroup functionGroup = null;
        FunctionGroup functionGroup01 = null;

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setName("le-name");
        legalEntity.setExternalId("le-name");
        legalEntity.setType(LegalEntityType.CUSTOMER);

        legalEntity = legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement = createServiceAgreement("sa-01", "id.external", "sa-01", legalEntity,
            legalEntity.getId(), legalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        ServiceAgreement serviceAgreement01 = createServiceAgreement("sa-02", "id.external.02", "sa-02", legalEntity,
            legalEntity.getId(), legalEntity.getId());
        serviceAgreement01 = serviceAgreementJpaRepository.save(serviceAgreement01);

        ApplicableFunctionPrivilege viewSa = businessFunctionCache.
            getByFunctionIdAndPrivilege("1028", "view");

        ApplicableFunctionPrivilege editSa = businessFunctionCache.
            getByFunctionIdAndPrivilege("1028", "edit");

        GroupedFunctionPrivilege groupedFunctionPrivilegeExecute = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeExecute.setApplicableFunctionPrivilegeId(viewSa.getId());
        groupedFunctionPrivilegeExecute.setFunctionGroup(functionGroup);

        GroupedFunctionPrivilege groupedFunctionPrivilegeRead = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeRead.setApplicableFunctionPrivilegeId(viewSa.getId());
        groupedFunctionPrivilegeRead.setFunctionGroup(functionGroup);

        GroupedFunctionPrivilege groupedFunctionPrivilegeRead3 = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeRead3.setApplicableFunctionPrivilegeId(editSa.getId());
        groupedFunctionPrivilegeRead3.setFunctionGroup(functionGroup01);

        functionGroup = FunctionGroupUtil.getFunctionGroup(null, "name", "description",
            newHashSet(groupedFunctionPrivilegeExecute, groupedFunctionPrivilegeRead),
            FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroupJpaRepository.saveAndFlush(functionGroup);

        functionGroup01 = FunctionGroupUtil.getFunctionGroup(null, "name-01", "description-01",
            newHashSet(groupedFunctionPrivilegeRead3), FunctionGroupType.DEFAULT, serviceAgreement01);
        functionGroupJpaRepository.saveAndFlush(functionGroup01);

        UserContext userContext = userContextJpaRepository
            .saveAndFlush(new UserContext(USER_ID, serviceAgreement.getId()));
        UserContext userContext01 = userContextJpaRepository
            .saveAndFlush(new UserContext(USER_ID, serviceAgreement01.getId()));

        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroupJpaRepository.saveAndFlush(dataGroup);

        DataGroup dataGroup01 = DataGroupUtil.createDataGroup("name01", "ARRANGEMENTS", "desc01", serviceAgreement);
        dataGroup01.setDataItemIds(Sets.newHashSet("00004", "00005", "00006"));
        dataGroupJpaRepository.saveAndFlush(dataGroup01);

        DataGroup dataGroup02 = DataGroupUtil.createDataGroup("name02", "ARRANGEMENTS", "desc02", serviceAgreement01);
        dataGroup02.setDataItemIds(Sets.newHashSet("00007", "00008", "00009"));
        dataGroupJpaRepository.saveAndFlush(dataGroup02);

        DataGroup dataGroup03 = DataGroupUtil.createDataGroup("name03", "CUSTOMERS", "desc03", serviceAgreement01);
        dataGroup03.setDataItemIds(Sets.newHashSet("00010", "00011", "00012"));
        dataGroupJpaRepository.saveAndFlush(dataGroup03);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup01 = new UserAssignedFunctionGroup(functionGroup01,
            userContext01);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup01);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));
        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), userAssignedFunctionGroup));
        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup02.getId()), userAssignedFunctionGroup01));
        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup03.getId()), userAssignedFunctionGroup01));

        List<Tuple> response = serviceAgreementJpaRepository
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(USER_ID, "ARRANGEMENTS",
                newHashSet(viewSa.getId()));

        assertThat(response, hasSize(6));

        assertEquals(serviceAgreement.getId(), response.get(0).get(0, String.class));
        assertEquals(dataGroup.getId(), response.get(0).get(1, String.class));
        assertTrue(dataGroup.getDataItemIds().contains(response.get(0).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(0).get(3, String.class));

        assertEquals(serviceAgreement.getId(), response.get(1).get(0, String.class));
        assertEquals(dataGroup.getId(), response.get(1).get(1, String.class));
        assertTrue(dataGroup.getDataItemIds().contains(response.get(1).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(1).get(3, String.class));

        assertEquals(serviceAgreement.getId(), response.get(2).get(0, String.class));
        assertEquals(dataGroup.getId(), response.get(2).get(1, String.class));
        assertTrue(dataGroup.getDataItemIds().contains(response.get(2).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(2).get(3, String.class));

        assertEquals(serviceAgreement.getId(), response.get(3).get(0, String.class));
        assertEquals(dataGroup01.getId(), response.get(3).get(1, String.class));
        assertTrue(dataGroup01.getDataItemIds().contains(response.get(3).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(3).get(3, String.class));

        assertEquals(serviceAgreement.getId(), response.get(4).get(0, String.class));
        assertEquals(dataGroup01.getId(), response.get(4).get(1, String.class));
        assertTrue(dataGroup01.getDataItemIds().contains(response.get(4).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(4).get(3, String.class));

        assertEquals(serviceAgreement.getId(), response.get(5).get(0, String.class));
        assertEquals(dataGroup01.getId(), response.get(5).get(1, String.class));
        assertTrue(dataGroup01.getDataItemIds().contains(response.get(5).get(2, String.class)));
        assertEquals(viewSa.getId(), response.get(5).get(3, String.class));
    }

    private void setupServiceAgreementsLegalEntitiesAndProviders() {

        String LegalEntityName1 = "service agreement legal entity 1";
        String LegalEntityName2 = "service agreement legal entity 2";
        String providerAdminId1 = "PA-1";
        String providerAdminId2 = "PA-2";
        String providerAdminId3 = "PA-3";
        String userId = "6";
        serviceAgreementLegalEntity1 = createLegalEntity("EX-LE-1", LegalEntityName1, null);
        serviceAgreementLegalEntity2 = createLegalEntity("EX-LE-2", LegalEntityName2, null);
        LegalEntity providerLE1 = createLegalEntity("prLe1", "providerName1", null);
        LegalEntity providerLE2 = createLegalEntity("prLe2", "providerName2", null);
        LegalEntity providerLE3 = createLegalEntity("prLe3", "providerName3", null);

        Participant provider1 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId1, true, false);
        List<String> providerUsers1 = Arrays.asList("1", "2");
        provider1.addParticipantUsers(providerUsers1);

        Participant provider2 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId2, true, false);
        List<String> providerUsers2 = Arrays.asList("3", "4");
        provider2.addParticipantUsers(providerUsers2);

        Participant provider3 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId3, true, false);
        List<String> providerUsers3 = Arrays.asList("5", "6", "7");
        provider3.addParticipantUsers(providerUsers3);

        legalEntityJpaRepository.save(providerLE1);
        legalEntityJpaRepository.save(providerLE2);
        legalEntityJpaRepository.save(providerLE3);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity1);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity2);

        serviceAgreement1 = createServiceAgreement("sa1", "id.ex1", "desc1", serviceAgreementLegalEntity1, null, null);
        serviceAgreement1.addParticipant(provider1, providerLE1.getId(), true, false);
        serviceAgreement1.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider1.setServiceAgreement(serviceAgreement1);
        serviceAgreement1.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement1);

        serviceAgreement2 = createServiceAgreement("sa2", "id.ex2", "desc2", serviceAgreementLegalEntity1, null, null);
        serviceAgreement2.setAdditions(additions);
        serviceAgreement2.addParticipant(new Participant(), serviceAgreementLegalEntity2.getId(), false, true);
        serviceAgreement2.addParticipant(provider2, providerLE2.getId(), true, false);
        provider2.setServiceAgreement(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        serviceAgreement3 = createServiceAgreement("sa3", "id.ex3", "desc3", serviceAgreementLegalEntity2, null, null);
        serviceAgreement3.addParticipant(provider3, providerLE3.getId(), true, false);
        provider3.setServiceAgreement(serviceAgreement3);
        serviceAgreement3.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        serviceAgreement3.addParticipant(new Participant(), serviceAgreementLegalEntity2.getId(), false, true);
        serviceAgreement3.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        addPermissionToServiceAgreement(userId, serviceAgreement3);
        flushAndClearSession();
    }

    private void setupSAListForPagination() {

        String LegalEntityName1 = "service agreement legal entity 1";
        String LegalEntityName2 = "service agreement legal entity 2";
        String providerAdminId1 = "PA-1";
        String providerAdminId2 = "PA-2";
        String providerAdminId3 = "PA-3";
        String providerAdminId4 = "PA-4";
        String providerAdminId5 = "PA-5";
        String providerAdminId6 = "PA-6";
        String providerAdminId7 = "PA-7";
        String providerAdminId8 = "PA-8";
        String providerAdminId9 = "PA-9";
        String providerAdminId10 = "PA-10";
        String userId = "6";
        serviceAgreementLegalEntity1 = createLegalEntity("EX-LE-1", LegalEntityName1, null);
        serviceAgreementLegalEntity2 = createLegalEntity("EX-LE-2", LegalEntityName2, null);
        LegalEntity providerLE1 = createLegalEntity("prLe1", "providerName1", null);
        LegalEntity providerLE2 = createLegalEntity("prLe2", "providerName2", null);
        LegalEntity providerLE3 = createLegalEntity("prLe3", "providerName3", null);
        LegalEntity providerLE4 = createLegalEntity("prLe4", "providerName4", null);
        LegalEntity providerLE5 = createLegalEntity("prLe5", "providerName5", null);
        LegalEntity providerLE6 = createLegalEntity("prLe6", "providerName6", null);
        LegalEntity providerLE7 = createLegalEntity("prLe7", "providerName7", null);
        LegalEntity providerLE8 = createLegalEntity("prLe8", "providerName8", null);
        LegalEntity providerLE9 = createLegalEntity("prLe9", "providerName9", null);
        LegalEntity providerLE10 = createLegalEntity("prLe10", "providerName10", null);

        Participant provider1 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId1, true, false);
        List<String> providerUsers1 = Arrays.asList("1", "2");
        provider1.addParticipantUsers(providerUsers1);

        Participant provider2 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId2, true, false);
        List<String> providerUsers2 = Arrays.asList("3", "4");
        provider2.addParticipantUsers(providerUsers2);

        Participant provider3 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId3, true, false);
        List<String> providerUsers3 = Arrays.asList("5", "6", "7");
        provider3.addParticipantUsers(providerUsers3);

        Participant provider4 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId4, true, false);
        List<String> providerUsers4 = Arrays.asList("8", "9", "10");
        provider4.addParticipantUsers(providerUsers4);

        Participant provider5 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId5, true, false);
        List<String> providerUsers5 = Arrays.asList("11", "12", "13");
        provider5.addParticipantUsers(providerUsers5);

        Participant provider6 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId6, true, false);
        List<String> providerUsers6 = Arrays.asList("14", "15", "16");
        provider6.addParticipantUsers(providerUsers6);

        Participant provider7 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId7, true, false);
        List<String> providerUsers7 = Arrays.asList("17", "18", "19");
        provider7.addParticipantUsers(providerUsers7);

        Participant provider8 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId8, true, false);
        List<String> providerUsers8 = Arrays.asList("20", "21", "22");
        provider8.addParticipantUsers(providerUsers8);

        Participant provider9 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId9, true, false);
        List<String> providerUsers9 = Arrays.asList("23", "24", "25");
        provider9.addParticipantUsers(providerUsers9);

        Participant provider10 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId10, true, false);
        List<String> providerUsers10 = Arrays.asList("26", "27", "28");
        provider10.addParticipantUsers(providerUsers10);

        legalEntityJpaRepository.save(providerLE1);
        legalEntityJpaRepository.save(providerLE2);
        legalEntityJpaRepository.save(providerLE3);
        legalEntityJpaRepository.save(providerLE4);
        legalEntityJpaRepository.save(providerLE5);
        legalEntityJpaRepository.save(providerLE6);
        legalEntityJpaRepository.save(providerLE7);
        legalEntityJpaRepository.save(providerLE8);
        legalEntityJpaRepository.save(providerLE9);
        legalEntityJpaRepository.save(providerLE10);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity1);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity2);

        serviceAgreement1 = createServiceAgreement("sa1", "id.ex1", "desc1", serviceAgreementLegalEntity1, null, null);
        serviceAgreement1.addParticipant(provider1, providerLE1.getId(), true, false);
        serviceAgreement1.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        serviceAgreement1.setMaster(true);
        provider1.setServiceAgreement(serviceAgreement1);
        serviceAgreement1.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement1);
        addPermissionToServiceAgreement(userId, serviceAgreement1);

        serviceAgreement2 = createServiceAgreement("sa2", "id.ex2", "desc2", serviceAgreementLegalEntity1, null, null);
        serviceAgreement2.setAdditions(additions);
        serviceAgreement2.addParticipant(new Participant(), serviceAgreementLegalEntity2.getId(), false, true);
        serviceAgreement2.addParticipant(provider2, providerLE2.getId(), true, false);
        provider2.setServiceAgreement(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement2);
        addPermissionToServiceAgreement(userId, serviceAgreement2);

        serviceAgreement3 = createServiceAgreement("sa3", "id.ex3", "desc3", serviceAgreementLegalEntity2, null, null);
        serviceAgreement3.addParticipant(provider3, providerLE3.getId(), true, false);
        provider3.setServiceAgreement(serviceAgreement3);
        serviceAgreement3.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        serviceAgreement3.addParticipant(new Participant(), serviceAgreementLegalEntity2.getId(), false, true);
        serviceAgreement3.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        addPermissionToServiceAgreement(userId, serviceAgreement3);

        ServiceAgreement serviceAgreement4 = createServiceAgreement("sa4", "id.ex4", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement4.addParticipant(provider4, providerLE4.getId(), true, false);
        serviceAgreement4.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider4.setServiceAgreement(serviceAgreement4);
        serviceAgreement4.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement4);
        addPermissionToServiceAgreement(userId, serviceAgreement4);

        ServiceAgreement serviceAgreement5 = createServiceAgreement("sa5", "id.ex5", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement5.addParticipant(provider5, providerLE5.getId(), true, false);
        serviceAgreement5.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider5.setServiceAgreement(serviceAgreement5);
        serviceAgreement5.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement5);
        addPermissionToServiceAgreement(userId, serviceAgreement5);

        ServiceAgreement serviceAgreement6 = createServiceAgreement("sa6", "id.ex6", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement6.addParticipant(provider6, providerLE6.getId(), true, false);
        serviceAgreement6.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider6.setServiceAgreement(serviceAgreement6);
        serviceAgreement6.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement6);
        addPermissionToServiceAgreement(userId, serviceAgreement6);

        ServiceAgreement serviceAgreement7 = createServiceAgreement("sa7", "id.ex7", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement7.addParticipant(provider7, providerLE7.getId(), true, false);
        serviceAgreement7.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider7.setServiceAgreement(serviceAgreement7);
        serviceAgreement7.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement7);
        addPermissionToServiceAgreement(userId, serviceAgreement7);

        ServiceAgreement serviceAgreement8 = createServiceAgreement("sa8", "id.ex8", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement8.addParticipant(provider8, providerLE8.getId(), true, false);
        serviceAgreement8.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider8.setServiceAgreement(serviceAgreement8);
        serviceAgreement8.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement8);
        addPermissionToServiceAgreement(userId, serviceAgreement8);

        ServiceAgreement serviceAgreement9 = createServiceAgreement("sa9", "id.ex9", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement9.addParticipant(provider9, providerLE9.getId(), true, false);
        serviceAgreement9.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider9.setServiceAgreement(serviceAgreement9);
        serviceAgreement9.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement9);
        addPermissionToServiceAgreement(userId, serviceAgreement9);

        ServiceAgreement serviceAgreement10 = createServiceAgreement("sa10", "id.ex10", "desc1",
            serviceAgreementLegalEntity1, null,
            null);
        serviceAgreement10.addParticipant(provider10, providerLE10.getId(), true, false);
        serviceAgreement10.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider10.setServiceAgreement(serviceAgreement10);
        serviceAgreement10.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement10);
        addPermissionToServiceAgreement(userId, serviceAgreement10);

        flushAndClearSession();
    }

    private PageRequest getPageableObjWithoutSortingObject(int from, int size) {
        return PageRequest.of(from, size);
    }

    private void addParticipantToServiceAgreement(ServiceAgreement serviceAgreement, LegalEntity providerLe,
        List<String> admins, List<String> users, boolean shareUsers, boolean shareAccounts) {
        Participant provider = new Participant();
        provider.setShareUsers(shareUsers);
        provider.setShareAccounts(shareAccounts);
        for (String adminId : admins) {
            provider.addAdmin(adminId);
        }
        if (users != null) {
            for (String userId : users) {
                provider.addParticipantUser(userId);
            }
        }
        serviceAgreement.addParticipant(provider, providerLe.getId(), shareUsers, shareAccounts);
    }

    private LegalEntity createAndSaveLegalEntity(String legalEntityName, String externalId, LegalEntity parent) {
        LegalEntity legalEntity = createLegalEntity(legalEntityName, externalId, parent);
        return legalEntityJpaRepository.save(legalEntity);
    }

    private Set<AssignablePermissionSet> createAssignablePermissionSet(String name, String description,
        AssignablePermissionType assignablePermissionType) {
        Set<AssignablePermissionSet> assignablePermissionSets = new HashSet<>();
        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName(name);
        assignablePermissionSet.setDescription(description);
        assignablePermissionSet.setType(assignablePermissionType);
        assignablePermissionSets.add(assignablePermissionSet);
        return assignablePermissionSets;
    }

}
