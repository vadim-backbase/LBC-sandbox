package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface ListDataItemPrivilegesRouteProxy {

    InternalRequest<List<PresentationUserDataItemPermission>> getDataItemPrivileges(
        @Body InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest,
        @Header("dataItemType") String dataItemType,
        @Header("dataItemId") String dataItemId);
}
