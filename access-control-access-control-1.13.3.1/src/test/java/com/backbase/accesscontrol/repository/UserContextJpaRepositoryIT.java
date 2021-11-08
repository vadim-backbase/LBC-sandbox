package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class UserContextJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private UserContextJpaRepository userContextJpaRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;

    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BusinessFunctionCache businessFunctionCache;

    private UserContext userContext;
    private UserContext userContext1;
    private UserContext userContext2;
    private UserContext userContext3;
    private UserContext userContext4;
    private UserContext userContext5;

    private LegalEntity legalEntity;

    private FunctionGroup savedDefaultFunctionGroup1;
    private FunctionGroup savedDefaultFunctionGroup2;
    private ServiceAgreement serviceAgreement;
    private DataGroup dataGroup;
    private DataGroup dataGroup2;

    private ApplicableFunctionPrivilege viewSa;
    private ApplicableFunctionPrivilege createSa;
    private ApplicableFunctionPrivilege viewPs;

    @Before
    public void setUp() {

        legalEntity = legalEntityJpaRepository
            .save(createLegalEntity(null, "le-ex-id", "le-name", null, LegalEntityType.BANK));

        // create SA
        serviceAgreement =
            createServiceAgreement("BB between self", "id.external", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        ServiceAgreement serviceAgreement1 =
            createServiceAgreement("BB between self 1", "id.external.1", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement1 = serviceAgreementJpaRepository.save(serviceAgreement1);

        ServiceAgreement serviceAgreement2 =
            createServiceAgreement("BB between self 2", "id.external.2", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement2 = serviceAgreementJpaRepository.save(serviceAgreement2);

        viewSa = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        createSa = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "create");
        viewPs = businessFunctionCache.getByFunctionIdAndPrivilege("1006", "view");

        //save function group 1
        GroupedFunctionPrivilege viewSaWithLimit = getGroupedFunctionPrivilege(null, viewSa, null);
        GroupedFunctionPrivilege createSaWitLimit = getGroupedFunctionPrivilege(null, createSa, null);
        GroupedFunctionPrivilege viewPsWithLimit = getGroupedFunctionPrivilege(null, viewPs, null);
        savedDefaultFunctionGroup1 = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewSaWithLimit,
                    createSaWitLimit,
                    viewPsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        functionGroupJpaRepository.flush();

        //save function group 2
        viewSaWithLimit = getGroupedFunctionPrivilege(null, viewSa, null);
        createSaWitLimit = getGroupedFunctionPrivilege(null, createSa, null);
        viewPsWithLimit = getGroupedFunctionPrivilege(null, viewPs, null);
        savedDefaultFunctionGroup2 = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name2", "function-group-description2",
                getGroupedFunctionPrivileges(
                    viewSaWithLimit,
                    createSaWitLimit,
                    viewPsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        functionGroupJpaRepository.flush();

        // create data group
        dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
        dataGroupJpaRepository.save(dataGroup);
        dataGroupJpaRepository.flush();

        dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc", serviceAgreement);

        String dataGroupItem = dataGroup.getDataItemIds().iterator().next();

        dataGroup2.getDataItemIds().add(dataGroupItem);
        dataGroup2.getDataItemIds().add("NOT FOUND");
        dataGroupJpaRepository.save(dataGroup2);
        dataGroupJpaRepository.flush();

        userContext = new UserContext("u1", serviceAgreement.getId());
        userContext1 = new UserContext("u2", serviceAgreement.getId());
        userContext2 = new UserContext("u3", serviceAgreement.getId());
        userContext3 = new UserContext("u1", serviceAgreement1.getId());
        userContext4 = new UserContext("u2", serviceAgreement1.getId());
        userContext5 = new UserContext("u3", serviceAgreement1.getId());
        userContextJpaRepository.save(userContext);
        userContextJpaRepository.save(userContext1);
        userContextJpaRepository.save(userContext2);
        userContextJpaRepository.save(userContext3);
        userContextJpaRepository.save(userContext4);
        userContextJpaRepository.save(userContext5);
        userContextJpaRepository.save(new UserContext("u1", serviceAgreement2.getId()));
        userContextJpaRepository.save(new UserContext("u2", serviceAgreement2.getId()));
        userContextJpaRepository.save(new UserContext("u3", serviceAgreement2.getId()));
    }

    @Test
    public void shouldGetAssignedFunctionAndDataGroups() {

        String serviceAgreementId = serviceAgreement.getId();

        UserContext userContext = new UserContext("user", serviceAgreementId);

        UserAssignedFunctionGroup uafgWithDataGroup = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1, userContext);

        uafgWithDataGroup.setUserAssignedFunctionGroupCombinations(newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), uafgWithDataGroup)));

        UserAssignedFunctionGroup uafgWithoutDataGroup = new UserAssignedFunctionGroup(savedDefaultFunctionGroup2, userContext);
        userContext.setUserAssignedFunctionGroups(newHashSet(uafgWithDataGroup, uafgWithoutDataGroup));

        userContextJpaRepository.saveAndFlush(userContext);

        entityManager.clear();

        PersistenceUtil unitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        Optional<UserContext> res = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionAndDataGroups("user", serviceAgreementId);

        assertTrue(res.isPresent());
        res.ifPresent(userAsset -> {
            assertTrue(unitUtil.isLoaded(userAsset, "userAssignedFunctionGroups"));
            assertEquals(2, res.get().getUserAssignedFunctionGroups().size());
            for (UserAssignedFunctionGroup userAssignedFunctionGroup : res.get().getUserAssignedFunctionGroups()) {
                assertTrue(unitUtil.isLoaded(userAssignedFunctionGroup, "userAssignedFunctionGroupCombinations"));
            }
        });
    }

    @Test
    public void shouldReturnNoUsersWhenNoneFound() {

        Optional<UserContext> res = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionAndDataGroups("user", "serviceAgreement");

        assertFalse(res.isPresent());
    }

    @Test
    public void shouldFindAllAfpIdsByUserIdAndServiceAgreementIdAndAppFnPrivilegeIds() {
        UserContext userContext = new UserContext("user", serviceAgreement.getId());

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1, userContext);

        userContext.setUserAssignedFunctionGroups(newHashSet(uafg));
        userContextJpaRepository.saveAndFlush(userContext);

        userAssignedFunctionGroupJpaRepository.save(uafg);

        List<String> persistedAppFnPrivilegesIds = new ArrayList<>();
        persistedAppFnPrivilegesIds.add(viewSa.getId());
        persistedAppFnPrivilegesIds.add(createSa.getId());

        List<String> afpIds = userContextJpaRepository
            .findAllByUserIdAndServiceAgreementIdAndAfpIds(
                userContext.getUserId(),
                serviceAgreement.getId(),
                ServiceAgreementState.ENABLED,
                new HashSet<>(persistedAppFnPrivilegesIds));

        assertEquals(2, afpIds.size());
        assertTrue(afpIds
            .containsAll(asList(viewSa.getId(), createSa.getId())));
    }

    @Test
    public void findAllAfpIdsByUserIdAndServiceAgreementIdAndAppFnPrivilegeIdsEmpty() {
        List<String> afpIds = userContextJpaRepository
            .findAllByUserIdAndServiceAgreementIdAndAfpIds(
                "user",
                serviceAgreement.getId(),
                ServiceAgreementState.ENABLED,
                null);

        assertEquals(0, afpIds.size());
    }

    @Test
    public void shouldFindAfpIdsByUserIdAndServiceAgreementId() {
        UserContext userContext = new UserContext("user", serviceAgreement.getId());

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1, userContext);

        userContext.setUserAssignedFunctionGroups(newHashSet(uafg));
        userContextJpaRepository.saveAndFlush(userContext);

        userAssignedFunctionGroupJpaRepository.save(uafg);

        List<String> afpIds = userContextJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementId(userContext.getUserId(), serviceAgreement.getId());

        assertEquals(3, afpIds.size());
        assertTrue(
            afpIds.containsAll(asList(viewSa.getId(), createSa.getId(), viewPs.getId())));
    }

    @Test
    public void shouldFindAfpIdsByUserIdAndServiceAgreementIdEmpty() {
        List<String> afpIds = userContextJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementId("user", serviceAgreement.getId());

        assertEquals(0, afpIds.size());
    }

    @Test
    public void findAllUserContextsByAssignDataGroupId(){
        UserAssignedFunctionGroup userAssignedFunctionGroup =
                new UserAssignedFunctionGroup(savedDefaultFunctionGroup1, userContext3);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);


        UserAssignedFunctionGroupCombination userAssignedFunctionGroupDataGroup
                = new UserAssignedFunctionGroupCombination(
                Sets.newHashSet(dataGroup2.getId()),
                userAssignedFunctionGroup
        );
        userAssignedCombinationRepository.save(userAssignedFunctionGroupDataGroup);

        List<UserContextProjection> userContextProjections = userContextJpaRepository.findAllUserContextsByAssignDataGroupId(dataGroup2.getId());
        assertEquals(1, userContextProjections .size());
        assertEquals("u1", userContextProjections.get(0).getUserId());
    }

    @Test
    public void findAllUserContextsByAssignFunctionGroupId(){
        UserAssignedFunctionGroup userAssignedFunctionGroup =
                new UserAssignedFunctionGroup(savedDefaultFunctionGroup1, userContext3);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);


        List<UserContextProjection> userContextProjections =
                userContextJpaRepository.findAllUserContextsByAssignFunctionGroupId(savedDefaultFunctionGroup1.getId());
        assertEquals(1, userContextProjections .size());
        assertEquals("u1", userContextProjections.get(0).getUserId());
    }

    @Test
    public void findUserIdsByServiceAgreementIdAndFunctionGroupId() {
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1,
            userContext1);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup1);

        UserAssignedFunctionGroup userAssignedFunctionGroup2 = new UserAssignedFunctionGroup(savedDefaultFunctionGroup1,
            userContext2);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup2);

        Page<String> userIdsPage = userContextJpaRepository
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(
                serviceAgreement.getId(),
                savedDefaultFunctionGroup1.getId(),
                PageRequest.of(0, 2)
            );

        assertEquals(3, userIdsPage.getTotalElements());
        assertEquals(2, userIdsPage.getContent().size());
        assertEquals(Lists.newArrayList(userContext.getUserId(), userContext1.getUserId()), userIdsPage.getContent());
    }

    @Test
    public void shouldNotReturnUserIdsWithSystemFunctionGroup() {
        FunctionGroup savedSystemFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup("fg_2", "system-function-group-name", "system-function-group-description",
                Collections.emptySet(),
                FunctionGroupType.SYSTEM, serviceAgreement)
        );

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedSystemFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        Page<String> userIdsPage = userContextJpaRepository
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(
                serviceAgreement.getId(),
                savedSystemFunctionGroup.getId(),
                PageRequest.of(0, 2)
            );

        assertEquals(1, userIdsPage.getTotalElements());
        assertEquals(1, userIdsPage.getContent().size());
        assertEquals(userContext.getUserId(), userIdsPage.getContent().get(0));
    }
}
