package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import java.util.Collection;
import java.util.List;

public interface FunctionGroupService {

    String addFunctionGroup(FunctionGroupBase functionGroupBase);

    String addSystemFunctionGroup(ServiceAgreement serviceAgreement, String functionGroupName,
        List<Permission> permissions);

    void updateFunctionGroup(String functionGroupId, FunctionGroupBase functionGroupBody);

    String updateFunctionGroupWithoutLegalEntity(String functionGroupId, FunctionGroupBase functionGroupBody);

    String deleteFunctionGroup(String id);

    List<FunctionsGetResponseBody> findAllBusinessFunctionsByServiceAgreement(String id, boolean isExternal);

    List<BulkFunctionGroupsPostResponseBody> getBulkFunctionGroups(Collection<String> ids);

    List<FunctionGroupsGetResponseBody> getFunctionGroupsByServiceAgreementId(String serviceAgreementId);

    FunctionGroupByIdGetResponseBody getFunctionGroupById(String functionGroupId);

    String getFunctionGroupsByNameAndServiceAgreementId(String name, String serviceAgreementId);

    String addFunctionGroupApproval(FunctionGroupApprovalBase requestData);

    void updateFunctionGroupApproval(FunctionGroupByIdPutRequestBody request, String functionGroupId,
        String approvalId);

    void deleteApprovalFunctionGroup(String functionGroupId, ApprovalDto requestData);

    PresentationFunctionGroupApprovalDetailsItem getByApprovalId(String approvalId);
}
