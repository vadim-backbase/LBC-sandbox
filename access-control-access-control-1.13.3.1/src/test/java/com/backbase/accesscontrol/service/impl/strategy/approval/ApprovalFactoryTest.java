package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalFactoryTest {

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private EventBus eventBus;

    @Test
    public void shouldFailForNotImplementedTypeCommandCombination() {

        ApprovalFactory approvalFactory = new ApprovalFactory(new ArrayList<>());
        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
            () -> approvalFactory.getApprovalItem(ApprovalAction.CREATE, ApprovalCategory.MANAGE_DATA_GROUPS));

        assertEquals("Invalid approval action/category", exception.getMessage());
    }

    @Test
    public void shouldPassSuccessfully() {
        ApprovalDataGroupDetails body = new ApprovalDataGroupDetails();
        body.setName("name");
        body.setDescription("desc");
        body.setType("type");
        body.setItems(Sets.newHashSet("1", "2"));
        ApprovalItem item = new ApproveCreateDataGroup(null, dataGroupService, eventBus);
        List<ApprovalItem<? extends AccessControlApproval, ?>> approvalItems = Collections.singletonList(item);
        ApprovalFactory approvalFactory = new ApprovalFactory(approvalItems);
        ApprovalItem approvalItem = approvalFactory
            .getApprovalItem(ApprovalAction.CREATE, ApprovalCategory.MANAGE_DATA_GROUPS);
        assertNotNull(approvalItem);
        assertEquals(approvalItem, item);
    }

}