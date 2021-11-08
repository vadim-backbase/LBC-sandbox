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
public class ApproveCreateServiceAgreementTest {

    @InjectMocks
    private ApproveCreateServiceAgreement approveCreateServiceAgreement;
    @Mock
    private ApprovalServiceAgreementUtil approvalServiceAgreementUtil;
    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    protected EventBus eventBus;

    @Test
    public void testShouldReturnFunctionGroupAsKey() {
        assertThat(approveCreateServiceAgreement.getKey().approvalAction, is(ApprovalAction.CREATE));
        assertThat(approveCreateServiceAgreement.getKey().approvalCategory,
            is(ApprovalCategory.MANAGE_SERVICE_AGREEMENT));
    }

    @Test
    public void testShouldSaveServiceAgreementOnCreatePendingItem() {

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId(null);
        approvalServiceAgreement.setExternalId("saId");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setName("saName");
        AccessControlApproval approvalRequest = approvalServiceAgreement.withApprovalId("approvalId");
        ServiceAgreement sa = new ServiceAgreement().withId("id");

        when(approvalServiceAgreementUtil
            .transformApprovalServiceAgreementToServiceAgreement(eq(approvalServiceAgreement)))
            .thenReturn(sa);
        when(persistenceServiceAgreementService
            .save(eq(sa)))
            .thenReturn(sa);

        doNothing().when(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        approveCreateServiceAgreement.execute((ApprovalServiceAgreement) approvalRequest);
        verify(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        verify(persistenceServiceAgreementService, times(1)).save(any(ServiceAgreement.class));
    }

}
