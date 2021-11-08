package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetAssignedUserPermissionsRouteProxy {

    InternalRequest<PresentationApprovalPermissions> getAssignedUsersPermissions(
        @Body InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String id,
        @Header("userId") String userId);

}
