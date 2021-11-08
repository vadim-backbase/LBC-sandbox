package com.backbase.accesscontrol.business.flows.approval;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.GetServiceAgreementApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetServiceAgreementApprovalDetailsByIdFlow extends
    AbstractFlow<GetServiceAgreementApprovalDetailsParametersFlow, ServiceAgreementApprovalDetailsItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetServiceAgreementApprovalDetailsByIdFlow.class);
    private final AccessControlApprovalService accessControlApprovalService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final ServiceAgreementApprovalDetailsItem execute(
        GetServiceAgreementApprovalDetailsParametersFlow data) {

        LOGGER.info("Trying to get service agreement details with approval id {}, service agreement id {}, user id {}",
            data.getApprovalId(),
            data.getServiceAgreementId(),
            data.getUserId());

        return accessControlApprovalService
            .getPersistenceApprovalServiceAgreement(data.getApprovalId(),
                data.getServiceAgreementId(), data.getUserId());
    }
}
