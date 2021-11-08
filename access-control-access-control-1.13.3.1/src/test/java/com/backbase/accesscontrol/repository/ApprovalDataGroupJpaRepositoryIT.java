package com.backbase.accesscontrol.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ApprovalDataGroupJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    ApprovalDataGroupJpaRepository testy;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @Before
    public void init(){
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void saveAndReadTest() {
        ApprovalDataGroup deleteDataGroup = new ApprovalDataGroup();
        deleteDataGroup.setApprovalId("app1");
        deleteDataGroup.setDataGroupId("d1");

        testy.save(deleteDataGroup);

        ApprovalDataGroupDetails createDataGroup = new ApprovalDataGroupDetails();
        createDataGroup.setApprovalId("app2");
        createDataGroup.setName("name2");
        createDataGroup.setDescription("desc2");
        createDataGroup.setServiceAgreementId("sa2");
        createDataGroup.setType("type2");

        testy.save(createDataGroup);

        ApprovalDataGroupDetails updateDataGroup = new ApprovalDataGroupDetails();
        updateDataGroup.setDataGroupId("d3");
        updateDataGroup.setApprovalId("app3");
        updateDataGroup.setName("name3");
        updateDataGroup.setDescription("desc3");
        updateDataGroup.setServiceAgreementId("sa3");
        updateDataGroup.setType("type3");

        testy.save(updateDataGroup);

        List<ApprovalDataGroup> res = testy.findAll().stream()
            .sorted(Comparator.comparing(ApprovalDataGroup::getId))
            .peek(dataGroup -> assertEquals(ApprovalCategory.MANAGE_DATA_GROUPS, dataGroup.getApprovalCategory()))
            .collect(Collectors.toList());

        assertEquals("d1", res.get(0).getDataGroupId());
        assertEquals(ApprovalAction.DELETE, res.get(0).getApprovalAction());
        assertEquals("app1", res.get(0).getApprovalId());

        assertNull(res.get(1).getDataGroupId());
        assertEquals(ApprovalAction.CREATE, res.get(1).getApprovalAction());
        assertEquals("app2", res.get(1).getApprovalId());

        assertEquals("d3", res.get(2).getDataGroupId());
        assertEquals(ApprovalAction.EDIT, res.get(2).getApprovalAction());
        assertEquals("app3", res.get(2).getApprovalId());

        ApprovalDataGroupDetails details = (ApprovalDataGroupDetails) res.get(1);

        assertEquals("name2", details.getName());
        assertEquals("desc2", details.getDescription());
        assertEquals("type2", details.getType());

        details = (ApprovalDataGroupDetails) res.get(2);

        assertEquals("name3", details.getName());
        assertEquals("desc3", details.getDescription());
        assertEquals("type3", details.getType());
    }

}