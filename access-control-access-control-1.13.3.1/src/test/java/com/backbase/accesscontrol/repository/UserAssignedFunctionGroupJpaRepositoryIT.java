package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class UserAssignedFunctionGroupJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;

    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private UserContextJpaRepository userContextJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @Autowired
    private BusinessFunctionCache businessFunctionCache;

    private LegalEntity legalEntity;
    private FunctionGroup functionGroup;
    private FunctionGroup systemFunctionGroup;
    private ServiceAgreement serviceAgreement;
    private UserContext userContext;
    private UserContext userContextTwo;
    private UserContext UserContextThree;

    private List<String> persistedAppFnPrivilegesIds = new ArrayList<>();
    private ApplicableFunctionPrivilege applicableFunctionPrivilegeEdit;
    private ApplicableFunctionPrivilege applicableFunctionPrivilegeView;

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String USER_ID2 = UUID.randomUUID().toString();
    private static final String USER_ID3 = UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();

        legalEntity = new LegalEntity();
        legalEntity.setName("le-name");
        legalEntity.setExternalId("le-name");
        legalEntity.setType(LegalEntityType.CUSTOMER);

        legalEntity = legalEntityJpaRepository.save(legalEntity);

        // create SA
        serviceAgreement = createServiceAgreement("sa-01", "id.external", "sa-01", legalEntity,
            legalEntity.getId(), legalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        applicableFunctionPrivilegeEdit = businessFunctionCache
            .getApplicableFunctionPrivilegeById(businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, null,
                    Lists.newArrayList("edit")).stream().findFirst().get());

        persistedAppFnPrivilegesIds.add(applicableFunctionPrivilegeEdit.getId());

        applicableFunctionPrivilegeView = businessFunctionCache
            .getApplicableFunctionPrivilegeById(businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, null,
                    Lists.newArrayList("view")).stream().findFirst().get());

        persistedAppFnPrivilegesIds.add(applicableFunctionPrivilegeView.getId());

        GroupedFunctionPrivilege groupedFunctionPrivilegeExecute = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeExecute.setApplicableFunctionPrivilegeId(applicableFunctionPrivilegeEdit.getId());
        groupedFunctionPrivilegeExecute.setFunctionGroup(functionGroup);

        GroupedFunctionPrivilege groupedFunctionPrivilegeRead = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeRead.setApplicableFunctionPrivilegeId(applicableFunctionPrivilegeView.getId());
        groupedFunctionPrivilegeRead.setFunctionGroup(functionGroup);

        GroupedFunctionPrivilege groupedFunctionPrivilegeRead2 = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeRead2.setApplicableFunctionPrivilegeId(applicableFunctionPrivilegeView.getId());
        groupedFunctionPrivilegeRead2.setFunctionGroup(functionGroup);

        functionGroup = FunctionGroupUtil.getFunctionGroup(null, "name", "description",
            newHashSet(groupedFunctionPrivilegeExecute, groupedFunctionPrivilegeRead, groupedFunctionPrivilegeRead2),
            FunctionGroupType.DEFAULT, serviceAgreement);
        systemFunctionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "name-system", "description",
                new HashSet<>(),
                FunctionGroupType.SYSTEM, serviceAgreement);

        functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.save(systemFunctionGroup);
        functionGroupJpaRepository.flush();

        userContext = userContextJpaRepository.save(new UserContext(USER_ID, serviceAgreement.getId()));
        userContextTwo = userContextJpaRepository.save(new UserContext(USER_ID2, serviceAgreement.getId()));
        UserContextThree = userContextJpaRepository.save(new UserContext(USER_ID3, serviceAgreement.getId()));
    }

    @Test
    @Transactional
    public void findByUserIdAndServiceAgreementIdAndFunctionGroupId() {

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        Optional<UserAssignedFunctionGroup> uaFgOptional = userAssignedFunctionGroupJpaRepository
            .findByUserIdAndServiceAgreementIdAndFunctionGroupId(USER_ID, serviceAgreement.getId(),
                functionGroup.getId());

        assertTrue(uaFgOptional.isPresent());
        assertEquals(functionGroup.getId(), uaFgOptional.get().getFunctionGroupId());
        assertEquals(userContext.getId(), uaFgOptional.get().getUserContextId());
    }

    @Test
    @Transactional
    public void findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType() throws Exception {

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        List<String> userIdsBySaExternalIdAndFgType = userAssignedFunctionGroupJpaRepository
            .findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(serviceAgreement.getExternalId(),
                FunctionGroupType.DEFAULT);

        assertThat(userIdsBySaExternalIdAndFgType, hasSize(1));
        assertThat(userIdsBySaExternalIdAndFgType, contains(userContext.getUserId()));
    }

    @Test
    @Transactional
    public void testCountAllByServiceAgreementIdAndUserIdIn() {

        FunctionGroup functionGroupOne = FunctionGroupUtil
            .getFunctionGroup(null, "name1", "description", new HashSet<>(),
                FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroupJpaRepository.save(functionGroupOne);
        functionGroupJpaRepository.flush();

        UserAssignedFunctionGroup userAssignedFunctionGroupOne = new UserAssignedFunctionGroup(functionGroupOne,
            userContext);
        UserAssignedFunctionGroup userAssignedFunctionGroupTwo = new UserAssignedFunctionGroup(functionGroupOne,
            userContextTwo);
        UserAssignedFunctionGroup userAssignedFunctionGroupThree = new UserAssignedFunctionGroup(functionGroupOne,
            UserContextThree);

        userAssignedFunctionGroupJpaRepository
            .saveAll(
                asList(userAssignedFunctionGroupOne, userAssignedFunctionGroupTwo, userAssignedFunctionGroupThree));

        long count = userAssignedFunctionGroupJpaRepository
            .countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(serviceAgreement.getId(),
                new HashSet<>(asList(USER_ID, USER_ID3)), FunctionGroupType.DEFAULT);

        assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndApplicableFunctionPrivilegeIds() {

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndAfpIds(serviceAgreement.getId(), persistedAppFnPrivilegesIds);

        assertEquals(1, userIdFgIdsMap.entrySet().size());
        assertThat(userIdFgIdsMap, hasKey(USER_ID));
        assertEquals(1, userIdFgIdsMap.get(USER_ID).size());
        assertThat(userIdFgIdsMap, hasValue(contains(functionGroup.getId())));
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndNullApplicableFunctionPrivilegeIds() {
        Map<String, Set<String>> userFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndAfpIds(serviceAgreement.getId(), null);

        assertEquals(0, userFgIdsMap.size());
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndEmptyApplicableFunctionPrivilegeIds() {
        Map<String, Set<String>> userFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndAfpIds(serviceAgreement.getId(), Collections.emptySet());

        assertEquals(0, userFgIdsMap.size());
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), "00001",
                "ARRANGEMENTS", persistedAppFnPrivilegesIds);

        assertEquals(1, userIdFgIdsMap.entrySet().size());
        assertThat(userIdFgIdsMap, hasKey(USER_ID));
        assertEquals(1, userIdFgIdsMap.get(USER_ID).size());
        assertThat(userIdFgIdsMap, hasValue(contains(functionGroup.getId())));
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndNullDataItemIdAndDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), null,
                "ARRANGEMENTS", persistedAppFnPrivilegesIds);

        assertEquals(1, userIdFgIdsMap.entrySet().size());
        assertThat(userIdFgIdsMap, hasKey(USER_ID));
        assertEquals(1, userIdFgIdsMap.get(USER_ID).size());
        assertThat(userIdFgIdsMap, hasValue(contains(functionGroup.getId())));
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndDataItemIdAndNullDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), "00001",
                null, persistedAppFnPrivilegesIds);

        assertEquals(1, userIdFgIdsMap.entrySet().size());
        assertThat(userIdFgIdsMap, hasKey(USER_ID));
        assertEquals(1, userIdFgIdsMap.get(USER_ID).size());
        assertThat(userIdFgIdsMap, hasValue(contains(functionGroup.getId())));
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndNullDataItemIdAndNullDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), null,
                null, persistedAppFnPrivilegesIds);

        assertEquals(1, userIdFgIdsMap.entrySet().size());
        assertThat(userIdFgIdsMap, hasKey(USER_ID));
        assertEquals(1, userIdFgIdsMap.get(USER_ID).size());
        assertThat(userIdFgIdsMap, hasValue(contains(functionGroup.getId())));
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndNonExistingDataItemIdAndDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(),
                "NON_EXISTING_DATA_ITEM_ID", "ARRANGEMENTS", persistedAppFnPrivilegesIds);

        assertEquals(0, userIdFgIdsMap.size());
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndDataItemIdAndUnknownDataGroupTypeAndAfpIds() {
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);

        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));

        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(),
                "00001", "UNKNOWN_TYPE", persistedAppFnPrivilegesIds);

        assertEquals(0, userIdFgIdsMap.size());
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndNullAfpIds() {
        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), "00001",
                "ARRANGEMENTS", persistedAppFnPrivilegesIds);

        assertEquals(0, userIdFgIdsMap.size());
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndEmptyAfpIds() {
        Map<String, Set<String>> userIdFgIdsMap = userAssignedFunctionGroupJpaRepository
            .findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreement.getId(), "00001",
                "ARRANGEMENTS", Collections.emptyList());

        assertEquals(0, userIdFgIdsMap.size());
    }

    @Test
    @Transactional
    public void validateUserIdAndServiceAgreementIdAndStatusAndApplicableFunctionPrivilegeIdsInResponse() {
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
        List<String> checkedPermissions = userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                USER_ID,
                serviceAgreement.getId(),
                ServiceAgreementState.ENABLED,
                new HashSet<>(persistedAppFnPrivilegesIds));

        assertThat(checkedPermissions,
            containsInAnyOrder(applicableFunctionPrivilegeEdit.getId(), applicableFunctionPrivilegeView.getId()));
    }

    @Test
    @Transactional
    public void countByUserIdAndServiceAgreementIdAndStatusAndApplicableFunctionPrivilegeIdsInEmpty() {
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
        long count = userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                USER_ID,
                serviceAgreement.getId(),
                ServiceAgreementState.ENABLED,
                null).size();

        assertEquals(0L, count);
    }

    @Test
    @Transactional
    public void checkPermissionOfDisabledServiceAgreement() {
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        long newCount = userAssignedFunctionGroupJpaRepository
            .findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
                USER_ID, serviceAgreement.getId(), ServiceAgreementState.ENABLED,
                new HashSet<>(persistedAppFnPrivilegesIds)).size();

        assertEquals(0, newCount);
    }
}
