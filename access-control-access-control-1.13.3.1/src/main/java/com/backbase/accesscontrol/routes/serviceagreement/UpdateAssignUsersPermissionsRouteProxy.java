package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface UpdateAssignUsersPermissionsRouteProxy {

    InternalRequest<PresentationApprovalStatus> putAssignUsersPermissions(
        @Body InternalRequest<PresentationFunctionDataGroupItems> internalRequest,
        @Header("serviceAgreementId") String id,
        @Header("userId") String userId);
}
