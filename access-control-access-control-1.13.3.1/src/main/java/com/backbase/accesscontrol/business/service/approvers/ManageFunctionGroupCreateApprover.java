package com.backbase.accesscontrol.business.service.approvers;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ACTION_CREATE;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.persistence.approvals.ApproveApprovalHandler;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ManageFunctionGroupCreateApprover implements Approver {

    private final AccessControlApprovalService accessControlApprovalService;
    private FunctionGroupPAndPService functionGroupPAndPService;
    private final ApproveApprovalHandler approveApprovalHandler;

    @Override
    public void manageApprove(String approvalId, String serviceAgreementId, String userId) {

        PresentationFunctionGroupApprovalDetailsItem functionGroupDetails = accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(approvalId, serviceAgreementId, userId);

        approveApprovalHandler.handleRequest(new SingleParameterHolder<>(approvalId), null);

        if (nonNull(functionGroupDetails.getNewState().getApprovalTypeId())) {
            final Optional<FunctionGroupsGetResponseBody> functionGroupsGetResponseBody = functionGroupPAndPService
                .getFunctionGroups(functionGroupDetails.getServiceAgreementId()).stream()
                .filter(functionGroup -> functionGroup.getName().equals(functionGroupDetails.getNewState().getName()))
                .findFirst();
            if (functionGroupsGetResponseBody.isPresent()) {
                String functionGroupId = functionGroupsGetResponseBody.get().getId();
                accessControlApprovalService.createApprovalType(functionGroupId,
                    functionGroupDetails.getNewState().getApprovalTypeId());
            }
        }
    }

    @Override
    public ApproverKey getKey() {
        return new ApproverKey(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, ACTION_CREATE);
    }
}
