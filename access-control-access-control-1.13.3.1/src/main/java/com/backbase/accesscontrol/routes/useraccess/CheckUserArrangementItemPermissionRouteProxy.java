package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface CheckUserArrangementItemPermissionRouteProxy {

    InternalRequest<Void> getArrangementPermissionCheck(
        @Body InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest,
        @Header("id") String arrangementId);
}
