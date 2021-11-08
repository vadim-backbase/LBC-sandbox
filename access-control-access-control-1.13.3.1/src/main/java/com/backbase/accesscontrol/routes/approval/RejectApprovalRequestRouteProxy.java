package com.backbase.accesscontrol.routes.approval;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface RejectApprovalRequestRouteProxy {

    InternalRequest<PresentationApprovalStatus> rejectApprovalRequest(
        @Header("approvalId") String approvalId,
        @Body InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId);

}
