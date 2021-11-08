package com.backbase.accesscontrol.routes.approval;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetPermissionsApprovalDetailsByIdRouteProxy {

    InternalRequest<PresentationPermissionsApprovalDetailsItem> getPermissionsApprovalById(
        @Body InternalRequest<Void> request,
        @Header("approvalId") String approvalId, @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId);

}
