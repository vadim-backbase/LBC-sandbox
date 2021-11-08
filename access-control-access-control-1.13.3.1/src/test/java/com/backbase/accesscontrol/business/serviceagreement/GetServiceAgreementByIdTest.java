package com.backbase.accesscontrol.business.serviceagreement;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.mappers.ServiceAgreementGetByIdMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetServiceAgreementByIdTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @InjectMocks
    private GetServiceAgreementById getServiceAgreementById;

    @Mock
    private ServiceAgreementGetByIdMapper serviceAgreementGetByIdMapper;

    @Test
    public void shouldPassIfGetFunctionAccessGroupIsInvokedInClientWithIdParameter() {
        String serviceAgreementId = "001";
        String serviceAgreementName = "name";

        ServiceAgreementItem data = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withValidFrom(new Date())
            .withValidUntil(new Date())
            .withStatus(
                com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status.DISABLED)
            .withName(serviceAgreementName);

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(data);

        getServiceAgreementById.getServiceAgreementById(new InternalRequest(), serviceAgreementId);
        verify(persistenceServiceAgreementService)
            .getServiceAgreementResponseBodyById(eq(serviceAgreementId));

        verify(serviceAgreementGetByIdMapper, times(1)).mapSingle(refEq(data));
    }

    @Test
    public void shouldReturnApprovalIdIfServiceAgreementIsPending() {
        String serviceAgreementId = "001";
        String serviceAgreementName = "name";
        String externalSaId = "externalSaId";
        String approvalId = "approvalId";

        ServiceAgreementItem data = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withExternalId(externalSaId)
            .withValidFrom(new Date())
            .withValidUntil(new Date())
            .withStatus(
                com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status.DISABLED)
            .withName(serviceAgreementName);

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(data);
        ApprovalServiceAgreement approvalSa = new ApprovalServiceAgreement();
        approvalSa.setExternalId(externalSaId);
        approvalSa.setApprovalId(approvalId);
        ServiceAgreementItemGetResponseBody mappedSa = new ServiceAgreementItemGetResponseBody()
            .withExternalId(externalSaId)
            .withApprovalId(approvalId)
            .withName(serviceAgreementName)
            .withStatus(Status.DISABLED)
            .withId(serviceAgreementId);
        when(serviceAgreementGetByIdMapper.mapSingle(data)).thenReturn(mappedSa);
        InternalRequest<ServiceAgreementItemGetResponseBody> body = getServiceAgreementById
            .getServiceAgreementById(new InternalRequest(), serviceAgreementId);

        verify(serviceAgreementGetByIdMapper, times(1)).mapSingle(refEq(data));
        assertEquals(approvalId, body.getData().getApprovalId());

    }
}
