package com.backbase.accesscontrol.repository;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

public class ApprovalFunctionGroupJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;

    @Autowired
    ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    private Set<String> function_privileges = new HashSet<>();

    @Before
    public void init() {
        repositoryCleaner.clean();
        function_privileges.add("TEST FP 1");
        function_privileges.add("TEST FP 2");
        function_privileges.add("TEST FP 3");
    }

    @Test
    @Transactional
    public void testGetApprovalFunctionIdsForUpdateAndDelete() {
        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId("DeleteFG1");
        approvalFunctionGroupRef.setFunctionGroupId("FG1");
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef);
        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId("FG2");
        approvalFunctionGroup.setApprovalId("Approval Id 1");
        approvalFunctionGroup.setDescription("Test Description 1");
        approvalFunctionGroup.setName("Test Name 1");
        approvalFunctionGroup.setServiceAgreementId("Test SA 1");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setPrivileges(function_privileges);
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        Set<String> fgIds = new HashSet<>();
        fgIds.add("FG2");
        fgIds.add("FG1");
        Optional<List<ApprovalFunctionGroupRef>> approvalFunctionGroupList =
            approvalFunctionGroupRefJpaRepository.findByFunctionGroupIdIn(fgIds);
        assertEquals("Should return 2 records", 2, approvalFunctionGroupList.get().size());
        approvalFunctionGroupList
            .get()
            .forEach(a -> {
                if (a.getApprovalAction().equals(ApprovalAction.DELETE)) {
                    assertEquals("FG1", a.getFunctionGroupId());
                } else {
                    assertEquals("FG2", a.getFunctionGroupId());
                }
            });
    }

    @Test
    @Transactional
    public void testSaveAndReadApprovalFunctionGroupsRef() {

        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId("appId1");
        approvalFunctionGroupRef.setFunctionGroupId("FG 1");
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef);

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId("Approval Id 1");
        approvalFunctionGroup.setDescription("Test Description 1");
        approvalFunctionGroup.setName("Test Name 1");
        approvalFunctionGroup.setServiceAgreementId("Test SA 1");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setApprovalTypeId("TEST A 1");
        approvalFunctionGroup.setPrivileges(function_privileges);
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroup);

        ApprovalFunctionGroup approvalFunctionGroup2 = new ApprovalFunctionGroup();
        approvalFunctionGroup2.setApprovalId("ApprovalId 2");
        approvalFunctionGroup2.setDescription("Test Description 2");
        approvalFunctionGroup2.setName("Test Name 2");
        approvalFunctionGroup2.setServiceAgreementId("Test SA 2");
        approvalFunctionGroup2.setStartDate(new Date());
        approvalFunctionGroup2.setApprovalTypeId("TEST B");
        function_privileges.clear();
        function_privileges.add("TEST FP 4");
        approvalFunctionGroup2.setPrivileges(function_privileges);
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroup2);

        List<ApprovalFunctionGroupRef> approvalFunctionGroupRefs =
            approvalFunctionGroupRefJpaRepository.findAll(Sort.by("id"));

        assertEquals("Check if 1 object is same", approvalFunctionGroupRefs.get(0), approvalFunctionGroupRef);
        assertEquals("Check if 2 object is same", approvalFunctionGroupRefs.get(1), approvalFunctionGroup);
        assertEquals("Check if 3 object is same", approvalFunctionGroupRefs.get(2), approvalFunctionGroup2);
        assertEquals("Checking number of records from database", 3, approvalFunctionGroupRefs.size());
    }

    @Test
    @Transactional
    public void testDeleteApprovalFunctionGroupRef() {

        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId("appId1");
        approvalFunctionGroupRef.setFunctionGroupId("FG 1");

        long id = approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef).getId();

        approvalFunctionGroupRefJpaRepository.deleteById(id);

        assertEquals("Checking number of records in database is 0",
            0, approvalFunctionGroupRefJpaRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testUpdateApprovalFunctionGroupRef() {

        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setApprovalId("appId1");
        approvalFunctionGroupRef.setFunctionGroupId("FG 1");

        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef);

        approvalFunctionGroupRef.setFunctionGroupId("FG EDIT");
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef);
        ApprovalFunctionGroupRef result = approvalFunctionGroupRefJpaRepository.findAll().get(0);

        assertEquals("Checking if function group id is updated", "FG EDIT", result.getFunctionGroupId());
    }

    @Test
    @Transactional
    public void testSaveApprovalFunctionGroup() {

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId("FG 1");
        approvalFunctionGroup.setApprovalId("Approval Id 1");
        approvalFunctionGroup.setDescription("Test Description 1");
        approvalFunctionGroup.setName("Test Name 1");
        approvalFunctionGroup.setServiceAgreementId("Test SA 1");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setApprovalTypeId("TEST B");
        approvalFunctionGroup.setPrivileges(function_privileges);
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        ApprovalFunctionGroupRef result = approvalFunctionGroupJpaRepository.findAll().get(0);
        assertEquals("Checking if created", approvalFunctionGroup, result);
    }

    @Test
    @Transactional
    public void testUpdateApprovalFunctionGroup() {

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId("FG 1");
        approvalFunctionGroup.setApprovalId("Approval Id 1");
        approvalFunctionGroup.setDescription("Test Description 1");
        approvalFunctionGroup.setName("Test Name 1");
        approvalFunctionGroup.setServiceAgreementId("Test SA 1");
        approvalFunctionGroup.setStartDate(new Date());
        approvalFunctionGroup.setApprovalTypeId("TEST B");
        approvalFunctionGroup.setPrivileges(function_privileges);
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);

        String edited = "EDITED";
        approvalFunctionGroup.setApprovalTypeId(edited);
        approvalFunctionGroup.setFunctionGroupId(edited);
        approvalFunctionGroup.setName(edited);
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        ApprovalFunctionGroup result = approvalFunctionGroupJpaRepository.findAll().get(0);

        assertEquals("Checking if approvalType is updated", edited, result.getApprovalTypeId());
        assertEquals("Checking if functionGroupId is updated", edited, result.getFunctionGroupId());
        assertEquals("Checking if name is updated", edited, result.getName());
    }

}
