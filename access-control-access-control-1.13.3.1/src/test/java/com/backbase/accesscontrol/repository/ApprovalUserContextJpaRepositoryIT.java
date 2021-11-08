package com.backbase.accesscontrol.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public class ApprovalUserContextJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private UserContextJpaRepository userContextJpaRepository;
    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @Before
    public void init() {
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void shouldReturnApprovalUserContext() {
        String approvalId = "appId";
        String userId = "userId";

        ServiceAgreement serviceAgreement;
        LegalEntity legalEntity;
        legalEntity = legalEntityJpaRepository.save(LegalEntityUtil.createLegalEntity("ex-id", "le-name", null));
        legalEntityJpaRepository.flush();

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName("SA-Name");
        serviceAgreement.setDescription("description");
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        ServiceAgreement serviceAgreementSave = serviceAgreementJpaRepository.save(serviceAgreement);

        FunctionGroup functionGroup = new FunctionGroup().withDescription("Desc")
            .withName("Name").withServiceAgreement(serviceAgreementSave).withType(
                FunctionGroupType.DEFAULT);
        functionGroupJpaRepository.save(functionGroup);

        DataGroup dataGroup = new DataGroup().withDescription("desc0").withName("name")
            .withServiceAgreement(serviceAgreement).withDataItemType("CONTACTS");
        dataGroupJpaRepository.save(dataGroup);

        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setServiceAgreementId(serviceAgreementSave.getId());
        Set<UserAssignedFunctionGroup> oldState = new HashSet<>();
        userContext.setUserAssignedFunctionGroups(oldState);
        UserContext userContextSave = userContextJpaRepository.save(userContext);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup =
            new ApprovalUserContextAssignFunctionGroup()
                .withFunctionGroupId(functionGroup.getId())
                .withDataGroups(Sets.newHashSet((dataGroup.getId())));

        ApprovalUserContext appUserContext = new ApprovalUserContext();
        appUserContext.setApprovalId(approvalId);
        appUserContext.setUserId(userContextSave.getUserId());
        appUserContext.setLegalEntityId(legalEntity.getId());
        appUserContext.setServiceAgreementId(serviceAgreement.getId());
        approvalUserContextAssignFunctionGroup.setApprovalUserContext(appUserContext);
        appUserContext.getApprovalUserContextAssignFunctionGroups().add(approvalUserContextAssignFunctionGroup);
        approvalUserContextJpaRepository.save(appUserContext);
        Optional<ApprovalUserContext> byApprovalIdWithFunctionAndDataGroups = approvalUserContextJpaRepository
            .findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId);
        assertNotNull(byApprovalIdWithFunctionAndDataGroups);

    }


    @Test
    @Transactional
    public void shouldReturnOptionalOfEmpty() {
        Optional<ApprovalUserContext> shouldReturnEmpty = approvalUserContextJpaRepository
            .findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies("");
        assertEquals(shouldReturnEmpty, Optional.empty());
    }

    @Test
    @Transactional
    public void shouldReturnOptionalOfEmptyWhenWrongApprovalIdIsSent() {
        Optional<ApprovalUserContext> shouldReturnEmpty = approvalUserContextJpaRepository
            .findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies("wrongId");
        assertEquals(shouldReturnEmpty, Optional.empty());
    }

}