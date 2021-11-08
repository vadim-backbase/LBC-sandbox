package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.approval.GetDataGroupApprovalDetailsByIdFlow;
import com.backbase.accesscontrol.business.flows.approval.GetFunctionGroupApprovalDetailsByIdFlow;
import com.backbase.accesscontrol.business.flows.approval.GetServiceAgreementApprovalDetailsByIdFlow;
import com.backbase.accesscontrol.dto.GetDataGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.GetFunctionGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.GetServiceAgreementApprovalDetailsParametersFlow;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ApprovalFlowService {

    private GetDataGroupApprovalDetailsByIdFlow getDataGroupApprovalDetailsByIdFlow;
    private GetFunctionGroupApprovalDetailsByIdFlow getFunctionGroupApprovalDetailsByIdFlow;
    private GetServiceAgreementApprovalDetailsByIdFlow getServiceAgreementApprovalDetailsByIdFlow;


    /**
     * Get data group approval details.
     *
     * @param parameters - {@link GetDataGroupApprovalDetailsParametersFlow}
     * @return {@link PresentationDataGroupApprovalDetailsItem}
     */
    public PresentationDataGroupApprovalDetailsItem getDataGroupApprovalDetailsById(
        GetDataGroupApprovalDetailsParametersFlow parameters) {
        return getDataGroupApprovalDetailsByIdFlow.start(parameters);
    }

    /**
     * Get function group approval details.
     *
     * @param parameters - {@link GetFunctionGroupApprovalDetailsParametersFlow}
     * @return {@link PresentationFunctionGroupApprovalDetailsItem}
     */
    public PresentationFunctionGroupApprovalDetailsItem getFunctionGroupApprovalDetailsById(
        GetFunctionGroupApprovalDetailsParametersFlow parameters) {
        return getFunctionGroupApprovalDetailsByIdFlow.start(parameters);
    }

    public ServiceAgreementApprovalDetailsItem getServiceAgreementDetailsById(String approvalId,
        String serviceAgreementId, String loggedUserId) {
        return getServiceAgreementApprovalDetailsByIdFlow
            .start(new GetServiceAgreementApprovalDetailsParametersFlow(approvalId, serviceAgreementId, loggedUserId));
    }
}
