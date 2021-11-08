package com.backbase.accesscontrol.repository;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

public class ApprovalServiceAgreementRefJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;

    @Autowired
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;


    @Before
    public void init() {
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void testGetApprovalSaIdsForUpdateAndDelete() {
        ApprovalServiceAgreementRef approvalServiceAgreementRef = new ApprovalServiceAgreementRef();
        approvalServiceAgreementRef.setApprovalId("DeleteSA");
        approvalServiceAgreementRef.setServiceAgreementId("SA1");
        approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreementRef);

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId("approvalId");
        approvalServiceAgreement.setServiceAgreementId("saId");
        approvalServiceAgreement.setCreatorLegalEntityId("leCreator");
        approvalServiceAgreement.setName("sa1");
        approvalServiceAgreement.setDescription("desc");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setEndDate(new Date(100000000));
        approvalServiceAgreement.setStartDate(new Date(10));
        approvalServiceAgreement.setExternalId("exSa");
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        approvalServiceAgreement.setAdditions(additions);
        Set<Long> adminAps = new HashSet<>();
        adminAps.add(1L);
        Set<Long> userAps = new HashSet<>();
        adminAps.add(2L);
        approvalServiceAgreement.setPermissionSetsAdmin(adminAps);
        approvalServiceAgreement.setPermissionSetsRegular(userAps);
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        List<ApprovalServiceAgreementRef> approvalServiceAgreementList =
            approvalServiceAgreementRefJpaRepository.findAll();
        assertEquals("Should return 2 records", 2, approvalServiceAgreementList.size());
        approvalServiceAgreementList
            .forEach(a -> {
                if (a.getApprovalAction().equals(ApprovalAction.DELETE)) {
                    assertEquals("SA1", a.getServiceAgreementId());
                } else {
                    assertEquals("saId", a.getServiceAgreementId());
                }
            });
    }

    @Test
    @Transactional
    public void testSaveAndReadApprovalServiceAgreementRef() {

        ApprovalServiceAgreementRef approvalServiceAgreementRef = new ApprovalServiceAgreementRef();
        approvalServiceAgreementRef.setApprovalId("appId1");
        approvalServiceAgreementRef.setServiceAgreementId("SA 1");
        approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreementRef);

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId("approvalId2");
        approvalServiceAgreement.setServiceAgreementId("saId");
        approvalServiceAgreement.setCreatorLegalEntityId("leCreator");
        approvalServiceAgreement.setName("sa1");
        approvalServiceAgreement.setDescription("desc");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setEndDate(new Date(100000000));
        approvalServiceAgreement.setStartDate(new Date(10));
        approvalServiceAgreement.setExternalId("exSa");
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        approvalServiceAgreement.setAdditions(additions);
        Set<Long> adminAps = new HashSet<>();
        adminAps.add(1L);
        Set<Long> userAps = new HashSet<>();
        adminAps.add(2L);
        approvalServiceAgreement.setPermissionSetsAdmin(adminAps);
        approvalServiceAgreement.setPermissionSetsRegular(userAps);
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        ApprovalServiceAgreement approvalServiceAgreement2 = new ApprovalServiceAgreement();
        approvalServiceAgreement2.setApprovalId("approvalId3");
        approvalServiceAgreement2.setServiceAgreementId("saId");
        approvalServiceAgreement2.setCreatorLegalEntityId("leCreator");
        approvalServiceAgreement2.setName("sa1");
        approvalServiceAgreement2.setDescription("desc");
        approvalServiceAgreement2.setMaster(true);
        approvalServiceAgreement2.setEndDate(new Date(100000000));
        approvalServiceAgreement2.setStartDate(new Date(10));
        approvalServiceAgreement2.setExternalId("exSa2");
        Map<String, String> additions2 = new HashMap<>();
        additions2.put("key", "value");
        approvalServiceAgreement2.setAdditions(additions2);
        Set<Long> adminAps2 = new HashSet<>();
        adminAps2.add(1L);
        Set<Long> userAps2 = new HashSet<>();
        adminAps2.add(2L);
        approvalServiceAgreement2.setPermissionSetsAdmin(adminAps2);
        approvalServiceAgreement2.setPermissionSetsRegular(userAps2);
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement2);

        List<ApprovalServiceAgreementRef> approvalServiceAgreementRefs =
            approvalServiceAgreementRefJpaRepository.findAll(Sort.by("id"));

        assertEquals("Check if 1 object is same", approvalServiceAgreementRefs.get(0), approvalServiceAgreementRef);
        assertEquals("Check if 2 object is same", approvalServiceAgreementRefs.get(1), approvalServiceAgreement);
        assertEquals("Check if 3 object is same", approvalServiceAgreementRefs.get(2), approvalServiceAgreement2);
        assertEquals("Checking number of records from database", 3, approvalServiceAgreementRefs.size());
    }

    @Test
    @Transactional
    public void testDeleteApprovalServiceAgreementRef() {

        ApprovalServiceAgreementRef approvalServiceAgreementRef = new ApprovalServiceAgreementRef();
        approvalServiceAgreementRef.setApprovalId("appId1");
        approvalServiceAgreementRef.setServiceAgreementId("SA 1");

        long id = approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreementRef).getId();

        approvalServiceAgreementRefJpaRepository.deleteById(id);

        assertEquals("Checking number of records in database is 0",
            0, approvalServiceAgreementRefJpaRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testUpdateApprovalServiceAgreementRef() {

        ApprovalServiceAgreementRef approvalServiceAgreementRef = new ApprovalServiceAgreementRef();
        approvalServiceAgreementRef.setApprovalId("appId1");
        approvalServiceAgreementRef.setServiceAgreementId("SA 1");

        approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreementRef);

        approvalServiceAgreementRef.setServiceAgreementId("SA EDIT");
        approvalServiceAgreementRefJpaRepository.save(approvalServiceAgreementRef);
        ApprovalServiceAgreementRef result = approvalServiceAgreementRefJpaRepository.findAll().get(0);

        assertEquals("Checking if function group id is updated", "SA EDIT", result.getServiceAgreementId());
    }

    @Test
    @Transactional
    public void testFindByApprovalID() {

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId("approvalId3");
        approvalServiceAgreement.setServiceAgreementId("saId");
        approvalServiceAgreement.setCreatorLegalEntityId("leCreator");
        approvalServiceAgreement.setName("sa1");
        approvalServiceAgreement.setDescription("desc");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setEndDate(new Date(100000000));
        approvalServiceAgreement.setStartDate(new Date(10));
        approvalServiceAgreement.setExternalId("exSa2");
        Map<String, String> additions2 = new HashMap<>();
        additions2.put("key", "value");
        approvalServiceAgreement.setAdditions(additions2);
        Set<Long> adminAps2 = new HashSet<>();
        adminAps2.add(1L);
        Set<Long> userAps2 = new HashSet<>();
        adminAps2.add(2L);
        approvalServiceAgreement.setPermissionSetsAdmin(adminAps2);
        approvalServiceAgreement.setPermissionSetsRegular(userAps2);
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        Optional<ApprovalServiceAgreementRef> response = approvalServiceAgreementRefJpaRepository.findByApprovalId("approvalId3");
        ApprovalServiceAgreement result = (ApprovalServiceAgreement) response.get();
        assertEquals("saId", result.getServiceAgreementId());
        assertEquals("leCreator", result.getCreatorLegalEntityId());
    }


    @Test
    @Transactional
    public void testSaveApprovalServiceAgreementGroup() {
        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId("approvalId2");
        approvalServiceAgreement.setServiceAgreementId("saId");
        approvalServiceAgreement.setCreatorLegalEntityId("leCreator");
        approvalServiceAgreement.setName("sa1");
        approvalServiceAgreement.setDescription("desc");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setEndDate(new Date(100000000));
        approvalServiceAgreement.setStartDate(new Date(10));
        approvalServiceAgreement.setExternalId("exSa");
        Map<String, String> additions = new HashMap<>();
        additions.put("key", "value");
        approvalServiceAgreement.setAdditions(additions);
        Set<Long> adminAps = new HashSet<>();
        adminAps.add(1L);
        Set<Long> userAps = new HashSet<>();
        adminAps.add(2L);
        approvalServiceAgreement.setPermissionSetsAdmin(adminAps);
        approvalServiceAgreement.setPermissionSetsRegular(userAps);
        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);
        ApprovalServiceAgreementRef result = approvalServiceAgreementJpaRepository.findAll().get(0);
        assertEquals("Checking if created", approvalServiceAgreement, result);
    }
}
