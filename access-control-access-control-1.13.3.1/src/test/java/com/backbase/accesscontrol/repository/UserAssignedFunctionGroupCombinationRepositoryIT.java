package com.backbase.accesscontrol.repository;

import static junit.framework.TestCase.assertTrue;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.google.common.collect.Sets;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserAssignedFunctionGroupCombinationRepositoryIT extends TestRepositoryContext {

    private LegalEntity legalEntity;

    private FunctionGroup functionGroup;

    private ServiceAgreement serviceAgreement;

    private UserAssignedFunctionGroup userAssignedFunctionGroup;

    private DataGroup dataGroup1;

    private DataGroup dataGroup2;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Autowired
    private DataGroupItemJpaRepository dataGroupItemJpaRepository;

    @Autowired
    private UserContextJpaRepository userContextJpaRepository;
    @Autowired
    private RepositoryCleaner repositoryCleaner;

    private static final String USER_ID = UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();

        legalEntity = new LegalEntity();
        legalEntity.setName("le-name");
        legalEntity.setExternalId("le-name");
        legalEntity.setType(LegalEntityType.CUSTOMER);
        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setName("sa-01");
        serviceAgreement.setDescription("sa-01");
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(serviceAgreement);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        UserContext userContext = userContextJpaRepository.save(new UserContext(USER_ID, serviceAgreement.getId()));
        userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        dataGroup1 = DataGroupUtil.createDataGroup("dataGroup1", "ARRANGEMENTS", "arrangment1", serviceAgreement);
        dataGroup2 = DataGroupUtil.createDataGroup("dataGroup2", "CONTACTS", "arrangment2", serviceAgreement);
        dataGroupJpaRepository.save(dataGroup1);
        dataGroupJpaRepository.save(dataGroup2);

        UserAssignedFunctionGroupCombination userAssignedFunctionGroupDataGroup1 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroup1.getId()),
            userAssignedFunctionGroup
        );

        UserAssignedFunctionGroupCombination userAssignedFunctionGroupDataGroup2 = new UserAssignedFunctionGroupCombination(
            Sets.newHashSet(dataGroup2.getId()),
            userAssignedFunctionGroup
        );
        userAssignedCombinationRepository.save(userAssignedFunctionGroupDataGroup1);
        userAssignedCombinationRepository.save(userAssignedFunctionGroupDataGroup2);
    }

    @Test
    @Transactional
    public void shouldReturnNumberOfAssignedDataGroups() {
        assertTrue(userAssignedCombinationRepository.existsByDataGroupIdsIn(Sets.newHashSet(dataGroup1.getId())));
    }
}