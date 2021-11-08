package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import java.util.List;
import org.apache.camel.Body;

public interface GetArrangementPrivilegesRouteProxy {

    InternalRequest<List<ArrangementPrivilegesGetResponseBody>> getArrangementPrivileges(
        @Body InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest);
}
