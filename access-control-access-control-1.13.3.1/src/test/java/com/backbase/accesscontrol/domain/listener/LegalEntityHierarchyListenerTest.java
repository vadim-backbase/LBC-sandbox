package com.backbase.accesscontrol.domain.listener;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import org.junit.Before;
import org.junit.Test;

public class LegalEntityHierarchyListenerTest {

    private LegalEntityHierarchyListener legalEntityHierarchyListener;

    @Before
    public void setUp() throws Exception {
        legalEntityHierarchyListener = new LegalEntityHierarchyListener();
    }

    @Test
    public void setHierarchyAttributes()  {
        LegalEntity parent = createLegalEntity(null, "parent", "parent", null, LegalEntityType.BANK);
        LegalEntity child = createLegalEntity(null, "child", "child", parent, LegalEntityType.CUSTOMER);
        legalEntityHierarchyListener.setAncestorsAndChildren(child);
        assertEquals(1, child.getLegalEntityAncestors().size());
        assertEquals(parent, child.getLegalEntityAncestors().get(0));
        assertEquals(LegalEntityType.CUSTOMER, child.getType());
    }
}