package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApproveUpdateDataGroupTest {

    @InjectMocks
    private ApproveUpdateDataGroup approveUpdateDataGroup;

    @Mock
    private DataGroupService dataGroupService;

    @Mock
    private AccessControlApprovalJpaRepository repository;

    @Mock
    protected EventBus eventBus;

    @Test
    public void shouldReturnDataGroupAsKey() {
        assertThat(approveUpdateDataGroup.getKey().approvalAction, is(ApprovalAction.EDIT));
        assertThat(approveUpdateDataGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_DATA_GROUPS));
    }

    @Test
    public void shouldCallSaveDataGroupOnCreatePendingItem() {

        String id = "123";
        String description = "description";
        String name = "Name";
        String type = "type";
        String serviceAgreement = "SA-01";
        List<String> items = Collections.singletonList("item1");

        ApprovalDataGroupDetails approvalDataGroup = new ApprovalDataGroupDetails();
        approvalDataGroup.setDataGroupId(id);
        approvalDataGroup.setDescription(description);
        approvalDataGroup.setName(name);
        approvalDataGroup.setType(type);
        approvalDataGroup.setServiceAgreementId(serviceAgreement);
        approvalDataGroup.setItems(new HashSet<>(items));

        DataGroupByIdPutRequestBody data = new DataGroupByIdPutRequestBody()
            .withId(id)
            .withName(name)
            .withDescription(description)
            .withServiceAgreementId(serviceAgreement)
            .withItems(items)
            .withType(type);

        approveUpdateDataGroup.execute(approvalDataGroup);

        verify(repository).delete(eq(approvalDataGroup));
        verify(dataGroupService, times(1)).update(eq(id), eq(data));
    }

}