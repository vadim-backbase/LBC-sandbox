package com.backbase.accesscontrol.business.flows.approval;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.GetFunctionGroupApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFunctionGroupApprovalDetailsByIdFlow extends
    AbstractFlow<GetFunctionGroupApprovalDetailsParametersFlow, PresentationFunctionGroupApprovalDetailsItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetFunctionGroupApprovalDetailsByIdFlow.class);
    private final AccessControlApprovalService accessControlApprovalService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PresentationFunctionGroupApprovalDetailsItem execute(
        GetFunctionGroupApprovalDetailsParametersFlow params) {

        LOGGER.info("Trying to get function group details with approval id {}, service agreement id {}, user id {}",
            params.getApprovalId(),
            params.getServiceAgreementId(),
            params.getUserId());

        return accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(params.getApprovalId(),
                params.getServiceAgreementId(), params.getUserId());
    }
}
