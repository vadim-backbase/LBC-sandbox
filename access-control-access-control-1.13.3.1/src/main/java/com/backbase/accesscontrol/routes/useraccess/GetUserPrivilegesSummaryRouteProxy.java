package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import java.util.List;
import org.apache.camel.Body;

/**
 * Get User Privileges Summary Route Proxy.
 */
public interface GetUserPrivilegesSummaryRouteProxy {

    InternalRequest<List<UserPermissionsSummaryGetResponseBody>> getUserPrivilegesSummary(
        @Body InternalRequest<Void> request);
}
