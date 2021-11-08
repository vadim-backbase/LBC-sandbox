package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import java.util.List;
import org.apache.camel.Body;

public interface AssignUserPermissionsBatchRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> assignUserPermissionsBatch(
        @Body InternalRequest<List<PresentationAssignUserPermissions>> userPermissions);
}
