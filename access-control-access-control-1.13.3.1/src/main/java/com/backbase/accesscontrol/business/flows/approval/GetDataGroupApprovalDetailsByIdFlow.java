package com.backbase.accesscontrol.business.flows.approval;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.GetDataGroupApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetDataGroupApprovalDetailsByIdFlow extends
    AbstractFlow<GetDataGroupApprovalDetailsParametersFlow, PresentationDataGroupApprovalDetailsItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDataGroupApprovalDetailsByIdFlow.class);
    private AccessControlApprovalService accessControlApprovalService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PresentationDataGroupApprovalDetailsItem execute(GetDataGroupApprovalDetailsParametersFlow params) {

        LOGGER.info("Trying to get approval by id {}, service agreement id {}, user id {}",
            params.getApprovalId(),
            params.getServiceAgreementId(),
            params.getUserId());

        return accessControlApprovalService
            .getPersistenceApprovalDataGroups(params.getApprovalId(),
                params.getServiceAgreementId(), params.getUserId());

    }

}
