package com.backbase.accesscontrol.service.impl.strategy.approval;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ApprovalServiceAgreementUtil;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApproveUpdateServiceAgreementTest {

    @InjectMocks
    private ApproveUpdateServiceAgreement approveUpdateServiceAgreement;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ApprovalServiceAgreementUtil approvalServiceAgreementUtil;
    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    protected EventBus eventBus;

    @Test
    public void testShouldReturnFunctionGroupAsKey() {
        assertThat(approveUpdateServiceAgreement.getKey().approvalAction, is(ApprovalAction.EDIT));
        assertThat(approveUpdateServiceAgreement.getKey().approvalCategory,
            is(ApprovalCategory.MANAGE_SERVICE_AGREEMENT));
    }

    @Test
    public void testShouldUpdateServiceAgreementOnUpdatePendingItem() {
        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId("1");
        approvalServiceAgreement.setExternalId("saId");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setName("saName");
        AccessControlApproval approvalRequest = approvalServiceAgreement.withApprovalId("approvalId");

        when(approvalServiceAgreementUtil
            .transformApprovalServiceAgreementToServiceAgreement(eq(approvalServiceAgreement)))
            .thenReturn(new ServiceAgreement());
        doNothing().when(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        approveUpdateServiceAgreement.execute((ApprovalServiceAgreement) approvalRequest);
        verify(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        verify(persistenceServiceAgreementService, times(1)).update(any(ServiceAgreement.class));
    }

}
