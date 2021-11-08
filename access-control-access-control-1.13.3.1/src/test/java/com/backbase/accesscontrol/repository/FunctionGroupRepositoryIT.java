package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.FUNCTION_GROUP_WITH_SA_AND_LEGAL_ENTITY_AND_PERMISSION_SETS_REGULAR;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public class FunctionGroupRepositoryIT extends TestRepositoryContext {

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @Autowired
    private BusinessFunctionCache businessFunctionCache;


    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;
    private LegalEntity legalEntity;
    private ServiceAgreement serviceAgreement;
    private ApplicableFunctionPrivilege applicableFunctionPrivilege1;
    private ApplicableFunctionPrivilege applicableFunctionPrivilege2;

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        applicableFunctionPrivilege1 = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "view");
        applicableFunctionPrivilege2 = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "edit");

        legalEntity = legalEntityJpaRepository
            .save(LegalEntityUtil.createLegalEntity(null, "le-name", "le-ex-id", null, LegalEntityType.BANK));
        legalEntityJpaRepository.flush();

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName("sa name");
        serviceAgreement.setDescription("sa description");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setExternalId("externalId");
        serviceAgreementJpaRepository.save(serviceAgreement);
        serviceAgreementJpaRepository.flush();

        functionGroup1 = getFunctionGroup(null, "fg1", "desc.fg1",
            getGroupedFunctionPrivileges(getGroupedFunctionPrivilege(null, applicableFunctionPrivilege1, null)),
            FunctionGroupType.DEFAULT, null);
        functionGroup1.setServiceAgreement(serviceAgreement);
        functionGroup2 = getFunctionGroup(null, "fg2", "desc.fg2",
            getGroupedFunctionPrivileges(getGroupedFunctionPrivilege(null, applicableFunctionPrivilege2, null)),
            FunctionGroupType.DEFAULT, null);
        functionGroup2.setServiceAgreement(serviceAgreement);
        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);
        functionGroupJpaRepository.flush();
    }

    @Test
    @Transactional
    public void shouldGetFunctionGroupsByIdsIn() {
        List<String> ids = asList(functionGroup1.getId(), functionGroup2.getId());
        List<FunctionGroup> functionGroups = functionGroupJpaRepository.findByIdIn(ids);

        assertEquals(2, functionGroups.size());
        assertEquals(1,
            functionGroups.stream().filter(functionGroup -> functionGroup.getId().equals(functionGroup1.getId()))
                .collect(Collectors.toList()).size());
        assertEquals(1,
            functionGroups.stream().filter(functionGroup -> functionGroup.getId().equals(functionGroup1.getId()))
                .collect(Collectors.toList()).size());
    }

    @Test
    @Transactional
    public void shouldGetFunctionGroupByIdAndType() {

        Optional<FunctionGroup> functionGroup = functionGroupJpaRepository
            .findByIdAndType(functionGroup1.getId(), FunctionGroupType.DEFAULT);

        assertTrue(functionGroup.isPresent());
        assertEquals(functionGroup1.getId(), functionGroup.get().getId());
        assertEquals(functionGroup1.getDescription(), functionGroup.get().getDescription());
        assertEquals(functionGroup1.getName(), functionGroup.get().getName());
        assertEquals(functionGroup1.getPermissions().size(),
            functionGroup.get().getPermissions().size());
    }

    @Test
    @Transactional
    public void shouldGetFunctionGroupById() {

        Optional<FunctionGroup> functionGroup = functionGroupJpaRepository.findById(functionGroup1.getId());

        assertTrue(functionGroup.isPresent());
        assertEquals(functionGroup1.getId(), functionGroup.get().getId());
        assertEquals(functionGroup1.getDescription(), functionGroup.get().getDescription());
        assertEquals(functionGroup1.getName(), functionGroup.get().getName());
        assertEquals(functionGroup1.getPermissions().size(),
            functionGroup.get().getPermissions().size());
    }

    @Test
    @Transactional
    public void shouldDeleteFunctionGroupById() {
        functionGroupJpaRepository.deleteById(functionGroup1.getId());

        Optional<FunctionGroup> functionGroup = functionGroupJpaRepository
            .findByIdAndType(functionGroup1.getId(), FunctionGroupType.DEFAULT);

        assertFalse(functionGroup.isPresent());
    }

    @Test
    @Transactional
    public void shouldGetFunctionGroupNameAndServiceAgreementId() {
        LegalEntity entity = legalEntityJpaRepository
            .save(LegalEntityUtil.createLegalEntity(null, "le-002", "le-ex-id-2", null, LegalEntityType.BANK));
        legalEntityJpaRepository.flush();

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName("SA-Name1");
        serviceAgreement.setDescription("description");
        serviceAgreement.setCreatorLegalEntity(entity);
        serviceAgreementJpaRepository.save(serviceAgreement);
        serviceAgreementJpaRepository.flush();

        FunctionGroup functionGroup3 = getFunctionGroup(null, "fg3", "desc.fg3",
            getGroupedFunctionPrivileges(getGroupedFunctionPrivilege(null, applicableFunctionPrivilege1, null)),
            FunctionGroupType.DEFAULT, null);
        functionGroup3.setServiceAgreement(serviceAgreement);
        functionGroupJpaRepository.save(functionGroup3);

        Optional<FunctionGroup> responseFunctionGroup = functionGroupJpaRepository
            .findByNameAndServiceAgreementId("fg3", serviceAgreement.getId());

        assertTrue(responseFunctionGroup.isPresent());
        assertEquals("fg3", responseFunctionGroup.get().getName());
        assertEquals(serviceAgreement.getId(), responseFunctionGroup.get().getServiceAgreement().getId());
    }

    @Test
    @Transactional
    public void shouldCountEntitiesWithNameAndIdNot() {
        ServiceAgreement serviceAgreement1 = createServiceAgreement("some name0", "some ex id 0", "some desc",
            legalEntity, null, null);

        ServiceAgreement serviceAgreement2 = createServiceAgreement("some name2", "some ex id 2", "some desc",
            legalEntity, null, null);

        serviceAgreementJpaRepository.saveAll(asList(serviceAgreement1, serviceAgreement2));

        String name = UUID.randomUUID().toString();

        FunctionGroup functionGroup1 = getFunctionGroup(null, name, "desc", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement1);
        FunctionGroup functionGroup2 = getFunctionGroup(null, name, "desc", new HashSet<>(),
            FunctionGroupType.DEFAULT, serviceAgreement2);
        FunctionGroup anotherFunctionGroup = getFunctionGroup(null, "another name", "desc",
            new HashSet<>(), FunctionGroupType.DEFAULT, serviceAgreement2);

        functionGroupJpaRepository.saveAll(asList(functionGroup1, functionGroup2, anotherFunctionGroup));

        boolean exists1 = functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(name, serviceAgreement1.getId(), functionGroup1.getId());
        boolean exists2 = functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(name, serviceAgreement2.getId(), functionGroup2.getId());
        boolean exists3 = functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(name, serviceAgreement1.getId(), anotherFunctionGroup.getId());
        boolean exists4 = functionGroupJpaRepository
            .existsByNameAndServiceAgreementIdAndIdNot(name, serviceAgreement2.getId(), anotherFunctionGroup.getId());

        assertFalse(exists1);
        assertFalse(exists2);
        assertTrue(exists3);
        assertTrue(exists4);

    }

    @Test
    public void readByIdOrByServiceAgreementExternalIdAndName() {

        Optional<FunctionGroup> result = functionGroupJpaRepository
            .readByServiceAgreementExternalIdAndName(functionGroup1.getServiceAgreement().getExternalId(),
                functionGroup1.getName(),
                FUNCTION_GROUP_WITH_SA_AND_LEGAL_ENTITY_AND_PERMISSION_SETS_REGULAR);
        assertEquals(result.get(), functionGroup1);

    }

    @Test
    public void readByIdIn() {
        List<FunctionGroup> result = functionGroupJpaRepository
            .readByIdIn(Collections.singletonList(functionGroup2.getId()),
                FUNCTION_GROUP_WITH_SA_AND_LEGAL_ENTITY_AND_PERMISSION_SETS_REGULAR);
        assertEquals(result.get(0), functionGroup2);
    }
}
